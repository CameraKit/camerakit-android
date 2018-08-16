package camerakit.android.gradle

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.plugins.AndroidMavenPlugin
import org.gradle.api.plugins.MavenRepositoryHandlerConvention
import org.gradle.api.tasks.Upload
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper

class CameraKitLibraryPlugin implements Plugin<Project> {

    private static final String GROUP = 'camerakit'
    private static final String VERSION = '1.0.0-beta4'

    private static final int COMPILE_SDK = 28
    private static final int TARGET_SDK = 28
    private static final int MIN_SDK = 16

    void apply(Project project) {
        project.buildscript {
            repositories {
                google()
                jcenter()
            }

            dependencies {
                classpath 'com.android.tools.build:gradle:3.1.4'
                classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.60'
                classpath 'com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.1'
                classpath 'com.github.dcendents:android-maven-gradle-plugin:2.1'
            }
        }

        project.plugins.apply(LibraryPlugin)
        project.plugins.apply(KotlinAndroidPluginWrapper)
        project.plugins.apply(AndroidMavenPlugin)

        project.repositories {
            google()
            jcenter()
        }

        project.dependencies {
            implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.2.60'
            implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:0.24.0'
            implementation 'com.android.support:appcompat-v7:27.1.1'
        }

        def kotlin = project.extensions.getByName('kotlin') as KotlinProjectExtension
        kotlin.experimental.setCoroutines(Coroutines.ENABLE)

        def android = project.extensions.getByName('android') as BaseExtension
        android.compileSdkVersion(COMPILE_SDK)
        android.defaultConfig.targetSdkVersion(TARGET_SDK)
        android.defaultConfig.minSdkVersion(MIN_SDK)

        project.group = GROUP
        project.version = VERSION

        def camerakit =
            project.extensions
                .create('camerakit', CameraKitLibraryExtension, project)

        project.afterEvaluate {
            Upload installTask = project.tasks.getByName('install')
            MavenRepositoryHandlerConvention repositories =
                new DslObject(installTask.getRepositories())
                    .getConvention()
                    .getPlugin(MavenRepositoryHandlerConvention.class)

            repositories.mavenInstaller {
                pom.project {
                    groupId project.group
                    artifactId camerakit.artifact
                    packaging 'aar'
                }
            }

            if (camerakit.publish) {
                setupPublishing(project, camerakit, android)
            }
        }
    }

    void setupPublishing(Project project, CameraKitLibraryExtension camerakit, LibraryExtension android) {
        project.plugins.apply(BintrayPlugin)

        def configFile = new File(project.rootDir, 'camerakit.json')
        def config = new JsonSlurper().parseText(configFile.text)

        BintrayExtension bintray = project.extensions.findByType(BintrayExtension)
        bintray.setUser(config.bintrayUser)
        bintray.setKey(config.bintrayKey)
        bintray.setConfigurations('archives')
        bintray.pkg.setName(camerakit.name)
        bintray.pkg.setRepo(config.bintrayRepo)
        bintray.pkg.setUserOrg(config.bintrayOrg)
        bintray.pkg.setVcsUrl(config.vcsUrl)
        bintray.pkg.setLicenses('MIT')
        bintray.pkg.version {
            name = project.version
            released = new Date()
        }

        project.task('cleanForDeployment') {
            doLast {
                project.delete(project.buildDir)
            }
        }

        project.task('buildForDeployment') {
            shouldRunAfter('cleanForDeployment')
            finalizedBy('assemble')

            doFirst {
                android.variantFilter { variant ->
                    if (variant.buildType.name == 'debug') {
                        variant.setIgnore(true)
                    }
                }
            }
        }

        project.task('deployRelease') {
            shouldRunAfter('buildForDeployment')
            dependsOn('cleanForDeployment')
            dependsOn('buildForDeployment')
            finalizedBy('bintrayUpload')
        }
    }

}

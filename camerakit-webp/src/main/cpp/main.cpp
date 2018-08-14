/**
 *
 */

#include <jni.h>

namespace camerakit {
  namespace jni {
//    extern int register_CameraSurfaceTexture(JNIEnv* env);
//    extern int register_GLEnvironment(JNIEnv* env);
//    extern int register_GLPreviewFrame(JNIEnv* env);
//    extern int register_GLPreviewProgram(JNIEnv* env);
//    extern int register_NativePreviewFrame(JNIEnv* env);
//    extern int register_NativePreviewProgram(JNIEnv* env);
  }
}

using namespace camerakit;

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

//    jni::register_CameraSurfaceTexture(env);
//    jni::register_GLEnvironment(env);
//    jni::register_GLPreviewFrame(env);
//    jni::register_GLPreviewProgram(env);
//    jni::register_NativePreviewFrame(env);
//    jni::register_NativePreviewProgram(env);

    return JNI_VERSION_1_6;
}

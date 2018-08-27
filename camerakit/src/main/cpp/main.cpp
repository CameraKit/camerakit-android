/**
 *
 */

#include <jni.h>

namespace camerakit {
  namespace jni {
    extern int register_CameraSurfaceView(JNIEnv* env);
    extern int register_CameraSurfaceTexture(JNIEnv* env);
  }
}

using namespace camerakit;

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    jni::register_CameraSurfaceView(env);
    jni::register_CameraSurfaceTexture(env);
    return JNI_VERSION_1_6;
}

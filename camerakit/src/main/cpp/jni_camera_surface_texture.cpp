/**
 *
 */

#include <jni.h>

#include "camerakit/CameraSurfaceTexture.hpp"

namespace camerakit {

  struct fields_t {
      jfieldID nativeHandle;
  };

  static fields_t fields;

  // ----------------------------------------------------------------------------------------

  static void CameraSurfaceTexture_setCameraSurfaceTexture(JNIEnv* env, jobject thiz, const CameraSurfaceTexture* cameraSurfaceTexture) {
      CameraSurfaceTexture* const currentObject
          = (CameraSurfaceTexture*) env->GetLongField(thiz, fields.nativeHandle);

      if (currentObject) {
          // TODO: stop old
      }

      env->SetLongField(thiz, fields.nativeHandle, reinterpret_cast<jlong>(cameraSurfaceTexture));
  }

  static CameraSurfaceTexture* CameraSurfaceTexture_getCameraSurfaceTexture(JNIEnv* env, jobject thiz) {
      long handle = (long) env->GetLongField(thiz, fields.nativeHandle);
      return reinterpret_cast<CameraSurfaceTexture*>(handle);
  }

  // ----------------------------------------------------------------------------------------

  static void CameraSurfaceTexture_init(JNIEnv* env, jobject thiz, jint inputTexture, jint outputTexture) {
      CameraSurfaceTexture* cameraSurfaceTexture = new CameraSurfaceTexture(inputTexture, outputTexture);
      CameraSurfaceTexture_setCameraSurfaceTexture(env, thiz, cameraSurfaceTexture);

      jclass clazz = env->GetObjectClass(thiz);
      if (clazz == NULL) {
          // TODO: throw error
          return;
      }
  }

  static void CameraSurfaceTexture_setSize(JNIEnv* env, jobject thiz, jint width, jint height) {
      CameraSurfaceTexture* cameraSurfaceTexture = CameraSurfaceTexture_getCameraSurfaceTexture(env, thiz);
      cameraSurfaceTexture->setSize(width, height);
  }

  static void CameraSurfaceTexture_updateTexImage(JNIEnv* env, jobject thiz, jfloatArray transformMatrix, jfloatArray extraTransformMatrix) {
      CameraSurfaceTexture* cameraSurfaceTexture = CameraSurfaceTexture_getCameraSurfaceTexture(env, thiz);
      jfloat* matrix = env->GetFloatArrayElements(transformMatrix, 0);
      jfloat* extraMatrix = env->GetFloatArrayElements(extraTransformMatrix, 0);
      cameraSurfaceTexture->updateTexImage(matrix, extraMatrix);
      env->ReleaseFloatArrayElements(transformMatrix, matrix, 0);
      env->ReleaseFloatArrayElements(extraTransformMatrix, extraMatrix, 0);
  }

  static void CameraSurfaceTexture_finalize(JNIEnv* env, jobject thiz) {
      CameraSurfaceTexture* cameraSurfaceTexture = CameraSurfaceTexture_getCameraSurfaceTexture(env, thiz);

      // TODO: stop resources

      CameraSurfaceTexture_setCameraSurfaceTexture(env, thiz, 0);
  }


  static void CameraSurfaceTexture_release(JNIEnv* env, jobject thiz) {
      CameraSurfaceTexture* cameraSurfaceTexture = CameraSurfaceTexture_getCameraSurfaceTexture(env, thiz);
  }

  // ----------------------------------------------------------------------------------------

  namespace jni {

    const char* const classPathName = "com/camerakit/preview/CameraSurfaceTexture";

    static JNINativeMethod methods[] = {
        {"nativeInit",           "(II)V",   (void*) CameraSurfaceTexture_init},
        {"nativeSetSize",        "(II)V",   (void*) CameraSurfaceTexture_setSize},
        {"nativeUpdateTexImage", "([F[F)V", (void*) CameraSurfaceTexture_updateTexImage},
        {"nativeFinalize",       "()V",     (void*) CameraSurfaceTexture_finalize},
        {"nativeRelease",        "()V",     (void*) CameraSurfaceTexture_release}
    };

    int register_CameraSurfaceTexture(JNIEnv* env) {
        jclass clazz = env->FindClass(classPathName);
        if (clazz == NULL) {
            // TODO: throw
            return -1;
        }

        jfieldID nativeHandle = env->GetFieldID(clazz, "nativeHandle", "J");
        if (nativeHandle == NULL) {
            // TODO: throw
            return -1;
        }

        fields.nativeHandle = nativeHandle;

        int result = env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0]));
        return result;
    }

  }

}

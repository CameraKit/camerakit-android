/**
 *
 */

#include <jni.h>

#include "camerakit/CameraSurfaceView.hpp"

namespace camerakit {

  struct fields_t {
      jfieldID nativeHandle;
  };

  static fields_t fields;

  // ----------------------------------------------------------------------------------------

  static void CameraSurfaceView_setCameraSurfaceView(JNIEnv* env, jobject thiz, const CameraSurfaceView* cameraSurfaceView) {
      CameraSurfaceView* const currentObject
              = (CameraSurfaceView*) env->GetLongField(thiz, fields.nativeHandle);

      if (currentObject) {
          // TODO: stop old
      }

      env->SetLongField(thiz, fields.nativeHandle, reinterpret_cast<jlong>(cameraSurfaceView));
  }

  static CameraSurfaceView* CameraSurfaceView_getCameraSurfaceView(JNIEnv* env, jobject thiz) {
      long handle = (long) env->GetLongField(thiz, fields.nativeHandle);
      return reinterpret_cast<CameraSurfaceView*>(handle);
  }

  // ----------------------------------------------------------------------------------------

  static void CameraSurfaceView_init(JNIEnv* env, jobject thiz) {
      CameraSurfaceView* cameraSurfaceView = new CameraSurfaceView();
      CameraSurfaceView_setCameraSurfaceView(env, thiz, cameraSurfaceView);

      jclass clazz = env->GetObjectClass(thiz);
      if (clazz == NULL) {
          // TODO: throw error
          return;
      }
  }

  static void CameraSurfaceView_onSurfaceCreated(JNIEnv* env, jobject thiz) {
      CameraSurfaceView* cameraSurfaceView = CameraSurfaceView_getCameraSurfaceView(env, thiz);
      cameraSurfaceView->onSurfaceCreated();
  }

  static void CameraSurfaceView_onSurfaceChanged(JNIEnv* env, jobject thiz, jint width, jint height) {
      CameraSurfaceView* cameraSurfaceView = CameraSurfaceView_getCameraSurfaceView(env, thiz);
      cameraSurfaceView->onSurfaceChanged(width, height);
  }

  static void CameraSurfaceView_onDrawFrame(JNIEnv* env, jobject thiz) {
      CameraSurfaceView* cameraSurfaceView = CameraSurfaceView_getCameraSurfaceView(env, thiz);
      cameraSurfaceView->onDrawFrame();
  }

  static void CameraSurfaceView_drawTexture(JNIEnv* env, jobject thiz, jint texture, jint textureWidth, jint textureHeight) {
      CameraSurfaceView* cameraSurfaceView = CameraSurfaceView_getCameraSurfaceView(env, thiz);
      cameraSurfaceView->drawTexture((GLuint) texture, textureWidth, textureHeight);
  }

  static void CameraSurfaceView_finalize(JNIEnv* env, jobject thiz) {
      CameraSurfaceView* cameraSurfaceView = CameraSurfaceView_getCameraSurfaceView(env, thiz);

      // TODO: stop resources

      CameraSurfaceView_setCameraSurfaceView(env, thiz, 0);
  }


  static void CameraSurfaceView_release(JNIEnv* env, jobject thiz) {
      CameraSurfaceView* cameraSurfaceView = CameraSurfaceView_getCameraSurfaceView(env, thiz);
      cameraSurfaceView->abandon();
  }

  // ----------------------------------------------------------------------------------------

  namespace jni {

    const char* const classPathName = "com/camerakit/preview/CameraSurfaceView";

    static JNINativeMethod methods[] = {
            {"nativeInit",             "()V",    (void*) CameraSurfaceView_init},
            {"nativeOnSurfaceCreated", "()V",    (void*) CameraSurfaceView_onSurfaceCreated},
            {"nativeOnSurfaceChanged", "(II)V",  (void*) CameraSurfaceView_onSurfaceChanged},
            {"nativeOnDrawFrame",      "()V",    (void*) CameraSurfaceView_onDrawFrame},
            {"nativeDrawTexture",      "(III)V", (void*) CameraSurfaceView_drawTexture},
            {"nativeFinalize",         "()V",    (void*) CameraSurfaceView_finalize},
            {"nativeRelease",          "()V",    (void*) CameraSurfaceView_release}
    };

    int register_CameraSurfaceView(JNIEnv* env) {
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

#include <jni.h>
#include <stdio.h>
#include <android/bitmap.h>
#include <cstring>
#include <unistd.h>
#include <vector>

#define  LOG_TAG    "DEBUG"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

extern "C"
{
JNIEXPORT jobject JNICALL
Java_com_wonderkiln_camerakit_YuvOperator_jniStoreYuvData(JNIEnv *env, jobject obj,
                                                          jbyteArray yuv420sp, jint width,
                                                          jint height);
JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_YuvOperator_jniRotateYuvCcw90(JNIEnv *env, jobject obj,
                                                            jobject handle);
JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_YuvOperator_jniRotateYuvCw90(JNIEnv *env, jobject obj,
                                                           jobject handle);
JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_YuvOperator_jniRotateYuv180(JNIEnv *env, jobject obj,
                                                          jobject handle);
JNIEXPORT jobject JNICALL
Java_com_wonderkiln_camerakit_YuvOperator_jniGetYuvData(JNIEnv *env,
                                                        jobject obj,
                                                        jobject handle);
JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_YuvOperator_jniFreeYuvData(JNIEnv *env, jobject obj,
                                                         jobject handle);
}

class JniYuvOperator {
public:
    unsigned char *_storedYuvData;
    int _width;
    int _height;
    int _length;

    JniYuvOperator() {
        _storedYuvData = NULL;
    }
};

JNIEXPORT jobject JNICALL
Java_com_wonderkiln_camerakit_YuvOperator_jniStoreYuvData(JNIEnv *env, jobject obj,
                                                          jbyteArray yuv420sp, jint width,
                                                          jint height) {
    int length = env->GetArrayLength(yuv420sp);
    unsigned char *yuv = new unsigned char[length];
    env->GetByteArrayRegion(yuv420sp, 0, length, reinterpret_cast<jbyte *>(yuv));

    JniYuvOperator *yuvOperator = new JniYuvOperator();
    yuvOperator->_storedYuvData = yuv;
    yuvOperator->_width = width;
    yuvOperator->_height = height;
    yuvOperator->_length = length;
    return env->NewDirectByteBuffer(yuvOperator, 0);
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_YuvOperator_jniRotateYuvCcw90(JNIEnv *env, jobject obj,
                                                            jobject handle) {
    JniYuvOperator *yuvOperator = (JniYuvOperator *) env->GetDirectBufferAddress(handle);
    unsigned char *yuv = yuvOperator->_storedYuvData;
    int width = yuvOperator->_width;
    int height = yuvOperator->_height;
    int length = yuvOperator->_length;

    std::vector<unsigned char> yuvCopy(yuv, yuv + length);
    int bufferSize = sizeof(yuvCopy);
    int n = 0;
    int uvHeight = height >> 1;
    int wh = width * height;

    if (bufferSize < width * height + width) {
        return;
    }

    for (int j = width - 1; j >= 0; j--) {
        for (int i = 0; i < height; i++) {
            yuv[n++] = yuvCopy[width * i + j];
        }
    }

    if (bufferSize < wh + width * uvHeight + width) {
        return;
    }

    for (int j = width - 1; j > 0; j -= 2) {
        for (int i = 0; i < uvHeight; i++) {
            yuv[n++] = yuvCopy[wh + width * i + j - 1];
            yuv[n++] = yuvCopy[wh + width * i + j];
        }
    }
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_YuvOperator_jniRotateYuvCw90(JNIEnv *env, jobject obj,
                                                           jobject handle) {
    JniYuvOperator *yuvOperator = (JniYuvOperator *) env->GetDirectBufferAddress(handle);
    unsigned char *yuv = yuvOperator->_storedYuvData;
    int width = yuvOperator->_width;
    int height = yuvOperator->_height;
    int length = yuvOperator->_length;


    std::vector<unsigned char> yuvCopy(yuv, yuv + length);
    int bufferSize = sizeof(yuvCopy);
    int wh = width * height;
    int k = 0;

    if (bufferSize < width * height + width) {
        return;
    }

    for (int i = 0; i < width; i++) {
        for (int j = height - 1; j >= 0; j--) {
            yuv[k] = yuvCopy[width * j + i];
            k++;
        }
    }

    if (bufferSize < wh + width * height / 2 + width) {
        return;
    }
    for (int i = 0; i < width; i += 2) {
        for (int j = height / 2 - 1; j >= 0; j--) {
            yuv[k] = yuvCopy[wh + width * j + i];
            yuv[k + 1] = yuvCopy[wh + width * j + i + 1];
            k += 2;
        }
    }
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_YuvOperator_jniRotateYuv180(JNIEnv *env, jobject obj,
                                                          jobject handle) {
    JniYuvOperator *yuvOperator = (JniYuvOperator *) env->GetDirectBufferAddress(handle);
    unsigned char *yuv = yuvOperator->_storedYuvData;
    int width = yuvOperator->_width;
    int height = yuvOperator->_height;
    int length = yuvOperator->_length;

    std::vector<unsigned char> yuvCopy(yuv, yuv + length);
    int bufferSize = sizeof(yuvCopy);
    int n = 0;
    int uh = height >> 1;
    int wh = width * height;

    if (bufferSize < width * height + width) {
        return;
    }

    for (int j = height - 1; j >= 0; j--) {
        for (int i = width - 1; i >= 0; i--) {
            yuv[n++] = yuvCopy[width * j + i];
        }
    }

    if (bufferSize < wh + width * uh) {
        return;
    }

    for (int j = uh - 1; j >= 0; j--) {
        for (int i = width - 1; i > 0; i -= 2) {
            yuv[n] = yuvCopy[wh + width * j + i - 1];
            yuv[n + 1] = yuvCopy[wh + width * j + i];
            n += 2;
        }
    }
}

JNIEXPORT jobject JNICALL
Java_com_wonderkiln_camerakit_YuvOperator_jniGetYuvData(JNIEnv *env, jobject obj,
                                                        jobject handle) {
    JniYuvOperator *yuvOperator = (JniYuvOperator *) env->GetDirectBufferAddress(handle);
    unsigned char *yuvData = yuvOperator->_storedYuvData;
    int length = yuvOperator->_length;

    jbyteArray array = env->NewByteArray(length);
    env->SetByteArrayRegion(array, 0, length, reinterpret_cast<jbyte *>(yuvData));
    return array;
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_YuvOperator_jniFreeYuvData(JNIEnv *env, jobject obj,
                                                         jobject handle) {
    JniYuvOperator *yuvOperator = (JniYuvOperator *) env->GetDirectBufferAddress(handle);
    if (yuvOperator->_storedYuvData == NULL) return;
    delete[] yuvOperator->_storedYuvData;
    yuvOperator->_storedYuvData = NULL;
    delete yuvOperator;
}

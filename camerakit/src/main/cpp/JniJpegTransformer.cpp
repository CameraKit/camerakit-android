#include <jni.h>
#include <stdio.h>
#include <cstring>
#include <unistd.h>
#include <turbojpeg.h>

extern "C"
{
JNIEXPORT jobject JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniStoreJpeg
        (JNIEnv *env, jobject obj, jbyteArray jpeg, jint jpegSize);

JNIEXPORT jbyteArray JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniCommit
        (JNIEnv *env, jobject obj, jobject handle);

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniReleaseJpeg
        (JNIEnv *env, jobject obj, jobject handle);

JNIEXPORT jint JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniGetWidth
        (JNIEnv *env, jobject obj, jobject handle);

JNIEXPORT jint JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniGetHeight
        (JNIEnv *env, jobject obj, jobject handle);

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniRotate
        (JNIEnv *env, jobject obj, jobject handle, jint degrees);

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniFlipHorizontal
        (JNIEnv *env, jobject obj, jobject handle);

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniFlipVertical
        (JNIEnv *env, jobject obj, jobject handle);

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniCrop
        (JNIEnv *env, jobject obj, jobject handle, jint left, jint top, jint right, jint bottom);
}

class JpegTransformer {
public:
    unsigned char *jpeg;
    unsigned long jpegSize;

    int width;
    int height;

    tjhandle transformHandle;

    JpegTransformer() {
        jpeg = NULL;
        jpegSize = 0;

        width = 0;
        height = 0;

        transformHandle = NULL;
    }
};

JNIEXPORT jobject JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniStoreJpeg
        (JNIEnv *env, jobject obj, jbyteArray jpeg, jint jpegSize) {
    tjhandle tjHandle = tjInitDecompress();
    jbyte *jpegBuffer = env->GetByteArrayElements(jpeg, 0);

    if (!jpegBuffer) return NULL;

    int width, height, jpegSubsamp;
    int status = tjDecompressHeader2(
            tjHandle,
            (unsigned char *) jpegBuffer,
            (unsigned long) jpegSize,
            &width,
            &height,
            &jpegSubsamp
    );

    if (status != 0) {
        return NULL;
    }

    JpegTransformer *jpegTransformer = new JpegTransformer();
    jpegTransformer->width = width;
    jpegTransformer->height = height;
    jpegTransformer->jpeg = (unsigned char *) jpegBuffer;
    jpegTransformer->jpegSize = (unsigned long) jpegSize;

    tjDestroy(tjHandle);

    tjhandle transformHandle = tjInitTransform();
    jpegTransformer->transformHandle = transformHandle;

    return env->NewDirectByteBuffer(jpegTransformer, 0);
}

JNIEXPORT jbyteArray JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniCommit
        (JNIEnv *env, jobject obj, jobject handle) {
    JpegTransformer *jpegTransformer = (JpegTransformer *) env->GetDirectBufferAddress(handle);
    tjhandle tjHandle = jpegTransformer->transformHandle;
    tjDestroy(tjHandle);

    jbyteArray array = env->NewByteArray((jsize) jpegTransformer->jpegSize);
    env->SetByteArrayRegion(array, 0, (jsize) jpegTransformer->jpegSize, reinterpret_cast<jbyte *>(jpegTransformer->jpeg));
    return array;
}

JNIEXPORT jint JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniGetWidth
        (JNIEnv *env, jobject obj, jobject handle) {
    JpegTransformer *jpegTransformer = (JpegTransformer *) env->GetDirectBufferAddress(handle);
    return jpegTransformer->width;
}

JNIEXPORT jint JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniGetHeight
        (JNIEnv *env, jobject obj, jobject handle) {
    JpegTransformer *jpegTransformer = (JpegTransformer *) env->GetDirectBufferAddress(handle);
    return jpegTransformer->height;
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniRotate
        (JNIEnv *env, jobject obj, jobject handle, jint degrees) {
    JpegTransformer *jpegTransformer = (JpegTransformer *) env->GetDirectBufferAddress(handle);
    tjhandle tjHandle = jpegTransformer->transformHandle;

    tjtransform *transform = new tjtransform();
    if (degrees == 90) {
        transform->op = TJXOP_ROT90;
    } else if (degrees == 180) {
        transform->op = TJXOP_ROT180;
    } else if (degrees == 270) {
        transform->op = TJXOP_ROT270;
    }

    tjTransform(tjHandle, jpegTransformer->jpeg, jpegTransformer->jpegSize, 1, &jpegTransformer->jpeg, &jpegTransformer->jpegSize, transform, 0);
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniFlipHorizontal
        (JNIEnv *env, jobject obj, jobject handle) {
    JpegTransformer *jpegTransformer = (JpegTransformer *) env->GetDirectBufferAddress(handle);
    tjhandle tjHandle = jpegTransformer->transformHandle;

    tjtransform *transform = new tjtransform();
    transform->op = TJXOP_HFLIP;

    tjTransform(tjHandle, jpegTransformer->jpeg, jpegTransformer->jpegSize, 1, &jpegTransformer->jpeg, &jpegTransformer->jpegSize, transform, 0);
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniFlipVertical
        (JNIEnv *env, jobject obj, jobject handle) {
    JpegTransformer *jpegTransformer = (JpegTransformer *) env->GetDirectBufferAddress(handle);
    tjhandle tjHandle = jpegTransformer->transformHandle;

    tjtransform *transform = new tjtransform();
    transform->op = TJXOP_VFLIP;

    tjTransform(tjHandle, jpegTransformer->jpeg, jpegTransformer->jpegSize, 1, &jpegTransformer->jpeg, &jpegTransformer->jpegSize, transform, 0);
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniCrop
        (JNIEnv *env, jobject obj, jobject handle, jint left, jint top, jint width, jint height) {
    JpegTransformer *jpegTransformer = (JpegTransformer *) env->GetDirectBufferAddress(handle);
    tjhandle tjHandle = jpegTransformer->transformHandle;

    tjtransform *transform = new tjtransform();
    tjregion cropRegion;
    cropRegion.x = left - (left % 16);
    cropRegion.y = top - (top % 16);
    cropRegion.w = width;
    cropRegion.h = height;

    transform->r = cropRegion;
    transform->options = TJXOPT_CROP;

    tjTransform(tjHandle, jpegTransformer->jpeg, jpegTransformer->jpegSize, 1, &jpegTransformer->jpeg, &jpegTransformer->jpegSize, transform, 0);
}

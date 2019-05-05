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

    bool flipHorizontal;
    bool flipVertical;

    int rotation;

    int cropLeft = 0;
    int cropTop = 0;
    int cropWidth = 0;
    int cropHeight = 0;

    JpegTransformer() {
        jpeg = NULL;
        jpegSize = 0;

        flipHorizontal = false;
        flipVertical = false;

        rotation = 0;

        cropLeft = 0;
        cropTop = 0;
        cropWidth = 0;
        cropHeight = 0;
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
    return env->NewDirectByteBuffer(jpegTransformer, 0);
}

JNIEXPORT jbyteArray JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniCommit
        (JNIEnv *env, jobject obj, jobject handle) {
    JpegTransformer *jpegTransformer = (JpegTransformer *) env->GetDirectBufferAddress(handle);

    unsigned char *jpeg = jpegTransformer->jpeg;
    unsigned long jpegSize = jpegTransformer->jpegSize;

    tjhandle tjHandle = tjInitTransform();

    if (jpegTransformer->flipVertical) {
        tjtransform *transform = new tjtransform();
        transform->op = TJXOP_VFLIP;
        tjTransform(tjHandle, jpeg, jpegSize, 1, &jpeg, &jpegSize, transform, 0);
    }

    if (jpegTransformer->flipHorizontal) {
        tjtransform *transform = new tjtransform();
        transform->op = TJXOP_HFLIP;
        tjTransform(tjHandle, jpeg, jpegSize, 1, &jpeg, &jpegSize, transform, 0);
    }

    tjtransform *transform = new tjtransform();
    if (jpegTransformer->rotation == 90) {
        transform->op = TJXOP_ROT90;
    } else if (jpegTransformer->rotation == 180) {
        transform->op = TJXOP_ROT180;
    } else if (jpegTransformer->rotation == 270) {
        transform->op = TJXOP_ROT270;
    }

    if (jpegTransformer->cropWidth > 0) {
        tjregion cropRegion;
        cropRegion.x = jpegTransformer->cropLeft - (jpegTransformer->cropLeft % 16);
        cropRegion.y = jpegTransformer->cropTop - (jpegTransformer->cropTop % 16);
        cropRegion.w = jpegTransformer->cropWidth;
        cropRegion.h = jpegTransformer->cropHeight;

        transform->r = cropRegion;
        transform->options = TJXOPT_CROP;
    }

    tjTransform(tjHandle, jpeg, jpegSize, 1, &jpeg, &jpegSize, transform, 0);
    tjDestroy(tjHandle);

    jbyteArray array = env->NewByteArray((jsize) jpegSize);
    env->SetByteArrayRegion(array, 0, (jsize) jpegSize, reinterpret_cast<jbyte *>(jpeg));

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
    jpegTransformer->rotation = degrees;
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniFlipHorizontal
        (JNIEnv *env, jobject obj, jobject handle) {
    JpegTransformer *jpegTransformer = (JpegTransformer *) env->GetDirectBufferAddress(handle);
    jpegTransformer->flipHorizontal = true;
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniFlipVertical
        (JNIEnv *env, jobject obj, jobject handle) {
    JpegTransformer *jpegTransformer = (JpegTransformer *) env->GetDirectBufferAddress(handle);
    jpegTransformer->flipVertical = true;
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_JpegTransformer_jniCrop
        (JNIEnv *env, jobject obj, jobject handle, jint left, jint top, jint width, jint height) {
    JpegTransformer *jpegTransformer = (JpegTransformer *) env->GetDirectBufferAddress(handle);
    jpegTransformer->cropLeft = left;
    jpegTransformer->cropTop = top;
    jpegTransformer->cropWidth = width;
    jpegTransformer->cropHeight = height;
}

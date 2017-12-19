#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <android/bitmap.h>
#include <cstring>
#include <unistd.h>
#include "jpge.h"
#include "jpgd.h"

#define  LOG_TAG    "DEBUG"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

extern "C"
{
JNIEXPORT jobject JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniStoreBitmapData(JNIEnv *env, jobject obj,
                                                                jbyteArray bitmap);
JNIEXPORT jbyteArray JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniGetJpegData(JNIEnv *env,
                                                            jobject obj,
                                                            jobject handle,
                                                            jint quality);
JNIEXPORT jobject JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniGetBitmapFromStoredBitmapData(JNIEnv *env,
                                                                              jobject obj,
                                                                              jobject handle);
JNIEXPORT jint JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniGetWidth(JNIEnv *env, jobject obj,
                                                         jobject handle);
JNIEXPORT jint JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniGetHeight(JNIEnv *env, jobject obj,
                                                          jobject handle);
JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniFreeBitmapData(JNIEnv *env, jobject obj,
                                                               jobject handle);
JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniRotateBitmapCcw90(JNIEnv *env, jobject obj,
                                                                  jobject handle);
JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniRotateBitmapCw90(JNIEnv *env, jobject obj,
                                                                 jobject handle);
JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniRotateBitmap180(JNIEnv *env, jobject obj,
                                                                jobject handle);
JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniCropBitmap(JNIEnv *env, jobject obj,
                                                           jobject handle, uint32_t left,
                                                           uint32_t top, uint32_t right,
                                                           uint32_t bottom);
JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniScaleNNBitmap(JNIEnv *env, jobject obj,
                                                              jobject handle, uint32_t newWidth,
                                                              uint32_t newHeight);
JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniScaleBIBitmap(JNIEnv *env, jobject obj,
                                                              jobject handle, uint32_t newWidth,
                                                              uint32_t newHeight);
JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniFlipBitmapHorizontal(JNIEnv *env, jobject obj,
                                                                     jobject handle);
JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniFlipBitmapVertical(JNIEnv *env, jobject obj,
                                                                   jobject handle);
}


class JniBitmap {
public:
    uint32_t *_storedBitmapPixels;
    uint32_t _width;
    uint32_t _height;

    JniBitmap() {
        _storedBitmapPixels = NULL;
    }
};

typedef struct {
    uint8_t alpha, red, green, blue;
} ARGB;

int32_t convertArgbToInt(ARGB argb) {
    return (argb.alpha) | (argb.red << 24) | (argb.green << 16)
           | (argb.blue << 8);
}

void convertIntToArgb(uint32_t pixel, ARGB *argb) {
    argb->red = ((pixel >> 24) & 0xff);
    argb->green = ((pixel >> 16) & 0xff);
    argb->blue = ((pixel >> 8) & 0xff);
    argb->alpha = (pixel & 0xff);
}

JNIEXPORT void JNICALL Java_com_wonderkiln_camerakit_BitmapOperator_jniCropBitmap(
        JNIEnv *env, jobject obj, jobject handle, uint32_t left, uint32_t top,
        uint32_t right, uint32_t bottom) {
    JniBitmap *jniBitmap = (JniBitmap *) env->GetDirectBufferAddress(handle);
    if (jniBitmap->_storedBitmapPixels == NULL)
        return;
    uint32_t *previousData = jniBitmap->_storedBitmapPixels;
    uint32_t oldWidth = jniBitmap->_width;
    uint32_t newWidth = right - left, newHeight = bottom - top;
    uint32_t *newBitmapPixels = new uint32_t[newWidth * newHeight];
    uint32_t *whereToGet = previousData + left + top * oldWidth;
    uint32_t *whereToPut = newBitmapPixels;
    for (int y = top; y < bottom; ++y) {
        memcpy(whereToPut, whereToGet, sizeof(uint32_t) * newWidth);
        whereToGet += oldWidth;
        whereToPut += newWidth;
    }
    //done copying , so replace old data with new one
    delete[] previousData;
    jniBitmap->_storedBitmapPixels = newBitmapPixels;
    jniBitmap->_width = newWidth;
    jniBitmap->_height = newHeight;
}

JNIEXPORT void JNICALL Java_com_wonderkiln_camerakit_BitmapOperator_jniRotateBitmapCcw90(
        JNIEnv *env, jobject obj, jobject handle) {
    JniBitmap *jniBitmap = (JniBitmap *) env->GetDirectBufferAddress(handle);
    if (jniBitmap->_storedBitmapPixels == NULL)
        return;
    uint32_t *previousData = jniBitmap->_storedBitmapPixels;
    uint32_t newWidth = jniBitmap->_height;
    uint32_t newHeight = jniBitmap->_width;
    jniBitmap->_width = newWidth;
    jniBitmap->_height = newHeight;
    uint32_t *newBitmapPixels = new uint32_t[newWidth * newHeight];
    int whereToGet = 0;
    for (int x = 0; x < newWidth; ++x)
        for (int y = newHeight - 1; y >= 0; --y) {
            uint32_t pixel = previousData[whereToGet++];
            newBitmapPixels[newWidth * y + x] = pixel;
        }
    delete[] previousData;
    jniBitmap->_storedBitmapPixels = newBitmapPixels;
}

JNIEXPORT void JNICALL Java_com_wonderkiln_camerakit_BitmapOperator_jniRotateBitmapCw90(
        JNIEnv *env, jobject obj, jobject handle) {
    JniBitmap *jniBitmap = (JniBitmap *) env->GetDirectBufferAddress(handle);
    if (jniBitmap->_storedBitmapPixels == NULL)
        return;
    uint32_t *previousData = jniBitmap->_storedBitmapPixels;
    uint32_t newWidth = jniBitmap->_height;
    uint32_t newHeight = jniBitmap->_width;
    jniBitmap->_width = newWidth;
    jniBitmap->_height = newHeight;
    uint32_t *newBitmapPixels = new uint32_t[newWidth * newHeight];
    int whereToGet = 0;
    jniBitmap->_storedBitmapPixels = newBitmapPixels;
    for (int x = newWidth - 1; x >= 0; --x)
        for (int y = 0; y < newHeight; ++y) {
            uint32_t pixel = previousData[whereToGet++];
            newBitmapPixels[newWidth * y + x] = pixel;
        }
    delete[] previousData;
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniRotateBitmap180(JNIEnv *env, jobject obj,
                                                                jobject handle) {
    JniBitmap *jniBitmap = (JniBitmap *) env->GetDirectBufferAddress(handle);
    if (jniBitmap->_storedBitmapPixels == NULL)
        return;
    uint32_t *pixels = jniBitmap->_storedBitmapPixels;
    uint32_t *pixels2 = jniBitmap->_storedBitmapPixels;
    uint32_t width = jniBitmap->_width;
    uint32_t height = jniBitmap->_height;
    int whereToGet = 0;
    for (int y = height - 1; y >= height / 2; --y)
        for (int x = width - 1; x >= 0; --x) {
            uint32_t tempPixel = pixels2[width * y + x];
            pixels2[width * y + x] = pixels[whereToGet];
            pixels[whereToGet] = tempPixel;
            ++whereToGet;
        }
    if (height % 2 == 1) {
        int y = height / 2;
        whereToGet = width * y;
        int lastXToHandle = width % 2 == 0 ? (width / 2) : (width / 2) - 1;
        for (int x = width - 1; x >= lastXToHandle; --x) {
            uint32_t tempPixel = pixels2[width * y + x];
            pixels2[width * y + x] = pixels[whereToGet];
            pixels[whereToGet] = tempPixel;
            ++whereToGet;
        }
    }
}

JNIEXPORT jint JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniGetWidth(JNIEnv *env, jobject obj,
                                                         jobject handle) {
    JniBitmap *jniBitmap = (JniBitmap *) env->GetDirectBufferAddress(handle);
    if (jniBitmap->_storedBitmapPixels == NULL)
        return -1;
    return jniBitmap->_width;
}

JNIEXPORT jint JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniGetHeight(JNIEnv *env, jobject obj,
                                                          jobject handle) {
    JniBitmap *jniBitmap = (JniBitmap *) env->GetDirectBufferAddress(handle);
    if (jniBitmap->_storedBitmapPixels == NULL)
        return -1;
    return jniBitmap->_height;
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniFreeBitmapData(JNIEnv *env, jobject obj,
                                                               jobject handle) {
    JniBitmap *jniBitmap = (JniBitmap *) env->GetDirectBufferAddress(handle);
    if (jniBitmap->_storedBitmapPixels == NULL)
        return;
    delete[] jniBitmap->_storedBitmapPixels;
    jniBitmap->_storedBitmapPixels = NULL;
    delete jniBitmap;
}

JNIEXPORT jbyteArray JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniGetJpegData(JNIEnv *env,
                                                            jobject obj,
                                                            jobject handle,
                                                            jint quality) {
    JniBitmap *jniBitmap = (JniBitmap *) env->GetDirectBufferAddress(handle);
    if (jniBitmap->_storedBitmapPixels == NULL) {
        return NULL;
    }

    int width = jniBitmap->_width;
    int height = jniBitmap->_height;

    int rgbBufferSize = width * height * 3;
    unsigned char *rgbBuffer = new unsigned char[rgbBufferSize];
    unsigned char *rgbData = new unsigned char[rgbBufferSize];
    unsigned char *rgbTemp = rgbData;
    uint32_t *bitmapTemp = jniBitmap->_storedBitmapPixels;
    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            rgbTemp[0] = ((bitmapTemp[0]) & 0xff);
            rgbTemp[1] = ((bitmapTemp[0] >> 8) & 0xff);
            rgbTemp[2] = ((bitmapTemp[0] >> 16) & 0xff);

            rgbTemp += 3;
            bitmapTemp++;
        }
    }

    int m_quality = quality;
    jpge::params config;
    config.m_quality = m_quality;

    bool success = jpge::compress_image_to_jpeg_file_in_memory(rgbBuffer, rgbBufferSize, width,
                                                               height, 3,
                                                               rgbData, config);
    delete[] rgbData;

    if (success) {
        jbyteArray array = env->NewByteArray(rgbBufferSize);
        env->SetByteArrayRegion(array, 0, rgbBufferSize, reinterpret_cast<jbyte *>(rgbBuffer));

        delete[] rgbBuffer;

        return array;
    }

    return NULL;
}

JNIEXPORT

jobject JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniGetBitmapFromStoredBitmapData(JNIEnv *env,
                                                                              jobject obj,
                                                                              jobject handle) {
    JniBitmap *jniBitmap = (JniBitmap *) env->GetDirectBufferAddress(handle);
    if (jniBitmap->_storedBitmapPixels == NULL) {
        LOGD("no bitmap data was stored. returning null...");
        return NULL;
    }

    jclass bitmapCls = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapFunction = env->GetStaticMethodID(bitmapCls,
                                                            "createBitmap",
                                                            "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jstring configName = env->NewStringUTF("ARGB_8888");
    jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
    jmethodID valueOfBitmapConfigFunction = env->GetStaticMethodID(
            bitmapConfigClass, "valueOf",
            "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;");
    jobject bitmapConfig = env->CallStaticObjectMethod(bitmapConfigClass,
                                                       valueOfBitmapConfigFunction, configName);
    jobject newBitmap = env->CallStaticObjectMethod(bitmapCls,
                                                    createBitmapFunction,
                                                    jniBitmap->_width,
                                                    jniBitmap->_height, bitmapConfig);

    int ret;
    void *bitmapPixels;
    if ((ret = AndroidBitmap_lockPixels(env, newBitmap, &bitmapPixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return NULL;
    }
    uint32_t *newBitmapPixels = (uint32_t *) bitmapPixels;
    int pixelsCount = jniBitmap->_height
                      * jniBitmap->_width;
    memcpy(newBitmapPixels, jniBitmap->_storedBitmapPixels,
           sizeof(uint32_t) * pixelsCount);
    AndroidBitmap_unlockPixels(env, newBitmap);
    return newBitmap;
}

JNIEXPORT jobject JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniStoreBitmapData(JNIEnv *env, jobject obj,
                                                                jbyteArray bitmap) {
    jsize bitmapLength = env->GetArrayLength(bitmap);

    jbyte *bitmapBytes = env->GetByteArrayElements(bitmap, false);
    unsigned char *bitmapChars = (unsigned char *) bitmapBytes;

    static const int requestedComps = 4;
    int actualComps, width, height;

    unsigned char *decodedBytes = jpgd::decompress_jpeg_image_from_memory(
            bitmapChars, bitmapLength, &width, &height, &actualComps, requestedComps);

    uint32_t *pixels = new uint32_t[width * height];
    uint32_t *pixelsTemp = pixels;
    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            pixelsTemp[0] = (uint32_t) decodedBytes[0] |
                            (uint32_t) decodedBytes[1] << 8 |
                            (uint32_t) decodedBytes[2] << 16 |
                            (uint32_t) decodedBytes[3] << 24;

            pixelsTemp++;
            decodedBytes += 4;
        }
    }

    JniBitmap *jniBitmap = new JniBitmap();

    jniBitmap->_storedBitmapPixels = pixels;
    jniBitmap->_width = (uint32_t) width;
    jniBitmap->_height = (uint32_t) height;

    return env->NewDirectByteBuffer(jniBitmap, 0);
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniScaleNNBitmap(JNIEnv *env, jobject obj,
                                                              jobject handle, uint32_t newWidth,
                                                              uint32_t newHeight) {
    JniBitmap *jniBitmap = (JniBitmap *) env->GetDirectBufferAddress(handle);
    if (jniBitmap->_storedBitmapPixels == NULL)
        return;
    uint32_t oldWidth = jniBitmap->_width;
    uint32_t oldHeight = jniBitmap->_height;
    uint32_t *previousData = jniBitmap->_storedBitmapPixels;
    uint32_t *newBitmapPixels = new uint32_t[newWidth * newHeight];
    int x2, y2;
    int whereToPut = 0;
    for (int y = 0; y < newHeight; ++y) {
        for (int x = 0; x < newWidth; ++x) {
            x2 = x * oldWidth / newWidth;
            if (x2 < 0)
                x2 = 0;
            else if (x2 >= oldWidth)
                x2 = oldWidth - 1;
            y2 = y * oldHeight / newHeight;
            if (y2 < 0)
                y2 = 0;
            else if (y2 >= oldHeight)
                y2 = oldHeight - 1;
            newBitmapPixels[whereToPut++] = previousData[(y2 * oldWidth) + x2];
        }
    }

    delete[] previousData;
    jniBitmap->_storedBitmapPixels = newBitmapPixels;
    jniBitmap->_width = newWidth;
    jniBitmap->_height = newHeight;
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniScaleBIBitmap(JNIEnv *env, jobject obj,
                                                              jobject handle, uint32_t newWidth,
                                                              uint32_t newHeight) {

    JniBitmap *jniBitmap = (JniBitmap *) env->GetDirectBufferAddress(handle);
    if (jniBitmap->_storedBitmapPixels == NULL)
        return;
    uint32_t oldWidth = jniBitmap->_width;
    uint32_t oldHeight = jniBitmap->_height;
    uint32_t *previousData = jniBitmap->_storedBitmapPixels;
    uint32_t *newBitmapPixels = new uint32_t[newWidth * newHeight];
    int xTopLeft, yTopLeft;
    int x, y, lastTopLefty;
    float xRatio = (float) newWidth / (float) oldWidth, yratio =
            (float) newHeight / (float) oldHeight;
    float ycRatio2 = 0, ycRatio1 = 0;
    float xt, yt;
    float xcRatio2 = 0, xcratio1 = 0;
    ARGB rgbTopLeft, rgbTopRight, rgbBottomLeft, rgbBottomRight, rgbTopMiddle, rgbBottomMiddle, result;
    for (x = 0; x < newWidth; ++x) {
        xTopLeft = (int) (xt = x / xRatio);
        if (xTopLeft >= oldWidth - 1)
            xTopLeft--;
        if (xt <= xTopLeft + 1) {
            xcratio1 = xt - xTopLeft;
            xcRatio2 = 1 - xcratio1;
        }
        for (y = 0, lastTopLefty = -30000; y < newHeight; ++y) {
            yTopLeft = (int) (yt = y / yratio);
            if (yTopLeft >= oldHeight - 1)
                --yTopLeft;
            if (lastTopLefty == yTopLeft - 1) {
                rgbTopLeft = rgbBottomLeft;
                rgbTopRight = rgbBottomRight;
                rgbTopMiddle = rgbBottomMiddle;
                convertIntToArgb(
                        previousData[((yTopLeft + 1) * oldWidth) + xTopLeft],
                        &rgbBottomLeft);
                convertIntToArgb(
                        previousData[((yTopLeft + 1) * oldWidth)
                                     + (xTopLeft + 1)], &rgbBottomRight);
                rgbBottomMiddle.alpha = rgbBottomLeft.alpha * xcRatio2
                                        + rgbBottomRight.alpha * xcratio1;
                rgbBottomMiddle.red = rgbBottomLeft.red * xcRatio2
                                      + rgbBottomRight.red * xcratio1;
                rgbBottomMiddle.green = rgbBottomLeft.green * xcRatio2
                                        + rgbBottomRight.green * xcratio1;
                rgbBottomMiddle.blue = rgbBottomLeft.blue * xcRatio2
                                       + rgbBottomRight.blue * xcratio1;
            } else if (lastTopLefty != yTopLeft) {
                convertIntToArgb(previousData[(yTopLeft * oldWidth) + xTopLeft],
                                 &rgbTopLeft);
                convertIntToArgb(
                        previousData[((yTopLeft + 1) * oldWidth) + xTopLeft],
                        &rgbTopRight);
                rgbTopMiddle.alpha = rgbTopLeft.alpha * xcRatio2
                                     + rgbTopRight.alpha * xcratio1;
                rgbTopMiddle.red = rgbTopLeft.red * xcRatio2
                                   + rgbTopRight.red * xcratio1;
                rgbTopMiddle.green = rgbTopLeft.green * xcRatio2
                                     + rgbTopRight.green * xcratio1;
                rgbTopMiddle.blue = rgbTopLeft.blue * xcRatio2
                                    + rgbTopRight.blue * xcratio1;
                convertIntToArgb(
                        previousData[((yTopLeft + 1) * oldWidth) + xTopLeft],
                        &rgbBottomLeft);
                convertIntToArgb(
                        previousData[((yTopLeft + 1) * oldWidth)
                                     + (xTopLeft + 1)], &rgbBottomRight);
                rgbBottomMiddle.alpha = rgbBottomLeft.alpha * xcRatio2
                                        + rgbBottomRight.alpha * xcratio1;
                rgbBottomMiddle.red = rgbBottomLeft.red * xcRatio2
                                      + rgbBottomRight.red * xcratio1;
                rgbBottomMiddle.green = rgbBottomLeft.green * xcRatio2
                                        + rgbBottomRight.green * xcratio1;
                rgbBottomMiddle.blue = rgbBottomLeft.blue * xcRatio2
                                       + rgbBottomRight.blue * xcratio1;
            }
            lastTopLefty = yTopLeft;
            if (yt <= yTopLeft + 1) {
                ycRatio1 = yt - yTopLeft;
                ycRatio2 = 1 - ycRatio1;
            }
            result.alpha = rgbTopMiddle.alpha * ycRatio2
                           + rgbBottomMiddle.alpha * ycRatio1;
            result.blue = rgbTopMiddle.blue * ycRatio2
                          + rgbBottomMiddle.blue * ycRatio1;
            result.red = rgbTopMiddle.red * ycRatio2
                         + rgbBottomMiddle.red * ycRatio1;
            result.green = rgbTopMiddle.green * ycRatio2
                           + rgbBottomMiddle.green * ycRatio1;
            newBitmapPixels[(y * newWidth) + x] = convertArgbToInt(result);
        }
    }
    delete[] previousData;
    jniBitmap->_storedBitmapPixels = newBitmapPixels;
    jniBitmap->_width = newWidth;
    jniBitmap->_height = newHeight;
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniFlipBitmapHorizontal(JNIEnv *env, jobject obj,
                                                                     jobject handle) {
    JniBitmap *jniBitmap = (JniBitmap *) env->GetDirectBufferAddress(handle);
    if (jniBitmap->_storedBitmapPixels == NULL)
        return;
    uint32_t *previousData = jniBitmap->_storedBitmapPixels;
    int width = jniBitmap->_width, middle = width / 2, height =
            jniBitmap->_height;
    for (int y = 0; y < height; ++y) {
        uint32_t *idx1 = previousData + width * y;
        uint32_t *idx2 = previousData + width * (y + 1) - 1;
        for (int x = 0; x < middle; ++x) {
            uint32_t pixel = *idx1;
            *idx1 = *idx2;
            *idx2 = pixel;
            ++idx1;
            --idx2;
        }
    }
}

JNIEXPORT void JNICALL
Java_com_wonderkiln_camerakit_BitmapOperator_jniFlipBitmapVertical(JNIEnv *env, jobject obj,
                                                                   jobject handle) {
    JniBitmap *jniBitmap = (JniBitmap *) env->GetDirectBufferAddress(handle);
    if (jniBitmap->_storedBitmapPixels == NULL)
        return;
    uint32_t *previousData = jniBitmap->_storedBitmapPixels;
    int width = jniBitmap->_width, height =
            jniBitmap->_height, middle = height / 2;
    for (int y = 0; y < middle; ++y) {
        uint32_t *idx1 = previousData + width * y;
        uint32_t *idx2 = previousData + width * (height - y - 1);
        for (int x = 0; x < width; ++x) {
            uint32_t pixel = *idx1;
            *idx1 = *idx2;
            *idx2 = pixel;
            ++idx2;
            ++idx1;
        }
    }
}
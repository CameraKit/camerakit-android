package com.flurgle.camerakit.encoding;

import android.content.Context;
import android.graphics.ImageFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

public class VideoEncoder extends Encoder {

    File mVideoFile;
    FileOutputStream mFOS;

    MediaCodec mMediaCodec;
    ByteBuffer[] inputBuffers;
    ByteBuffer[] outputBuffers;
    MediaFormat mediaFormat = null;
    public static int frameID = 0;

    public VideoEncoder(Context context,  int cameraFacing, int width, int height) throws IOException {
        super(cameraFacing, width, height);

        try {
            mVideoFile = new File(context.getExternalFilesDir(null), "video.mp4");
            mFOS = new FileOutputStream(mVideoFile, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        queue = new ArrayBlockingQueue<>(100);

        mMediaCodec = MediaCodec.createEncoderByType("video/avc");
        mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 8000);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);

        try {
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);

            mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            frameID = 0;

            rotatedFrameData = new byte[width * height * (ImageFormat.getBitsPerPixel(ImageFormat.YV12)) / 8];
            planeManagedData = new byte[width * height * (ImageFormat.getBitsPerPixel(ImageFormat.YV12)) / 8];

            encoderStarted = true;
            mMediaCodec.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void stopEncoder() {
        encoderStarted = false;
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }

        if (mFOS != null) {
            try {
                mFOS.close();
            } catch (IOException e) {

            }
        }
    }


    @Override
    public void encode(byte[] rawData) {
        inputBuffers = mMediaCodec.getInputBuffers();
        outputBuffers = mMediaCodec.getOutputBuffers();

        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(0);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();

            int size = inputBuffer.limit();
            inputBuffer.put(rawData);

            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, size, (System.currentTimeMillis() - startMS) * 1000, 0);
        } else {
            return;
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        do {
            if (outputBufferIndex >= 0) {
                Frame frame = new Frame(frameID);
                ByteBuffer outBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
                int dataLength = 0;

                outBuffer.get(outData);

                dataLength = outData.length - 2;
                frame.frameData = new byte[dataLength];
                // skipping 0x00 0x80 while copying
                System.arraycopy(outData, 2, frame.frameData, 0, dataLength);

                try {
                    if (bufferInfo.offset != 0) {
                        mFOS.write(outData, bufferInfo.offset, outData.length - bufferInfo.offset);
                    } else {
                        mFOS.write(outData, 0, outData.length);
                    }
                    mFOS.flush();
                } catch (IOException e) {
                    Log.e("Encoding", e.toString());
                }

                try {
                    queue.put(frame);
                } catch (InterruptedException e) {
                    Log.e("EncodeDecode", "interrupted while waiting");
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.e("EncodeDecode", "frame is null");
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    Log.e("EncodeDecode", "problem inserting in the queue");
                    e.printStackTrace();
                }
                Log.d("EncodeDecode", "H263 frame enqueued. queue size now: " + queue.size());

                frameID++;
                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);

            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mMediaCodec.getOutputBuffers();
                Log.e("EncodeDecode", "output buffer of encoder : info changed");
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.e("EncodeDecode", "output buffer of encoder : format changed");
            } else {
                Log.e("EncodeDecode", "unknown value of outputBufferIndex : " + outputBufferIndex);
            }
        } while (outputBufferIndex >= 0);
    }


}

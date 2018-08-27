#pragma once

#include "CameraSurfaceTexture.hpp"

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <android/log.h>

#include <stdlib.h>
#include <memory.h>

#define  LOG_TAG "CameraSurfaceView"
#define  LOG_DEBUG(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOG_ERROR(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

namespace camerakit {

  class CameraSurfaceView {

    public:
      CameraSurfaceView();
      virtual ~CameraSurfaceView();

    public:
      void onSurfaceCreated();
      void onSurfaceChanged(int width, int height);
      void onDrawFrame();
      void drawTexture(GLuint texture, int textureWidth, int textureHeight);
      void abandon();

    private:
      int surfaceWidth;
      int surfaceHeight;

      GLuint vertexBuffer;
      GLuint program;
      GLint aPosition;
      GLint aTexCoord;

    private:
      static const GLfloat* VertexData();
      static const GLushort* VertexIndices();

      static const char* VertexShaderCode();
      static const char* FragmentShaderCode();

      static GLuint LoadShader(GLenum shaderType, const char* shaderCode);
      static GLuint CreateProgram(const char* vertexShaderCode, const char* fragmentShaderCode);

  };

}

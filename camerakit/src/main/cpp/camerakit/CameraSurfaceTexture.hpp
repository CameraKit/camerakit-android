#pragma once

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <android/log.h>

#include <stdlib.h>

#define  LOG_TAG "CameraSurfaceTexture"
#define  LOG_DEBUG(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOG_ERROR(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

namespace camerakit {

  class CameraSurfaceTexture {

    public:
      CameraSurfaceTexture(GLuint inputTexture, GLuint outputTexture);
      virtual ~CameraSurfaceTexture();

    public:
      void setSize(int width, int height);
      void updateTexImage(float* transformMatrix, float* rotationMatrix);

    private:
      int width;
      int height;

      GLuint inputTexture;
      GLuint outputTexture;
      GLuint framebuffer;
      GLuint vertexBuffer;

      GLuint program;
      GLint aPosition;
      GLint aTexCoord;
      GLint uTransformMatrix;
      GLint uRotationMatrix;

    private:
      static const GLfloat* VertexData();
      static const GLushort* VertexIndices();

      static const char* VertexShaderCode();
      static const char* FragmentShaderCode();

      static GLuint LoadShader(GLenum shaderType, const char* shaderCode);
      static GLuint CreateProgram(const char* vertexShaderCode, const char* fragmentShaderCode);

  };

}

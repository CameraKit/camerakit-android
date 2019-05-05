#include "CameraSurfaceTexture.hpp"

namespace camerakit {

  CameraSurfaceTexture::CameraSurfaceTexture(GLuint inputTexture, GLuint outputTexture)
      : width(0),
        height(0) {

      this->inputTexture = inputTexture;
      this->outputTexture = outputTexture;

      glBindTexture(GL_TEXTURE_EXTERNAL_OES, inputTexture);
      glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
      glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
      glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
      glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

      glGenBuffers(1, &vertexBuffer);
      glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
      glBufferData(GL_ARRAY_BUFFER, 24 * sizeof(GLfloat), VertexData(), GL_STATIC_DRAW);

      GLuint program = CreateProgram(VertexShaderCode(), FragmentShaderCode());
      if (!program) {
          // TODO: throw here
          return;
      }

      glUseProgram(program);

      GLint aPosition = glGetAttribLocation(program, "aPosition");
      GLint aTexCoord = glGetAttribLocation(program, "aTexCoord");
      GLint uTransformMatrix = glGetUniformLocation(program, "uTransformMatrix");
      GLint uRotationMatrix = glGetUniformLocation(program, "uRotationMatrix");

      glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

      if (glGetError() != GL_NO_ERROR) {
          glDeleteProgram(program);

          // TODO: throw here
          return;
      }

      this->program = program;
      this->aPosition = aPosition;
      this->aTexCoord = aTexCoord;
      this->uTransformMatrix = uTransformMatrix;
      this->uRotationMatrix = uRotationMatrix;
  }

  CameraSurfaceTexture::~CameraSurfaceTexture() {
      if (vertexBuffer != 0) {
          glDeleteBuffers(1, &vertexBuffer);
          vertexBuffer = 0;
      }
  }

  void CameraSurfaceTexture::setSize(int width, int height) {
      this->width = width;
      this->height = height;

      if (glIsFramebuffer(framebuffer)) {
          glDeleteFramebuffers(1, &framebuffer);
      }

      glGenFramebuffers(1, &framebuffer);
      glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
      glBindTexture(GL_TEXTURE_2D, outputTexture);

      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);

      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

      glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, outputTexture, 0);
      glBindFramebuffer(GL_FRAMEBUFFER, 0);
  }

  void CameraSurfaceTexture::updateTexImage(float* transformMatrix, float* rotationMatrix) {
      glViewport(0, 0, width, height);

      glBindTexture(GL_TEXTURE_2D, 0);
      glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

      glDisable(GL_BLEND);
      glBindTexture(GL_TEXTURE_EXTERNAL_OES, inputTexture);

      glUseProgram(program);
      glUniformMatrix4fv(uTransformMatrix, 1, GL_FALSE, transformMatrix);
      glUniformMatrix4fv(uRotationMatrix, 1, GL_FALSE, rotationMatrix);
      glVertexAttribPointer(aPosition, 4, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (const GLvoid*) (0 * sizeof(GLfloat)));
      glEnableVertexAttribArray(aPosition);
      glVertexAttribPointer(aTexCoord, 2, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (const GLvoid*) (4 * sizeof(GLfloat)));
      glEnableVertexAttribArray(aTexCoord);
      glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
      glDrawElements(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_SHORT, VertexIndices());
  }

  const GLfloat* CameraSurfaceTexture::VertexData() {
      static const GLfloat vertexData[] = {
          -1.0f, -1.0f, 0.0, 1.0, 0.0f, 0.0f,
          +1.0f, -1.0f, 0.0, 1.0, 1.0f, 0.0f,
          -1.0f, +1.0f, 0.0, 1.0, 0.0f, 1.0f,
          +1.0f, +1.0f, 0.0, 1.0, 1.0f, 1.0f,
      };

      return vertexData;
  }

  const GLushort* CameraSurfaceTexture::VertexIndices() {
      static const GLushort vertexIndices[] = {
          0, 1, 2, 3
      };

      return vertexIndices;
  }

  const char* CameraSurfaceTexture::VertexShaderCode() {
      static const char vertexShader[] =
          "uniform mat4 uTransformMatrix;\n"
          "uniform mat4 uRotationMatrix;\n"
          "attribute vec4 aPosition;\n"
          "attribute vec4 aTexCoord;\n"
          "varying vec2 vTexCoord;\n"
          "void main() {\n"
          "    gl_Position = uRotationMatrix * aPosition;\n"
          "    vTexCoord = (uTransformMatrix * aTexCoord).xy;\n"
          "}\n";

      return vertexShader;
  }

  const char* CameraSurfaceTexture::FragmentShaderCode() {
      static const char fragmentShader[] =
          "#extension GL_OES_EGL_image_external:require\n"
          "precision mediump float;\n"
          "uniform samplerExternalOES uTexture;\n"
          "varying vec2 vTexCoord;\n"
          "void main() {\n"
          "    gl_FragColor = texture2D(uTexture, vTexCoord);\n"
          "}\n";

      return fragmentShader;
  }

  GLuint CameraSurfaceTexture::LoadShader(GLenum shaderType, const char* shaderCode) {
      GLuint shader = glCreateShader(shaderType);
      if (shader) {
          glShaderSource(shader, 1, &shaderCode, NULL);
          glCompileShader(shader);
          GLint compileStatus = GL_FALSE;
          glGetShaderiv(shader, GL_COMPILE_STATUS, &compileStatus);
          if (!compileStatus) {
              GLint infoLength = 0;
              glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLength);
              if (infoLength) {
                  char* infoBuffer = (char*) malloc((size_t) infoLength);
                  if (infoBuffer) {
                      glGetShaderInfoLog(shader, infoLength, NULL, infoBuffer);
                      // todo: output log
                      free(infoBuffer);
                  }
              }
              glDeleteShader(shader);
              shader = 0;
          }
      }
      return shader;
  }

  GLuint CameraSurfaceTexture::CreateProgram(const char* vertexShaderCode,
                                             const char* fragmentShaderCode) {
      GLuint vertexShader = LoadShader(GL_VERTEX_SHADER, vertexShaderCode);
      if (!vertexShader) {
          return 0;
      }

      GLuint fragmentShader = LoadShader(GL_FRAGMENT_SHADER, fragmentShaderCode);
      if (!fragmentShader) {
          return 0;
      }

      GLuint program = glCreateProgram();
      if (program) {
          glAttachShader(program, vertexShader);
          // TODO: check error and throw if needed

          glAttachShader(program, fragmentShader);
          // TODO: check error and throw if needed

          glLinkProgram(program);
          GLint linkStatus = GL_FALSE;
          glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
          if (!linkStatus) {
              GLint infoLength = 0;
              glGetProgramiv(program, GL_INFO_LOG_LENGTH, &infoLength);
              if (infoLength) {
                  char* infoBuffer = (char*) malloc((size_t) infoLength);
                  if (infoBuffer) {
                      glGetProgramInfoLog(program, infoLength, NULL, infoBuffer);
                      // todo: output log
                      free(infoBuffer);
                  }
              }
              glDeleteProgram(program);
              program = 0;
          }
      }
      return program;
  }

}

#include "CameraSurfaceView.hpp"

namespace camerakit {

  CameraSurfaceView::CameraSurfaceView()
          : surfaceWidth(0),
            surfaceHeight(0) {
  }

  CameraSurfaceView::~CameraSurfaceView() {
      if (vertexBuffer != 0) {
          glDeleteBuffers(1, &vertexBuffer);
          vertexBuffer = 0;
      }
  }

  void CameraSurfaceView::onSurfaceCreated() {
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

      glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

      if (glGetError() != GL_NO_ERROR) {
          glDeleteProgram(program);

          // TODO: throw here
          return;
      }

      this->program = program;
      this->aPosition = aPosition;
      this->aTexCoord = aTexCoord;
  }

  void CameraSurfaceView::onSurfaceChanged(int width, int height) {
      this->surfaceWidth = width;
      this->surfaceHeight = height;
  }

  void CameraSurfaceView::onDrawFrame() {
      glBindFramebuffer(GL_FRAMEBUFFER, 0);

      glClearColor(0.0, 0.0, 0.0, 1.0);
      glClear(GL_COLOR_BUFFER_BIT);
  }

  void CameraSurfaceView::drawTexture(GLuint texture, int textureWidth, int textureHeight) {
      glBindFramebuffer(GL_FRAMEBUFFER, 0);
      glBindTexture(GL_TEXTURE_2D, texture);

      int viewportX = 0;
      int viewportY = 0;
      int viewportWidth = surfaceWidth;
      int viewportHeight = surfaceHeight;

      int candidateWidth = (int) (((float) textureWidth / (float) textureHeight) * surfaceHeight);
      int candidateHeight = (int) (((float) textureHeight / (float) textureWidth) * surfaceWidth);

      if (candidateWidth > surfaceWidth) {
          viewportX = -1 * (candidateWidth - surfaceWidth) / 2;
          viewportWidth = candidateWidth;
      } else if (candidateHeight > surfaceHeight) {
          viewportY = -1 * (candidateHeight - surfaceHeight) / 2;
          viewportHeight = candidateHeight;
      }

      glViewport(viewportX, viewportY, viewportWidth, viewportHeight);

      glUseProgram(program);
      glVertexAttribPointer(aPosition, 4, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (const GLvoid*) (0 * sizeof(GLfloat)));
      glEnableVertexAttribArray(aPosition);
      glVertexAttribPointer(aTexCoord, 2, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (const GLvoid*) (4 * sizeof(GLfloat)));
      glEnableVertexAttribArray(aTexCoord);
      glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
      glDrawElements(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_SHORT, VertexIndices());
      glFlush();
  }

  void CameraSurfaceView::abandon() {
  }

  const GLfloat* CameraSurfaceView::VertexData() {
      static const GLfloat vertexData[] = {
              -1.0f, -1.0f, 0.0, 1.0, 0.0f, 0.0f,
              +1.0f, -1.0f, 0.0, 1.0, 1.0f, 0.0f,
              -1.0f, +1.0f, 0.0, 1.0, 0.0f, 1.0f,
              +1.0f, +1.0f, 0.0, 1.0, 1.0f, 1.0f,
      };

      return vertexData;
  }

  const GLushort* CameraSurfaceView::VertexIndices() {
      static const GLushort vertexIndices[] = {
              0, 1, 2, 3
      };

      return vertexIndices;
  }

  const char* CameraSurfaceView::VertexShaderCode() {
      static const char vertexShader[] =
              "attribute vec4 aPosition;\n"
              "attribute vec4 aTexCoord;\n"
              "varying vec2 vTexCoord;\n"
              "void main() {\n"
              "    gl_Position = aPosition;\n"
              "    vTexCoord = aTexCoord.xy;\n"
              "}\n";

      return vertexShader;
  }

  const char* CameraSurfaceView::FragmentShaderCode() {
      static const char fragmentShader[] =
              "precision mediump float;\n"
              "uniform sampler2D uTexture;\n"
              "varying vec2 vTexCoord;\n"
              "void main() {\n"
              "    vec4 color = texture2D(uTexture, vTexCoord);\n"
              "    gl_FragColor = color;\n"
              "}\n";

      return fragmentShader;
  }

  GLuint CameraSurfaceView::LoadShader(GLenum shaderType, const char* shaderCode) {
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

  GLuint CameraSurfaceView::CreateProgram(const char* vertexShaderCode, const char* fragmentShaderCode) {
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

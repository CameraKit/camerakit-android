cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_CXX_STANDARD 14)

add_library(camerakit-core SHARED
        camerakit/CameraSurfaceTexture.cpp
        camerakit/CameraSurfaceTexture.hpp
        camerakit/CameraSurfaceView.cpp
        camerakit/CameraSurfaceView.hpp)

target_link_libraries(camerakit-core
        PUBLIC -llog
        PUBLIC -lGLESv2)

add_library(camerakit SHARED
        jni_camera_surface_texture.cpp
        jni_camera_surface_view.cpp
        main.cpp)

target_link_libraries(camerakit
        PUBLIC camerakit-core)

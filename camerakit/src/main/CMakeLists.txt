cmake_minimum_required(VERSION 3.4.1)

set(JPEG_INC_DIR ${CMAKE_SOURCE_DIR}/cpp/libjpeg/include)
set(JPEG_BUILD_DIR ${CMAKE_SOURCE_DIR}/jniLibs)

include_directories(${JPEG_INC_DIR})

add_library(libjpeg STATIC IMPORTED)
set_target_properties(libjpeg PROPERTIES IMPORTED_LOCATION ${JPEG_BUILD_DIR}/${ANDROID_ABI}/libjpeg-turbo.a)

add_library(jpegTransformer SHARED ${CMAKE_SOURCE_DIR}/cpp/JniJpegTransformer.cpp)
add_library(yuvOperator SHARED ${CMAKE_SOURCE_DIR}/cpp/JniYuvOperator.cpp)

find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)

target_link_libraries(jpegTransformer yuvOperator libjpeg ${log-lib} ${jnigraphics-lib})

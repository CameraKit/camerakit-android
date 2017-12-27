cmake_minimum_required(VERSION 3.4.1)
add_library(JniBitmapOperator SHARED JniBitmapOperator.cpp)
add_library(JniYuvOperator SHARED JniYuvOperator.cpp)
add_library(jpgd SHARED jpgd.cpp)
add_library(jpge SHARED jpge.cpp)

find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)

target_link_libraries(JniBitmapOperator JniYuvOperator jpgd jpge ${log-lib} ${jnigraphics-lib})

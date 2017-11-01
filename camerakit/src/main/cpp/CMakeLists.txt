cmake_minimum_required(VERSION 3.4.1)
add_library(JniBitmapOperator SHARED JniBitmapOperator.cpp)
add_library(JniYuvOperator SHARED JniYuvOperator.cpp)

find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)

target_link_libraries(JniBitmapOperator JniYuvOperator ${log-lib} ${jnigraphics-lib})

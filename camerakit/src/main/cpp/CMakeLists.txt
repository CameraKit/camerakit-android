cmake_minimum_required(VERSION 3.4.1)
add_library(JniBitmapOperations SHARED JniBitmapOperations.cpp)

find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)

target_link_libraries(JniBitmapOperations ${log-lib} ${jnigraphics-lib})

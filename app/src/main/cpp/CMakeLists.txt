cmake_minimum_required(VERSION 3.22.1)

project("gyruntime")

add_library(gyruntime SHARED runtime.c)

find_library(log-lib log)
find_library(dl-lib dl)

target_link_libraries(gyruntime ${log-lib} ${dl-lib})

# cmake_minimum_required(VERSION 3.0)
# project(src VERSION 1.0 LANGUAGES CXX)

message("############### BEGIN SHADERS LOOP : " ${IN_SHADERS})
foreach(SHADER ${IN_SHADERS})
    message("#################### SHADER : " ${SHADER})
    compile_shader(${SHADER})
endforeach()

# set(SOURCE main.cpp)
file(GLOB_RECURSE SOURCE *.cpp *.h)
# message("#####################out shaders : " ${OUT_SHADERS})
message(${PROJECT_NAME})
# set(IN_SHADERS_FULL_PATH )
# foreach(IN_SHADER ${IN_SHADERS})
#     list(APPEND IN_SHADERS_FULL_PATH ${SHADER_SRC_DIR}/${IN_SHADER} ${OUT_SHADERS})
# endforeach()

add_executable(${PROJECT_NAME} ${SOURCE} ${OUT_SHADERS})

set(CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR})

find_package(bgfx REQUIRED)
target_include_directories(${PROJECT_NAME} PUBLIC ${BGFX_INCLUDE_DIRS})
target_compile_definitions(${PROJECT_NAME} PUBLIC __STDC_LIMIT_MACROS __STDC_FORMAT_MACROS __STDC_CONSTANT_MACROS)
if (MSVC)
    target_compile_options(${PROJECT_NAME} PUBLIC /Wall)
endif()


# find_package(OpenGL REQUIRED)

target_link_libraries(${PROJECT_NAME} PUBLIC 
    $<$<CONFIG:Debug>:${BGFX_LIBRARY_DEBUG}>
    $<$<CONFIG:Release>:${BGFX_LIBRARY_RELEASE}>
    $<$<CONFIG:Debug>:${BX_LIBRARY_DEBUG}>
    $<$<CONFIG:Release>:${BX_LIBRARY_RELEASE}>
    $<$<CONFIG:Debug>:${BIMG_LIBRARY_DEBUG}>
    $<$<CONFIG:Release>:${BIMG_LIBRARY_RELEASE}>
    $<$<CONFIG:Debug>:${ASTCCODEC_LIBRARY_DEBUG}>
    $<$<CONFIG:Release>:${ASTCCODEC_LIBRARY_RELEASE}>
    glfw
    # OpenGL::GL
)


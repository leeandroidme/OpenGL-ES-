LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
#LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
LOCAL_MODULE    := opengl-es
LOCAL_SRC_FILES := com_example_opengl_c_es_MyGLView.cpp
LOCAL_LDLIBS:=-L$(SYSROOT)/usr/lib -llog -lGLESv1_CM
include $(BUILD_SHARED_LIBRARY)
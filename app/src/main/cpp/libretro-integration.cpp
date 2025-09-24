#include <jni.h>
#include <string>
#include <libretro.h>
#include <dlfcn.h>
#include <android/log.h>

#define LOG_TAG "LibretroCore"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Type definitions
typedef void *dylib_t;
typedef void (*function_t)(void);

// Simple dylib implementations for Android
dylib_t dylib_load(const char *path) {
    return dlopen(path, RTLD_LAZY);
}

void dylib_close(dylib_t lib) {
    if (lib) dlclose(lib);
}

function_t dylib_proc(dylib_t lib, const char *proc) {
    return (function_t)dlsym(lib, proc);
}

// Declare libretro core function pointers
typedef void (*retro_init_t)(void);
typedef void (*retro_deinit_t)(void);
typedef unsigned (*retro_api_version_t)(void);
typedef void (*retro_get_system_info_t)(struct retro_system_info *info);
typedef void (*retro_get_system_av_info_t)(struct retro_system_av_info *info);
typedef void (*retro_set_environment_t)(retro_environment_t cb);
typedef void (*retro_set_video_refresh_t)(retro_video_refresh_t cb);
typedef void (*retro_set_audio_sample_t)(retro_audio_sample_t cb);
typedef void (*retro_set_audio_sample_batch_t)(retro_audio_sample_batch_t cb);
typedef void (*retro_set_input_poll_t)(retro_input_poll_t cb);
typedef void (*retro_set_input_state_t)(retro_input_state_t cb);
typedef void (*retro_set_controller_port_device_t)(unsigned port, unsigned device);
typedef void (*retro_reset_t)(void);
typedef void (*retro_run_t)(void);
typedef size_t (*retro_serialize_size_t)(void);
typedef bool (*retro_serialize_t)(void *data, size_t size);
typedef bool (*retro_unserialize_t)(const void *data, size_t size);
typedef void (*retro_cheat_reset_t)(void);
typedef void (*retro_cheat_set_t)(unsigned index, bool enabled, const char *code);
typedef bool (*retro_load_game_t)(const struct retro_game_info *game);
typedef bool (*retro_load_game_special_t)(unsigned game_type, const struct retro_game_info *info, size_t num_info);
typedef void (*retro_unload_game_t)(void);
typedef unsigned (*retro_get_region_t)(void);
typedef void* (*retro_get_memory_data_t)(unsigned id);
typedef size_t (*retro_get_memory_size_t)(unsigned id);

// Function pointers that will be resolved at runtime
static retro_init_t core_retro_init = NULL;
static retro_deinit_t core_retro_deinit = NULL;
static retro_api_version_t core_retro_api_version = NULL;
static retro_get_system_info_t core_retro_get_system_info = NULL;
static retro_get_system_av_info_t core_retro_get_system_av_info = NULL;
static retro_set_environment_t core_retro_set_environment = NULL;
static retro_set_video_refresh_t core_retro_set_video_refresh = NULL;
static retro_set_audio_sample_t core_retro_set_audio_sample = NULL;
static retro_set_audio_sample_batch_t core_retro_set_audio_sample_batch = NULL;
static retro_set_input_poll_t core_retro_set_input_poll = NULL;
static retro_set_input_state_t core_retro_set_input_state = NULL;
static retro_set_controller_port_device_t core_retro_set_controller_port_device = NULL;
static retro_reset_t core_retro_reset = NULL;
static retro_run_t core_retro_run = NULL;
static retro_serialize_size_t core_retro_serialize_size = NULL;
static retro_serialize_t core_retro_serialize = NULL;
static retro_unserialize_t core_retro_unserialize = NULL;
static retro_cheat_reset_t core_retro_cheat_reset = NULL;
static retro_cheat_set_t core_retro_cheat_set = NULL;
static retro_load_game_t core_retro_load_game = NULL;
static retro_load_game_special_t core_retro_load_game_special = NULL;
static retro_unload_game_t core_retro_unload_game = NULL;
static retro_get_region_t core_retro_get_region = NULL;
static retro_get_memory_data_t core_retro_get_memory_data = NULL;
static retro_get_memory_size_t core_retro_get_memory_size = NULL;

// Global variables for core management
static dylib_t core_handle = NULL;
static struct retro_system_info system_info;
static struct retro_game_info game_info;
static bool core_loaded = false;

// Static logging function for libretro
static void libretro_log(enum retro_log_level level, const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
    __android_log_vprint(ANDROID_LOG_INFO, LOG_TAG, fmt, args);
    va_end(args);
}

// Environment callback implementation
static bool environment_callback(unsigned cmd, void* data) {
    switch (cmd) {
        case RETRO_ENVIRONMENT_GET_SYSTEM_DIRECTORY:
            // Return system directory path
            *(const char**)data = "/data/data/com.reznor.emulation/files/system";
            return true;

        case RETRO_ENVIRONMENT_GET_SAVE_DIRECTORY:
            // Return save directory path
            *(const char**)data = "/data/data/com.reznor.emulation/files/saves";
            return true;

        case RETRO_ENVIRONMENT_GET_LOG_INTERFACE: {
            struct retro_log_callback* cb = (struct retro_log_callback*)data;
            cb->log = libretro_log;
            return true;
        }

        default:
            return false;
    }
}

// Global reference to Java object for callbacks
static JavaVM* javaVM = NULL;
static jobject libretroCoreObject = NULL;
static jmethodID videoFrameMethod = NULL;
static jmethodID inputPollMethod = NULL;
static jmethodID inputStateMethod = NULL;

// Video refresh callback
static void video_refresh_callback(const void* data, unsigned width, unsigned height, size_t pitch) {
    if (!data || !javaVM || !libretroCoreObject || !videoFrameMethod) return;

    JNIEnv* env;
    bool attached = false;

    if (javaVM->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        if (javaVM->AttachCurrentThread(&env, NULL) != JNI_OK) {
            LOGE("Failed to attach thread");
            return;
        }
        attached = true;
    }

    // Create ByteBuffer from video data
    jobject byteBuffer = env->NewDirectByteBuffer((void*)data, height * pitch);
    if (byteBuffer) {
        env->CallVoidMethod(libretroCoreObject, videoFrameMethod,
                           byteBuffer, (jint)width, (jint)height, (jint)pitch);
    }

    if (attached) {
        javaVM->DetachCurrentThread();
    }
}

// Audio sample callback
static void audio_sample_callback(int16_t left, int16_t right) {
    // Handle audio sample
}

// Audio batch callback
static size_t audio_sample_batch_callback(const int16_t* data, size_t frames) {
    // Handle audio batch
    return frames;
}

// Input poll callback
static void input_poll_callback(void) {
    if (!javaVM || !libretroCoreObject || !inputPollMethod) return;

    JNIEnv* env;
    bool attached = false;

    if (javaVM->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        if (javaVM->AttachCurrentThread(&env, NULL) != JNI_OK) {
            return;
        }
        attached = true;
    }

    env->CallVoidMethod(libretroCoreObject, inputPollMethod);

    if (attached) {
        javaVM->DetachCurrentThread();
    }
}

// Input state callback
static int16_t input_state_callback(unsigned port, unsigned device, unsigned index, unsigned id) {
    if (!javaVM || !libretroCoreObject || !inputStateMethod) return 0;

    JNIEnv* env;
    bool attached = false;

    if (javaVM->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        if (javaVM->AttachCurrentThread(&env, NULL) != JNI_OK) {
            return 0;
        }
        attached = true;
    }

    jint result = env->CallIntMethod(libretroCoreObject, inputStateMethod, (jint)port, (jint)device, (jint)index, (jint)id);

    if (attached) {
        javaVM->DetachCurrentThread();
    }

    return (int16_t)result;
}

extern "C" {

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    javaVM = vm;
    return JNI_VERSION_1_6;
}

JNIEXPORT jboolean JNICALL
Java_com_reznor_emulation_libretro_LibretroCore_loadCore(JNIEnv *env, jobject thiz, jstring core_path) {
    // Store reference to Java object
    libretroCoreObject = env->NewGlobalRef(thiz);

    // Get method IDs for callbacks
    jclass clazz = env->GetObjectClass(thiz);
    videoFrameMethod = env->GetMethodID(clazz, "onVideoFrame", "(Ljava/nio/ByteBuffer;III)V");
    inputPollMethod = env->GetMethodID(clazz, "onInputPoll", "()V");
    inputStateMethod = env->GetMethodID(clazz, "onInputState", "(IIII)I");

    const char *corePath = env->GetStringUTFChars(core_path, nullptr);

    LOGI("Loading core: %s", corePath);

    // Load the dynamic library
    core_handle = dylib_load(corePath);
    if (!core_handle) {
        LOGE("Failed to load core library");
        env->ReleaseStringUTFChars(core_path, corePath);
        return JNI_FALSE;
    }

    // Resolve function pointers from the loaded core
    core_retro_init = (retro_init_t)dylib_proc(core_handle, "retro_init");
    core_retro_deinit = (retro_deinit_t)dylib_proc(core_handle, "retro_deinit");
    core_retro_api_version = (retro_api_version_t)dylib_proc(core_handle, "retro_api_version");
    core_retro_get_system_info = (retro_get_system_info_t)dylib_proc(core_handle, "retro_get_system_info");
    core_retro_get_system_av_info = (retro_get_system_av_info_t)dylib_proc(core_handle, "retro_get_system_av_info");
    core_retro_set_environment = (retro_set_environment_t)dylib_proc(core_handle, "retro_set_environment");
    core_retro_set_video_refresh = (retro_set_video_refresh_t)dylib_proc(core_handle, "retro_set_video_refresh");
    core_retro_set_audio_sample = (retro_set_audio_sample_t)dylib_proc(core_handle, "retro_set_audio_sample");
    core_retro_set_audio_sample_batch = (retro_set_audio_sample_batch_t)dylib_proc(core_handle, "retro_set_audio_sample_batch");
    core_retro_set_input_poll = (retro_set_input_poll_t)dylib_proc(core_handle, "retro_set_input_poll");
    core_retro_set_input_state = (retro_set_input_state_t)dylib_proc(core_handle, "retro_set_input_state");
    core_retro_set_controller_port_device = (retro_set_controller_port_device_t)dylib_proc(core_handle, "retro_set_controller_port_device");
    core_retro_reset = (retro_reset_t)dylib_proc(core_handle, "retro_reset");
    core_retro_run = (retro_run_t)dylib_proc(core_handle, "retro_run");
    core_retro_serialize_size = (retro_serialize_size_t)dylib_proc(core_handle, "retro_serialize_size");
    core_retro_serialize = (retro_serialize_t)dylib_proc(core_handle, "retro_serialize");
    core_retro_unserialize = (retro_unserialize_t)dylib_proc(core_handle, "retro_unserialize");
    core_retro_cheat_reset = (retro_cheat_reset_t)dylib_proc(core_handle, "retro_cheat_reset");
    core_retro_cheat_set = (retro_cheat_set_t)dylib_proc(core_handle, "retro_cheat_set");
    core_retro_load_game = (retro_load_game_t)dylib_proc(core_handle, "retro_load_game");
    core_retro_load_game_special = (retro_load_game_special_t)dylib_proc(core_handle, "retro_load_game_special");
    core_retro_unload_game = (retro_unload_game_t)dylib_proc(core_handle, "retro_unload_game");
    core_retro_get_region = (retro_get_region_t)dylib_proc(core_handle, "retro_get_region");
    core_retro_get_memory_data = (retro_get_memory_data_t)dylib_proc(core_handle, "retro_get_memory_data");
    core_retro_get_memory_size = (retro_get_memory_size_t)dylib_proc(core_handle, "retro_get_memory_size");

    // Check if required functions are available
    if (!core_retro_init || !core_retro_get_system_info || !core_retro_set_environment ||
        !core_retro_set_video_refresh || !core_retro_set_input_poll || !core_retro_set_input_state ||
        !core_retro_load_game || !core_retro_run || !core_retro_reset) {
        LOGE("Core does not provide required functions");
        dylib_close(core_handle);
        core_handle = NULL;
        env->ReleaseStringUTFChars(core_path, corePath);
        return JNI_FALSE;
    }

    // Set callbacks
    core_retro_set_environment(environment_callback);
    core_retro_set_video_refresh(video_refresh_callback);
    core_retro_set_audio_sample(audio_sample_callback);
    core_retro_set_audio_sample_batch(audio_sample_batch_callback);
    core_retro_set_input_poll(input_poll_callback);
    core_retro_set_input_state(input_state_callback);

    // Initialize core
    core_retro_init();

    // Get system info
    core_retro_get_system_info(&system_info);

    LOGI("Core loaded successfully: %s", system_info.library_name);
    core_loaded = true;

    env->ReleaseStringUTFChars(core_path, corePath);
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_reznor_emulation_libretro_LibretroCore_unloadCore(JNIEnv *env, jobject thiz) {
    if (core_loaded && core_handle) {
        core_retro_deinit();
        dylib_close(core_handle);
        core_handle = NULL;
        core_loaded = false;
        LOGI("Core unloaded");
    }

    // Clean up Java references
    if (libretroCoreObject) {
        env->DeleteGlobalRef(libretroCoreObject);
        libretroCoreObject = NULL;
    }
    videoFrameMethod = NULL;
    inputPollMethod = NULL;
    inputStateMethod = NULL;
}

JNIEXPORT jboolean JNICALL
Java_com_reznor_emulation_libretro_LibretroCore_loadGame(JNIEnv *env, jobject thiz, jstring game_path) {
    if (!core_loaded) return JNI_FALSE;

    const char *gamePath = env->GetStringUTFChars(game_path, nullptr);

    game_info.path = gamePath;
    game_info.data = NULL;
    game_info.size = 0;
    game_info.meta = "";

    bool success = core_retro_load_game(&game_info);

    env->ReleaseStringUTFChars(game_path, gamePath);

    LOGI("Game loaded: %s", success ? "success" : "failed");
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_reznor_emulation_libretro_LibretroCore_runFrame(JNIEnv *env, jobject thiz) {
    if (core_loaded) {
        core_retro_run();
    }
}

JNIEXPORT void JNICALL
Java_com_reznor_emulation_libretro_LibretroCore_reset(JNIEnv *env, jobject thiz) {
    if (core_loaded) {
        core_retro_reset();
    }
}

}
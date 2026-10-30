#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "stable-diffusion.h"

#define LOG_TAG "GGML_JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_ggmlapp_MainActivity_initEngine(JNIEnv *env, jobject /* this */, jstring modelPath, jstring vaePath) {
    const char *model_path = env->GetStringUTFChars(modelPath, nullptr);
    const char *vae_path = env->GetStringUTFChars(vaePath, nullptr);
    
    LOGI("Initializing engine with model: %s", model_path);
    sd_ctx_t *ctx = new_sd_ctx(model_path, vae_path, nullptr, nullptr, nullptr, false, false, false, true, false, 4, false, false, false, false, false);
    env->ReleaseStringUTFChars(modelPath, model_path);
    env->ReleaseStringUTFChars(vaePath, vae_path);
    
    if (ctx == nullptr) {
        LOGI("Failed to create SD context");
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_ggmlapp_MainActivity_generateImage(JNIEnv *env, jobject /* this */, jstring prompt, jint steps, jint width, jint height) {
    const char *prompt_str = env->GetStringUTFChars(prompt, nullptr);
    LOGI("Generating image for: %s", prompt_str);
    
    // Note: Need a proper way to pass the ctx handle, but for a quick test we assume global
    // In a real app, use a long pointer to store sd_ctx_t
    // For now, this is a placeholder structure. 
    
    env->ReleaseStringUTFChars(prompt, prompt_str);
    return nullptr; // Placeholder
}

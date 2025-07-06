#include <jni.h>
#include <aaudio/AAudio.h>
#include <android/log.h>
#include <atomic>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "SquareWave", __VA_ARGS__)

static std::atomic<AAudioStream*> stream = nullptr;
static std::atomic<bool> playing = false;
static double phase = 0.0;
static double phaseIncrement = 0.0;

static aaudio_data_callback_result_t dataCallback(
        AAudioStream* stream,
        void* userData,
        void* audioData,
        int32_t numFrames) {
    float* buffer = static_cast<float*>(audioData);
    for (int i = 0; i < numFrames; ++i) {
        buffer[i] = (phase < M_PI) ? 0.8f : -0.8f;
        phase += phaseIncrement;
        if (phase >= 2 * M_PI) phase -= 2 * M_PI;
    }
    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_squarewave_MainActivity_nativeStart(
        JNIEnv* env, jobject /* this */, jdouble frequency) {
if (playing.load()) return;
phase = 0.0;
phaseIncrement = 2 * M_PI * frequency / 48000.0;

AAudioStreamBuilder* builder = nullptr;
AAudio_createStreamBuilder(&builder);
AAudioStreamBuilder_setPerformanceMode(builder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
AAudioStreamBuilder_setSharingMode(builder, AAUDIO_SHARING_MODE_EXCLUSIVE);
AAudioStreamBuilder_setFormat(builder, AAUDIO_FORMAT_PCM_FLOAT);
AAudioStreamBuilder_setChannelCount(builder, 1);
AAudioStreamBuilder_setSampleRate(builder, 48000);
AAudioStreamBuilder_setDataCallback(builder, dataCallback, nullptr);

AAudioStream* newStream = nullptr;
aaudio_result_t result = AAudioStreamBuilder_openStream(builder, &newStream);
AAudioStreamBuilder_delete(builder);
if (result < 0) {
LOGI("Failed to open stream: %d", result);
return;
}
stream.store(newStream);
playing.store(true);
AAudioStream_requestStart(newStream);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_squarewave_MainActivity_nativeStop(
        JNIEnv* env, jobject /* this */) {
if (!playing.load()) return;
AAudioStream* s = stream.load();
AAudioStream_requestStop(s);
AAudioStream_close(s);
stream.store(nullptr);
playing.store(false);
}
#include <jni.h>
#include <stdint.h>
#include <stddef.h>
#include "com_volgoblob_internal_infrastructure_aggregation_nativeGo_aggregators_NativeDc.h"

extern uintptr_t DcInit(void);
extern void GetHashBatch(uintptr_t h, long long int* data, size_t n);
extern long long unsigned int DcFinish(uintptr_t h);

JNIEXPORT jlong JNICALL Java_com_volgoblob_internal_infrastructure_aggregation_nativeGo_aggregators_NativeDc_dcInit(JNIEnv *env, jobject self) {
    return (jlong)(uintptr_t)DcInit();
}

JNIEXPORT void JNICALL Java_com_volgoblob_internal_infrastructure_aggregation_nativeGo_aggregators_NativeDc_getHashBatch(JNIEnv *env, jobject self, jlong handle, jobject buffer, jint count) {
    void* ptr = (*env)->GetDirectBufferAddress(env, buffer);
    if (ptr == NULL) {
        return;
    }
    long long int* data = (long long int*)ptr;
    GetHashBatch((uintptr_t)handle, data, (size_t)count);
}

JNIEXPORT jlong JNICALL Java_com_volgoblob_internal_infrastructure_aggregation_nativeGo_aggregators_NativeDc_dcFinish(JNIEnv *env, jobject self, jlong handle) {
    return (jlong)DcFinish((uintptr_t)handle);
}

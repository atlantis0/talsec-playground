#include <jni.h>

int result_callback(YR_SCAN_CONTEXT* context, int message, void* message_data, void* user_data);
extern "C" int Java_com_talsec_t_malware_Scan_check(JNIEnv* env, jobject,
                                                                   jstring app, jstring file_to_check, jstring rule_file_path);
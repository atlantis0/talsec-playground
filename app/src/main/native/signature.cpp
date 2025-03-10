
#include <jni.h>
#include <yara.h>
#include <openssl/ssl.h>
#include <android/log.h>
#include <utility>
#include <string>
#include <vector>
#include <list>
#include <iostream>
#include <sstream>
#include <android/log.h>

static const char* TAG = "Yara";

struct UserData {
    const char * file_path;
    const char * pkg_name;
    const char * rule_path;
    JNIEnv *env;
    jobject obj;
};

int result_callback(YR_SCAN_CONTEXT* context, int message, void* message_data, void* user_data) {
    if(message == CALLBACK_MSG_RULE_MATCHING) {
        YR_RULE* rule = (YR_RULE*) message_data;
        UserData * user_d = (struct UserData*) user_data;
        JNIEnv *env = user_d->env;
        jclass jniScanClass = env->GetObjectClass(user_d->obj);
        jmethodID matchResultMethod = env->GetMethodID(jniScanClass, "result", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
        jstring j_pkg_name = env->NewStringUTF(user_d->pkg_name);
        jstring j_file_path = env->NewStringUTF(user_d->file_path);
        jstring j_identifier = env->NewStringUTF(user_d->rule_path);
        env->CallVoidMethod(user_d->obj, matchResultMethod, j_pkg_name, j_file_path, j_identifier);
        // clean up refs
        env->DeleteLocalRef(j_pkg_name); env->DeleteLocalRef(j_file_path); env->DeleteLocalRef(j_identifier);
    }
    return CALLBACK_CONTINUE;
}

extern "C" int JNICALL Java_com_talsec_t_malware_Scan_check(JNIEnv *env, jobject obj,
                                                                      jstring app, jstring file_to_check, jstring rule_file_path) {
    const char *rule_file = env->GetStringUTFChars(rule_file_path, 0);
    const char *input_file = env->GetStringUTFChars(file_to_check, 0);
    const char *app_package_name = env->GetStringUTFChars(app, 0);

    yr_initialize();
    YR_COMPILER* compiler = NULL;
    YR_RULES* rules = NULL;

    jclass jniRuleProcessorClass = env->GetObjectClass(obj);

    if (yr_compiler_create(&compiler) != ERROR_SUCCESS) {
        // TODO - better error handling
        return -1;
    }
    // Compile a rule that use the variables in the condition.
    FILE* rule_file_f = fopen(rule_file,"r");
    if (yr_compiler_add_file(compiler, rule_file_f, NULL, rule_file) != 0)
    {
        return -1;
    }
    // close file
    fclose(rule_file_f);
    // get compiled rule
    if (yr_compiler_get_rules(compiler, &rules) != ERROR_SUCCESS) {
        yr_compiler_destroy(compiler);
        // TODO - better error handling
        return -1;
    }

    UserData user_data{};
    user_data.file_path = input_file;
    user_data.pkg_name = app_package_name;
    user_data.rule_path = rule_file;
    user_data.env = env;
    user_data.obj = obj;

    int result = yr_rules_scan_file(rules, input_file, 0, result_callback, &user_data, 60);

    if (result != ERROR_SUCCESS) {
        yr_rules_destroy(rules);
        // TODO - better error handling
        return -1;
    }

    // destroy compiler
    yr_compiler_destroy(compiler);

    // clean up
    yr_rules_destroy(rules);
    yr_finalize();
    env->ReleaseStringUTFChars(rule_file_path, rule_file);
    env->ReleaseStringUTFChars(file_to_check, input_file);
    env->ReleaseStringUTFChars(app, app_package_name);

    // all is good
    return 0;
}
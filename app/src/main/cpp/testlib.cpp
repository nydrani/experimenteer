#include <jni.h>
#include <android/log.h>
#include <string>
#include <dlfcn.h>
#include <fcntl.h>
#include <unistd.h>
#include <cerrno>

#define LOG_TAG "libtest"
#define LOGA(...)  __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)

// NOTE: PIN BLOCK STUFF
#define PIN_END 14
#define PIN_BLOCK_SIZE 8
#define PIN_BLOCK_NIBBLE_SIZE (PIN_BLOCK_SIZE * 2)
#define PIN_START 2
static unsigned char pinBlock[PIN_BLOCK_NIBBLE_SIZE];
static unsigned char curPos = 0;
static int randomFd = -1;


char getRandomByte(JNIEnv* env) {
    // securerandom instance --> finish

    jclass secRandomClass = env->FindClass("java/security/SecureRandom");
    jmethodID secRandomInit = env->GetMethodID(secRandomClass, "<init>", "()V");
    jmethodID secRandomNextInt = env->GetMethodID(secRandomClass, "nextInt", "()I");

    jobject secRandomObj = env->NewObject(secRandomClass, secRandomInit);

    char rand = static_cast<char>(env->CallIntMethod(secRandomObj, secRandomNextInt));

    return rand;
}

// key is encrypted by a keystore key --> decrypt it in here
jbyteArray decryptKey(JNIEnv* env, jbyteArray key, jbyteArray iv) {
    jstring jaesalgostring = env->NewStringUTF("AES/CBC/PKCS7Padding");
    jstring keyStoreType = env->NewStringUTF("AndroidKeyStore");
    jstring aesKeyAlias = env->NewStringUTF("AES256babey");

    // find all methods
    jclass keyStoreClass = env->FindClass("java/security/KeyStore");
    jmethodID keyStoreGetInstance = env->GetStaticMethodID(keyStoreClass, "getInstance", "(Ljava/lang/String;)Ljava/security/KeyStore;");
    jmethodID keyStoreLoad = env->GetMethodID(keyStoreClass, "load", "(Ljava/security/KeyStore$LoadStoreParameter;)V");
    jmethodID keyStoreGetKey = env->GetMethodID(keyStoreClass, "getKey", "(Ljava/lang/String;[C)Ljava/security/Key;");

    jclass ivParameterSpecClass = env->FindClass("javax/crypto/spec/IvParameterSpec");
    jmethodID ivParameterSpecInit = env->GetMethodID(ivParameterSpecClass, "<init>", "([B)V");

    jclass cipherClass = env->FindClass("javax/crypto/Cipher");
    jmethodID cipherGetInstance = env->GetStaticMethodID(cipherClass, "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;");
    jmethodID cipherInit = env->GetMethodID(cipherClass, "init", "(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V");
    jmethodID cipherDoFinal = env->GetMethodID(cipherClass, "doFinal", "([B)[B");

    // make calls here
    // grab secret key from keystore
    jobject keyStoreInstance = env->CallStaticObjectMethod(keyStoreClass, keyStoreGetInstance, keyStoreType);
    env->CallVoidMethod(keyStoreInstance, keyStoreLoad, nullptr);
    jobject secretKey = env->CallObjectMethod(keyStoreInstance, keyStoreGetKey, aesKeyAlias, nullptr);

    jobject ivParameterSpecInstance = env->NewObject(ivParameterSpecClass, ivParameterSpecInit, iv);

    // cipher decrypt
    // 2 is Cipher.DECRYPT_MODE
    jobject cipherInstance = env->CallStaticObjectMethod(cipherClass, cipherGetInstance, jaesalgostring);
    env->CallVoidMethod(cipherInstance, cipherInit, 2, secretKey, ivParameterSpecInstance);
    auto res = reinterpret_cast<jbyteArray>(env->CallObjectMethod(cipherInstance, cipherDoFinal, key));

    // free strings
    env->DeleteLocalRef(jaesalgostring);
    env->DeleteLocalRef(keyStoreType);
    env->DeleteLocalRef(aesKeyAlias);

    // return decrypted key
    return res;
}

// encrypt pinblock1 using DESede
jobject encryptPin(JNIEnv* env, jbyteArray key, jbyteArray data) {
    jstring jdesstring = env->NewStringUTF("DESede");
    jstring jdesalgostring = env->NewStringUTF("DESede/CBC/NoPadding");

    // find all methods
    jclass secretKeyFactoryClass = env->FindClass("javax/crypto/SecretKeyFactory");
    jmethodID secretKeyFactoryGetInstance = env->GetStaticMethodID(secretKeyFactoryClass, "getInstance", "(Ljava/lang/String;)Ljavax/crypto/SecretKeyFactory;");
    jmethodID secretKeyFactoryGenerateSecret = env->GetMethodID(secretKeyFactoryClass, "generateSecret", "(Ljava/security/spec/KeySpec;)Ljavax/crypto/SecretKey;");

    jclass desedeKeySpecClass = env->FindClass("javax/crypto/spec/DESedeKeySpec");
    jmethodID desedeKeySpecInit = env->GetMethodID(desedeKeySpecClass, "<init>", "([B)V");

    jclass cipherClass = env->FindClass("javax/crypto/Cipher");
    jmethodID cipherGetInstance = env->GetStaticMethodID(cipherClass, "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;");
    jmethodID cipherInit = env->GetMethodID(cipherClass, "init", "(ILjava/security/Key;)V");
    jmethodID cipherGetIV = env->GetMethodID(cipherClass, "getIV", "()[B");
    jmethodID cipherDoFinal = env->GetMethodID(cipherClass, "doFinal", "([B)[B");

    // make calls here
    // load key using JCA and use via Cipher instance
    jobject desedeKeySpecInstance = env->NewObject(desedeKeySpecClass, desedeKeySpecInit, key);
    jobject secretKeyFactoryInstance = env->CallStaticObjectMethod(secretKeyFactoryClass, secretKeyFactoryGetInstance, jdesstring);
    jobject secretKey = env->CallObjectMethod(secretKeyFactoryInstance, secretKeyFactoryGenerateSecret, desedeKeySpecInstance);

    // cipher encrypt
    // 1 is Cipher.ENCRYPT_MODE
    jobject cipherInstance = env->CallStaticObjectMethod(cipherClass, cipherGetInstance, jdesalgostring);
    env->CallVoidMethod(cipherInstance, cipherInit, 1, secretKey);
    auto iv = reinterpret_cast<jbyteArray>(env->CallObjectMethod(cipherInstance, cipherGetIV));
    auto res = reinterpret_cast<jbyteArray>(env->CallObjectMethod(cipherInstance, cipherDoFinal, data));

    // free strings
    env->DeleteLocalRef(jdesstring);
    env->DeleteLocalRef(jdesalgostring);

    // return iv and ciphertext
    jclass cipherResultClass = env->FindClass("xyz/velvetmilk/testingtool/models/CipherResult");
    jmethodID cipherResultInit = env->GetMethodID(cipherResultClass, "<init>", "([B[B)V");
    jobject cipherResultInstance = env->NewObject(cipherResultClass, cipherResultInit, iv, res);

    return cipherResultInstance;
}

extern "C" {
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    // am i even allowed to do this?
    // TODO: what do when this fails????
    randomFd = open("/dev/urandom", O_RDONLY | O_CLOEXEC);
    if (randomFd == -1) {
        LOGA("Failed to open /dev/urandom | %d", errno);
    }

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    // close urandom fd if open
    if (randomFd != -1) {
        close(randomFd);
    }
}

JNIEXPORT jstring JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_nativeString(JNIEnv* env, jobject obj) {
    std::string hello = "(string) Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jstring JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_nativeByteArray(JNIEnv* env, jobject obj, jbyteArray byteArray) {
    auto arrayLen = static_cast<u_int32_t>(env->GetArrayLength(byteArray));

    jbyte buffer[arrayLen];
    env->GetByteArrayRegion(byteArray, 0, arrayLen, buffer);

    const char* cstring = reinterpret_cast<const char*>(buffer);
    std::string byteString(cstring, arrayLen);

    jstring string = env->NewStringUTF(byteString.c_str());

    return string;
}

JNIEXPORT jbyteArray JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_nativeToByteArray(JNIEnv* env, jobject obj, jstring string) {
    auto strLength = static_cast<u_int32_t>(env->GetStringUTFLength(string));

    char buffer[strLength];
    env->GetStringUTFRegion(string, 0, strLength, buffer);

    jbyteArray array = env->NewByteArray(strLength);
    env->SetByteArrayRegion(array, 0, strLength, reinterpret_cast<const jbyte*>(buffer));

    return array;
}

JNIEXPORT jstring JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_nativeGrabSha256(JNIEnv* env, jobject obj, jstring string) {
    auto strLength = static_cast<u_int32_t>(env->GetStringUTFLength(string));

    char buffer[strLength];
    env->GetStringUTFRegion(string, 0, strLength, buffer);

    char command[] = "sha256sum ";
    char command_buffer[strLength+sizeof(command)/sizeof(command[0])];

    strcat(command_buffer, command);
    strcat(command_buffer, buffer);

    int result = system(command_buffer);
    LOGA("%d\n", result);

    return string;
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_nativeTestDlSym(JNIEnv* env, jobject obj) {
    void *handle;
    void (*func_dynamic_call)();

    handle = dlopen("libantidebugging.so", RTLD_LAZY);
    if (handle == nullptr) {
        /* fail to load the library */
        LOGA("Error: %s\n", dlerror());
        return static_cast<jboolean>(false);
    }

    *(void**)(&func_dynamic_call) = dlsym(handle, "dynamic_test");
    if (func_dynamic_call == nullptr) {
        /* no such symbol */
        LOGA("Error: %s\n", dlerror());
        dlclose(handle);
        return static_cast<jboolean>(false);
    }

    func_dynamic_call();
    dlclose(handle);

    return static_cast<jboolean>(true);
}

// NOTE: PIN BLOCK CODE
JNIEXPORT void JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_prepare(JNIEnv* env, jobject obj) {
    // clear block
    memset(pinBlock, 0, sizeof(pinBlock));
    curPos = PIN_START;
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_addDigit(JNIEnv* env, jobject obj, jbyte byte) {
    // dont add if beyond max length
    if (curPos >= PIN_END) {
        return static_cast<jboolean>(false);
    }

    // dont add if greater than (4 bit value) [2^4] {'0' -> 'F'}
    if (static_cast<unsigned char>(byte) > 16) {
        return static_cast<jboolean>(false);
    }

    pinBlock[curPos] = static_cast<unsigned char>(byte);
    ++curPos;

    return static_cast<jboolean>(true);
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_removeDigit(JNIEnv* env, jobject obj) {
    // dont add if beyond max length
    if (curPos <= PIN_START) {
        return static_cast<jboolean>(false);
    }

    --curPos;

    return static_cast<jboolean>(true);
}

JNIEXPORT jbyteArray JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_complete(JNIEnv* env, jobject obj) {
    jbyteArray arr = env->NewByteArray(PIN_BLOCK_SIZE);

    // fill header
    pinBlock[0] = 1;
    pinBlock[1] = static_cast<unsigned char>(curPos - PIN_START);

    // fill the rest with random bytes
    for (unsigned char i = curPos; i < PIN_BLOCK_NIBBLE_SIZE; ++i) {
        pinBlock[i] = static_cast<unsigned char>(getRandomByte(env));
    }

    for (unsigned char i = 0; i < PIN_BLOCK_NIBBLE_SIZE; ++i) {
        LOGA("pos: %d | val: %d", i, pinBlock[i]);
    }

    // squash from bytes to nibbles
    unsigned char squashedPinBlock[PIN_BLOCK_SIZE];
    //memset(squashedPinBlock, 0, sizeof(squashedPinBlock));
    for (unsigned char i = 0; i < PIN_BLOCK_SIZE; ++i) {
        squashedPinBlock[i] = pinBlock[2*i+1];
        squashedPinBlock[i] |= pinBlock[2*i] << 4;
    }

    env->SetByteArrayRegion(arr, 0, PIN_BLOCK_SIZE, reinterpret_cast<jbyte*>(squashedPinBlock));

    // clear block after copying and reset pointer
    memset(squashedPinBlock, 0, sizeof(squashedPinBlock));
    memset(pinBlock, 0, sizeof(pinBlock));
    curPos = PIN_START;

    return arr;
}

JNIEXPORT jbyte JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_random(JNIEnv* env, jobject obj) {
    char rand = getRandomByte(env);

    return static_cast<jbyte>(rand);
}

JNIEXPORT jbyte JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_urandom(JNIEnv* env, jobject obj) {
    char rand;
    ssize_t res = read(randomFd, &rand, 1);
    if (res == -1) {
        LOGA("Failed to read | %d", errno);
    }

    return static_cast<jbyte>(rand);
}

JNIEXPORT jbyteArray JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_decryptKey(JNIEnv* env, jobject obj, jbyteArray key, jbyteArray iv) {
    jbyteArray decrypted = decryptKey(env, key, iv);

    return decrypted;
}

JNIEXPORT jobject JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_encryptPin(JNIEnv* env, jobject obj, jbyteArray key, jbyteArray data) {
    jobject encrypted = encryptPin(env, key, data);

    return encrypted;
}
}

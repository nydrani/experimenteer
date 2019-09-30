#include <jni.h>
#include <android/log.h>
#include <android_native_app_glue.h>
#include <string>
#include <vector>
#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>


#define LOG_TAG "libvulkantest"
#define LOGA(...)  __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


// Vulkan call wrapper
#define CALL_VK(func) if (VK_SUCCESS != (func)) { LOGE("Vulkan error. File[%s], line[%d]", __FILE__, __LINE__); assert(false); }

#define DEBUG
#ifdef DEBUG
/// @brief A debug callback called from Vulkan validation layers.
static VKAPI_ATTR VkBool32 VKAPI_CALL debug_callback(VkDebugReportFlagsEXT flags, VkDebugReportObjectTypeEXT type,
                                                     uint64_t object, size_t location, int32_t message_code,
                                                     const char *layer_prefix, const char *message, void *user_data) {
    if (flags & VK_DEBUG_REPORT_ERROR_BIT_EXT) {
        LOGE("Validation Layer: Error: {}: {}", layer_prefix, message);
    } else if (flags & VK_DEBUG_REPORT_WARNING_BIT_EXT) {
        LOGW("Validation Layer: Warning: {}: {}", layer_prefix, message);
    } else if (flags & VK_DEBUG_REPORT_PERFORMANCE_WARNING_BIT_EXT) {
        LOGI("Validation Layer: Performance warning: {}: {}", layer_prefix, message);
    } else {
        LOGI("Validation Layer: Information: {}: {}", layer_prefix, message);
    }
	return VK_FALSE;
}
#endif
#undef DEBUG

// Global variables
VkInstance tutorialInstance;
VkPhysicalDevice tutorialGpu;
VkDevice tutorialDevice;
VkSurfaceKHR tutorialSurface;

// TODO: MOVE THIS TO H FILE
// We will call this function the window is opened.
// This is where we will initialise everything
bool initialised = false;
bool initialise(android_app *);

// Functions interacting with Android native activity
void terminate();
void handle_cmd(android_app *, int32_t);



// typical Android NativeActivity entry function
void android_main(struct android_app* app) {
    app->onAppCmd = handle_cmd;

    int events;
    android_poll_source* source;
    do {
        if (ALooper_pollAll(initialised ? 1: 0, nullptr, &events, (void**)&source) >= 0) {
            if (source != nullptr) source->process(app, source);
        }
    } while (app->destroyRequested == 0);
}

bool initialise(android_app *app) {
//    uint32_t apiVersion;
//    CALL_VK(vkEnumerateInstanceVersion(&apiVersion));
//    LOGA("Vulkan Instance Version: %d", apiVersion);

    VkApplicationInfo appInfo = {
            .sType = VK_STRUCTURE_TYPE_APPLICATION_INFO,
            .pNext = nullptr,
            .apiVersion = VK_MAKE_VERSION(1, 0, 0),
            .applicationVersion = VK_MAKE_VERSION(1, 0, 0),
            .engineVersion = VK_MAKE_VERSION(1, 0, 0),
            .pApplicationName = "tutorial01_load_vulkan",
            .pEngineName = "tutorial",
    };

    // prepare necessary extensions: Vulkan on Android need these to function
    std::vector<const char *> instanceExt;
    instanceExt.push_back("VK_KHR_surface");
    instanceExt.push_back("VK_KHR_android_surface");
    instanceExt.push_back(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);

    std::vector<const char *> deviceExt;
    deviceExt.push_back("VK_KHR_swapchain");

    std::vector<const char *> validationLayers;
    validationLayers.push_back("VK_LAYER_KHRONOS_validation");

    // Create the Vulkan instance
    VkInstanceCreateInfo instanceCreateInfo {
            .sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO,
            .pNext = nullptr,
            .pApplicationInfo = &appInfo,
            .enabledExtensionCount = static_cast<uint32_t>(instanceExt.size()),
            .ppEnabledExtensionNames = instanceExt.data(),
            .enabledLayerCount = static_cast<uint32_t>(validationLayers.size()),
            .ppEnabledLayerNames = validationLayers.data()
    };
    CALL_VK(vkCreateInstance(&instanceCreateInfo, nullptr, &tutorialInstance));

    // if we create a surface, we need the surface extension
    VkAndroidSurfaceCreateInfoKHR createInfo {
            .sType = VK_STRUCTURE_TYPE_ANDROID_SURFACE_CREATE_INFO_KHR,
            .pNext = nullptr,
            .flags = 0,
            .window = app->window
    };
    CALL_VK(vkCreateAndroidSurfaceKHR(tutorialInstance, &createInfo, nullptr,
                                      &tutorialSurface));

    // Find one GPU to use:
    // On Android, every GPU device is equal -- supporting graphics/compute/present
    // for this sample, we use the very first GPU device found on the system
    uint32_t gpuCount = 0;
    CALL_VK(vkEnumeratePhysicalDevices(tutorialInstance, &gpuCount, nullptr));
    LOGA("Vulkan Num Physical Devices: %d", gpuCount);

    VkPhysicalDevice tmpGpus[gpuCount];
    CALL_VK(vkEnumeratePhysicalDevices(tutorialInstance, &gpuCount, tmpGpus));
    tutorialGpu = tmpGpus[0];     // Pick up the first GPU Device

    // check for vulkan info on this GPU device
    VkPhysicalDeviceProperties  gpuProperties;
    vkGetPhysicalDeviceProperties(tutorialGpu, &gpuProperties);
    LOGA("Vulkan Physical Device Name: %s", gpuProperties.deviceName);
    LOGA("Vulkan Physical Device Info: apiVersion: %x", gpuProperties.apiVersion);
    LOGA("Vulkan Physical Device Info: driverVersion: %x", gpuProperties.driverVersion);
    LOGA("API Version Supported: %d.%d.%d",
         VK_VERSION_MAJOR(gpuProperties.apiVersion),
         VK_VERSION_MINOR(gpuProperties.apiVersion),
         VK_VERSION_PATCH(gpuProperties.apiVersion)
    );

    VkSurfaceCapabilitiesKHR surfaceCapabilities;
    vkGetPhysicalDeviceSurfaceCapabilitiesKHR(tutorialGpu,
                                              tutorialSurface,
                                              &surfaceCapabilities);

    LOGA("Vulkan Surface Capabilities:\n");
    LOGA("\tImage count: %u - %u\n", surfaceCapabilities.minImageCount,
         surfaceCapabilities.maxImageCount);
    LOGA("\tArray layers: %u\n", surfaceCapabilities.maxImageArrayLayers);
    LOGA("\tImage size (now): %dx%d\n",
         surfaceCapabilities.currentExtent.width,
         surfaceCapabilities.currentExtent.height);
    LOGA("\tImage size (extent): %dx%d - %dx%d\n",
         surfaceCapabilities.minImageExtent.width,
         surfaceCapabilities.minImageExtent.height,
         surfaceCapabilities.maxImageExtent.width,
         surfaceCapabilities.maxImageExtent.height);
    LOGA("\tUsage: %x\n", surfaceCapabilities.supportedUsageFlags);
    LOGA("\tCurrent transform: %u\n", surfaceCapabilities.currentTransform);
    LOGA("\tAllowed transforms: %x\n", surfaceCapabilities.supportedTransforms);
    LOGA("\tComposite alpha flags: %u\n", surfaceCapabilities.currentTransform);

    // Create a logical device from GPU we picked
    float priorities[] = { 1.0F };
    VkDeviceQueueCreateInfo queueCreateInfo {
            .sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO,
            .pNext = nullptr,
            .flags = 0,
            .queueCount = 1,
            .queueFamilyIndex = 0,
            .pQueuePriorities = priorities,
    };

    VkDeviceCreateInfo deviceCreateInfo {
            .sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO,
            .pNext = nullptr,
            .queueCreateInfoCount = 1,
            .pQueueCreateInfos = &queueCreateInfo,
            .enabledLayerCount = 0,
            .ppEnabledLayerNames = nullptr,
            .enabledExtensionCount = static_cast<uint32_t>(deviceExt.size()),
            .ppEnabledExtensionNames = deviceExt.data(),
            .pEnabledFeatures = nullptr,
    };

    CALL_VK(vkCreateDevice(tutorialGpu, &deviceCreateInfo, nullptr,
                           &tutorialDevice));
    initialised = true;
    return false;
}

void terminate() {
    vkDestroySurfaceKHR(tutorialInstance, tutorialSurface, nullptr);
    vkDestroyDevice(tutorialDevice, nullptr);
    vkDestroyInstance(tutorialInstance, nullptr);

    initialised = false;
}

// Process the next main command.
void handle_cmd(android_app* app, int32_t cmd) {
    switch (cmd) {
        case APP_CMD_INPUT_CHANGED:
            LOGA("APP_CMD_INPUT_CHANGED");
            break;
        case APP_CMD_INIT_WINDOW:
            LOGA("APP_CMD_INIT_WINDOW");
            break;
        case APP_CMD_TERM_WINDOW:
            LOGA("APP_CMD_TERM_WINDOW");
            break;
        case APP_CMD_WINDOW_RESIZED:
            LOGA("APP_CMD_WINDOW_RESIZED");
            break;
        case APP_CMD_WINDOW_REDRAW_NEEDED:
            LOGA("APP_CMD_WINDOW_REDRAW_NEEDED");
            break;
        case APP_CMD_CONTENT_RECT_CHANGED:
            LOGA("APP_CMD_CONTENT_RECT_CHANGED");
            break;
        case APP_CMD_GAINED_FOCUS:
            LOGA("APP_CMD_GAINED_FOCUS");
            break;
        case APP_CMD_LOST_FOCUS:
            LOGA("APP_CMD_LOST_FOCUS");
            break;
        case APP_CMD_CONFIG_CHANGED:
            LOGA("APP_CMD_CONFIG_CHANGED");
            break;
        case APP_CMD_LOW_MEMORY:
            LOGA("APP_CMD_LOW_MEMORY");
            break;
        case APP_CMD_START:
            LOGA("APP_CMD_START");
            break;
        case APP_CMD_RESUME:
            LOGA("APP_CMD_RESUME");
            break;
        case APP_CMD_SAVE_STATE:
            LOGA("APP_CMD_SAVE_STATE");
            break;
        case APP_CMD_PAUSE:
            LOGA("APP_CMD_PAUSE");
            break;
        case APP_CMD_STOP:
            LOGA("APP_CMD_STOP");
            break;
        case APP_CMD_DESTROY:
            LOGA("APP_CMD_DESTROY");
            break;
        default:
            LOGA("Event not handled: %d", cmd);
            break;
    }

    switch (cmd) {
        case APP_CMD_INIT_WINDOW:
            // The window is being shown, get it ready.
            initialise(app);
            break;
        case APP_CMD_TERM_WINDOW:
            // The window is being hidden or closed, clean it up.
            terminate();
            break;
        default:
            break;
    }
}

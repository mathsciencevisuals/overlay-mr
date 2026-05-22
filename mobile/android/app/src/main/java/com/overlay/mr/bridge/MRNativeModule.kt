package com.overlay.mr.bridge

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.overlay.mr.render.MRRendererCoordinator
import kotlinx.coroutines.*

class MRNativeModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val coordinator = MRRendererCoordinator(reactContext) { name, payload ->
        emitEvent(name, payload)
    }

    override fun getName(): String = "MRNativeModule"

    @ReactMethod
    fun initialize(config: ReadableMap, promise: Promise) {
        scope.launch {
            runCatching {
                coordinator.initialize(config.toHashMap())
            }.onSuccess {
                promise.resolve(null)
            }.onFailure {
                promise.reject("INIT_ERROR", it)
            }
        }
    }

    @ReactMethod
    fun updateConfig(config: ReadableMap, promise: Promise) {
        scope.launch {
            runCatching {
                coordinator.updateConfig(config.toHashMap())
            }.onSuccess {
                promise.resolve(null)
            }.onFailure {
                promise.reject("CONFIG_ERROR", it)
            }
        }
    }

    @ReactMethod
    fun startProcessing(promise: Promise) {
        scope.launch {
            runCatching { coordinator.start() }
                .onSuccess { promise.resolve(null) }
                .onFailure { promise.reject("START_ERROR", it) }
        }
    }

    @ReactMethod
    fun stopProcessing(promise: Promise) {
        scope.launch {
            runCatching { coordinator.stop() }
                .onSuccess { promise.resolve(null) }
                .onFailure { promise.reject("STOP_ERROR", it) }
        }
    }

    @ReactMethod
    fun capturePhoto(promise: Promise) {
        scope.launch {
            runCatching { coordinator.capturePhoto() }
                .onSuccess { promise.resolve(it) }
                .onFailure { promise.reject("CAPTURE_ERROR", it) }
        }
    }

    @ReactMethod
    fun startRecording(promise: Promise) {
        scope.launch {
            runCatching { coordinator.startRecording() }
                .onSuccess { promise.resolve(null) }
                .onFailure { promise.reject("RECORD_START_ERROR", it) }
        }
    }

    @ReactMethod
    fun stopRecording(promise: Promise) {
        scope.launch {
            runCatching { coordinator.stopRecording() }
                .onSuccess { promise.resolve(it) }
                .onFailure { promise.reject("RECORD_STOP_ERROR", it) }
        }
    }

    private fun emitEvent(name: String, payload: WritableMap) {
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(name, payload)
    }
}

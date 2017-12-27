package com.wonderkiln.camerakit;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class EventDispatcher {

    private Handler mainThreadHandler;

    private List<CameraKitEventListener> listeners;
    private List<BindingHandler> bindings;

    public EventDispatcher() {
        this.mainThreadHandler = new Handler(Looper.getMainLooper());
        this.listeners = new ArrayList<>();
        this.bindings = new ArrayList<>();
    }

    public void addListener(CameraKitEventListener listener) {
        this.listeners.add(listener);
    }

    public void addBinding(Object binding) {
        this.bindings.add(new BindingHandler(binding));
    }

    public void dispatch(final CameraKitEvent event) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                for (CameraKitEventListener listener : listeners) {
                    listener.onEvent(event);
                    if (event instanceof CameraKitError) listener.onError((CameraKitError) event);
                    if (event instanceof CameraKitImage) listener.onImage((CameraKitImage) event);
                    if (event instanceof CameraKitVideo) listener.onVideo((CameraKitVideo) event);
                }

                for (BindingHandler handler : bindings) {
                    try {
                        handler.dispatchEvent(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private class BindingHandler {

        private Map<Class, List<MethodHolder>> methods;

        public BindingHandler(@NonNull Object binding) {
            this.methods = new HashMap<>();

            for (Method method : binding.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(OnCameraKitEvent.class)) {
                    OnCameraKitEvent annotation = method.getAnnotation(OnCameraKitEvent.class);
                    Class<? extends CameraKitEvent> eventType = annotation.value();
                    addMethod(binding, method, eventType, methods);
                }
            }
        }

        private void addMethod(Object binding, Method method, Class<? extends CameraKitEvent> type, Map<Class, List<MethodHolder>> store) {
            if (!store.containsKey(type)) {
                store.put(type, new ArrayList<MethodHolder>());
            }

            store.get(type).add(new MethodHolder(binding, method));
        }

        public void dispatchEvent(@NonNull CameraKitEvent event) throws IllegalAccessException, InvocationTargetException {
            List<MethodHolder> baseMethods = methods.get(CameraKitEvent.class);
            if (baseMethods != null) {
                for (MethodHolder methodHolder : baseMethods) {
                    methodHolder.getMethod().invoke(methodHolder.getBinding(), event);
                }
            }

            List<MethodHolder> targetMethods = methods.get(event.getClass());
            if (targetMethods != null) {
                for (MethodHolder methodHolder : targetMethods) {
                    methodHolder.getMethod().invoke(methodHolder.getBinding(), event);
                }
            }
        }

        private class MethodHolder {

            private Object binding;
            private Method method;

            public MethodHolder(Object binding, Method method) {
                this.binding = binding;
                this.method = method;
            }

            public Object getBinding() {
                return binding;
            }

            public Method getMethod() {
                return method;
            }

        }

    }

}

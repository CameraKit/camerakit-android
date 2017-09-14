package com.flurgle.camerakit;

/**
 * Created by Bruno Capezzali on 9/14/17.
 */

public interface ErrorListener {
    void onError(Exception e);
    void onEvent(String event);
}

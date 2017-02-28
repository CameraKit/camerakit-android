package com.flurgle.camerakit.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class AutoUnfocusEditText extends EditText {

    public AutoUnfocusEditText(Context context) {
        super(context);
    }

    public AutoUnfocusEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoUnfocusEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            closeKeyboard();
            clearFocus();
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

}
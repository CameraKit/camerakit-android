package com.wonderkiln.camerakit.demo;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

public class AutoUnfocusEditText extends AppCompatEditText {

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

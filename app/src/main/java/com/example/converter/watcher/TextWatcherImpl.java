package com.example.converter.watcher;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TextWatcherImpl implements TextWatcher {
    private final FloatingActionButton button;

    public TextWatcherImpl(FloatingActionButton button) {
        this.button = button;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() == 0) button.setVisibility(View.GONE);
        else button.setVisibility(View.VISIBLE);
    }
}

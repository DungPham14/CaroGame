package com.ducdung.carogame.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ducdung.carogame.R;
import com.ducdung.carogame.data.Constants;

public class SettingActivity extends AppCompatActivity
    implements Constants, CompoundButton.OnCheckedChangeListener {
    private SharedPreferences.Editor mEditor;
    private CheckBox mCheckBoxBlockTwoHeadWin, mCheckBoxSound;
    private RadioGroup mRadioGroupOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initViews();
        handleViewsOnClick();
    }

    private void initViews() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, Context
            .MODE_PRIVATE);
        mEditor = sharedPreferences.edit();
        mCheckBoxBlockTwoHeadWin = (CheckBox) findViewById(R.id.checkbox_two_head_win_block);
        mCheckBoxSound = (CheckBox) findViewById(R.id.checkbox_sound);
        RadioButton radioButtonLandscape = (RadioButton) findViewById(R.id.radio_button_landscape);
        RadioButton radioButtonPortrait = (RadioButton) findViewById(R.id.radio_button_portrait);
        mRadioGroupOrientation = (RadioGroup) findViewById(R.id.radio_group_orientation);
        mCheckBoxBlockTwoHeadWin
            .setChecked(sharedPreferences.getBoolean(TWO_HEAD_WIN_BLOCK, true));
        mCheckBoxSound.setChecked(sharedPreferences.getBoolean(SOUND, true));
        boolean isPortrait = sharedPreferences.getBoolean(SCREEN_ORIENTATION_PORTRAIT, true);
        radioButtonPortrait.setChecked(isPortrait);
        radioButtonLandscape.setChecked(!isPortrait);
        radioButtonLandscape.setOnCheckedChangeListener(this);
        radioButtonPortrait.setOnCheckedChangeListener(this);
    }

    private void handleViewsOnClick() {
        mCheckBoxBlockTwoHeadWin.setOnCheckedChangeListener(this);
        mCheckBoxSound.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.checkbox_two_head_win_block:
                mEditor.putBoolean(TWO_HEAD_WIN_BLOCK, isChecked);
                break;
            case R.id.checkbox_sound:
                mEditor.putBoolean(SOUND, isChecked);
                break;
        }
        mEditor.apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        switch (mRadioGroupOrientation.getCheckedRadioButtonId()) {
            case R.id.radio_button_landscape:
                mEditor.putBoolean(SCREEN_ORIENTATION_PORTRAIT, false);
                break;
            case R.id.radio_button_portrait:
                mEditor.putBoolean(SCREEN_ORIENTATION_PORTRAIT, true);
                break;
        }
        mEditor.apply();
    }
}

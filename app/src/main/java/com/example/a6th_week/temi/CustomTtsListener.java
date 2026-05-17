package com.example.a6th_week.temi;


import android.util.Log;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import org.jetbrains.annotations.NotNull;

public class CustomTtsListener implements Robot.TtsListener{
    @Override
    public void onTtsStatusChanged(@NotNull TtsRequest ttsRequest) {
        Log.i("Temi","Tts Status changed! "+ttsRequest.getStatus()+" - Custom");
    }
}


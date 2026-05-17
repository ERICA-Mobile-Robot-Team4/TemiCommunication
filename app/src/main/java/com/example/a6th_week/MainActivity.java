package com.example.a6th_week;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//import com.example.temisdk.temi.CustomTtsListener;
//import com.example.temisdk.temi.RoboTemi;
//import com.example.temisdk.temi.RoboTemiListeners;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements
        OnRobotReadyListener,
        View.OnClickListener {

    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Robot robot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        robot = Robot.getInstance();

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        robot.addOnRobotReadyListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        robot.removeOnRobotReadyListener(this);
    }


    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            try {
                final ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
                robot.onStart(activityInfo);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onClick(View view) {
        Class exampleContext = null;
        int id = view.getId();
        if (id == R.id.button1) {
            exampleContext = MainActivity1.class;
        } else if (id == R.id.button2) {
            exampleContext = MainActivity2.class;
        } else if (id == R.id.button3) {
            exampleContext = MainActivity3.class;
        }else if (id == R.id.button4) {
            exampleContext = MainActivity4.class;
        }
        Intent intent = new Intent(getApplicationContext(),exampleContext);
        startActivity(intent);
    }
}
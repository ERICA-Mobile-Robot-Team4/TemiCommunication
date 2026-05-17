package com.example.a6th_week;


import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a6th_week.main2act.Main2ActivityDialogAdapter;
import com.example.a6th_week.temi.RoboTemiListeners;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity4 extends AppCompatActivity implements
        Robot.AsrListener,
        Robot.TtsListener,
        Robot.ConversationViewAttachesListener,
        Robot.WakeupWordListener{

    RoboTemiListeners roboTemiListeners;

    ArrayList<String> list;//테미에게 말한 내용을 기록할 리스트
    Main2ActivityDialogAdapter adapter;// 대화내용 기록을 위한 어답터
    ListView lv; // 대화내용 출력을 위한 어답터


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        roboTemiListeners = new RoboTemiListeners();// 각 액티비티 간 객체의 전달은 힘들다.


        list = new ArrayList<>();//새로운 리스트 객체를 추가
        adapter = new Main2ActivityDialogAdapter(this , list);
        lv = findViewById(R.id.dialoglist);
        lv.setAdapter(adapter);//리스트 뷰에 어답터 등록

        roboTemiListeners.getRobot().addAsrListener(this);//robot 객체는 현재 private 처리되어있기 때문에 참조하기 위해서 get을 사용하였다.
        //  ^---- 여기가 Robot 객체 ----^                    << 여긴 robot.addAsrListener(this);와 동일하다.
        roboTemiListeners.getRobot().addTtsListener(this);
        roboTemiListeners.getRobot().addWakeupWordListener(this);
        roboTemiListeners.getRobot().addConversationViewAttachesListenerListener(this);

        /*
        RoboTemiListeners는 RoboTemi의 조종하는 객체를 extend를 하였기 때문에,
        protected, public 조종 관련 코드를 사용할 수 있다.*/
        //roboTemiListeners.followMe();//앱의 화면이 켜진 직후 앞에 있는 사람을 따라온다.

    }

    public void stopAndSavePos(View view){// 정지하고 현재 위치를 저장
        EditText etxt = findViewById(R.id.editText1);//작성한 창
        roboTemiListeners.stopMovement();
        roboTemiListeners.saveLocation(etxt.getText().toString());
        etxt.setText("");//창 초기화
        if(etxt.getText().equals("back"))
            finish();//어플리케이션 닫기
    }

    public void back(View view) {
        finish();
    }


    @Override
    public void onAsrResult(@NotNull String s) {//무언가를 테미에게 말하면 기록한다.
        list.add("(사용자) : "+s);
        adapter.notifyDataSetChanged();//리스트 업데이트
        if (s.equals("닫기"))
            finish();
        if (s.equals("이리 와"))
            roboTemiListeners.followMe();
        if (s.equals("그만"))
            roboTemiListeners.stopMovement();
        if ( s.equals("회전") )
            roboTemiListeners.turnBy(90,3.0F);
        if (s.equals("직진"))
            roboTemiListeners.skidJoy(1,2);
        if (s.equals("1번")||s.equals("일번")) {
            roboTemiListeners.tiltAngle(55, 1);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            roboTemiListeners.tiltAngle(-25, 1);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            roboTemiListeners.tiltAngle(55, 1);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            roboTemiListeners.tiltAngle(-25, 1);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            roboTemiListeners.tiltAngle(20, 1);
        }
        if (s.equals("2번")||s.equals("이번")) {
            roboTemiListeners.turnBy(30,3.0F);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            roboTemiListeners.turnBy(-60,3.0F);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            roboTemiListeners.turnBy(60,3.0F);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            roboTemiListeners.turnBy(-30,3.0F);
        }

    }

    @Override
    public void onTtsStatusChanged(@NotNull TtsRequest ttsRequest) {
        String s = ttsRequest.getSpeech();
        list.add("(테미) : "+s);
        adapter.notifyDataSetChanged();//리스트 업데이트
    }

    @Override
    public void onConversationAttaches(boolean b) {
        list.add("(테미) : 대화중..");
        adapter.notifyDataSetChanged();//리스트 업데이트
    }

    @Override
    public void onWakeupWord(@NotNull String s, int i) {
        list.add("(테미 호출됨) : "+s);
        adapter.notifyDataSetChanged();//리스트 업데이트
    }

}

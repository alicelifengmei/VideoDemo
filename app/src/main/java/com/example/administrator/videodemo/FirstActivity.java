package com.example.administrator.videodemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.administrator.videodemo.universalvideoviewsample.UniversalVideoActivity;

/**
 * Created by lifengmei on 2016/11/10 17:23.
 */
public class FirstActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        Button btn = (Button) findViewById(R.id.viewvideo);
        Button btn0 = (Button) findViewById(R.id.mp);
        Button btn1 = (Button) findViewById(R.id.mp1);
        Button btn2 = (Button) findViewById(R.id.mp2);
        btn.setOnClickListener(this);
        btn0.setOnClickListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.videoView:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.mp:
                startActivity(new Intent(this, MediaPlayerActivity.class));

                break;
            case R.id.mp1:
                Intent intent1 = new Intent(this, UniversalVideoActivity.class);
                intent1.putExtra("from",1);
                startActivity(intent1);

                break;
            case R.id.mp2:
                Intent intent2 = new Intent(this, UniversalVideoActivity.class);
                intent2.putExtra("from",2);
                startActivity(intent2);
                break;
        }

    }
}

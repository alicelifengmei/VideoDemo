package com.example.administrator.videodemo;

import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()+"/test.mp4");
        VideoView vvVideo = (VideoView) findViewById(R.id.vv_video);
        vvVideo.setMediaController(new MediaController(this));
        String dataPath = "http://www.androidbook.com/akc/filestorage/android/documentfiles/3389/movie.mp4";
        Uri uri = Uri.parse(dataPath);
        vvVideo.setVideoURI(uri);
        vvVideo.start();
        vvVideo.requestFocus();

    }
}

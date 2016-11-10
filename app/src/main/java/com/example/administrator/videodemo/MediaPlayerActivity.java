package com.example.administrator.videodemo;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * //播放的点，
 * 1，播放，暂停，进度条，点击进度条切换到指定播放进度，后台释放。
 * 监听：准备，错误，进度
 * 权限：网络，weak_lock（播放时不黑屏？）黑屏时仍播放，慎重有必要时使用setWakeMode()
 * 资源类型，视频格式的支持
 * 2，横竖屏，断点续播
 * 3，缓存，缓冲，流畅，清晰
 * 4，后台播放 Service
 *
 * Created by lifengmei on 2016/11/1 15:36.
 */
public class MediaPlayerActivity extends Activity implements SurfaceHolder.Callback,
        MediaPlayer.OnCompletionListener,MediaPlayer.OnErrorListener,MediaPlayer.OnInfoListener,
        MediaPlayer.OnPreparedListener,MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnVideoSizeChangedListener,MediaPlayer.OnBufferingUpdateListener{


    private Display currDisplay;
    private MediaPlayer mediaPlayer;
    private SurfaceView svVedio;
    private SurfaceHolder sfHolder;
    private int vWidth,vHeight;
    private Button btnStartOrPause;
    private boolean isStart = false;
    private Timer mTimer = new Timer();
    private Handler handlerProgress = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int position = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();

            if (duration > 0) {
                long pos = skbProgress.getMax() * position / duration;
                skbProgress.setProgress((int) pos);
            }
        }
    };
    private SeekBar skbProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initSet();
    }
    private void initView() {
        setContentView(R.layout.activity_mediaplayer);
        svVedio = (SurfaceView) findViewById(R.id.sv_video);
        btnStartOrPause = (Button) findViewById(R.id.btn_start_or_pause);
        skbProgress = (SeekBar) findViewById(R.id.skbProgress);
        skbProgress.setOnSeekBarChangeListener(new SeekBarChangeEvent());
        btnStartOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isStart){
                    pause();
                    isStart = false;
                    btnStartOrPause.setText("开始");
                }else{
                    //在指定了MediaPlayer播放的容器后，我们就可以使用prepare或者prepareAsync来准备播放了
//                    String dataPath = Environment.getExternalStorageDirectory().getPath()+"/test.mp4";
//                    String dataPath = "http://www.androidbook.com/akc/filestorage/android/documentfiles/3389/movie.mp4";
                    String dataPath = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
                    Uri uri = Uri.parse(dataPath);
                    play(uri);
                    isStart = true;
                    btnStartOrPause.setText("暂停");
                }
            }
        });
    }

    private void initSet() {
        sfHolder = svVedio.getHolder();
        sfHolder.addCallback(this);
        sfHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mTimer.schedule(mTimerTask, 0, 1000);
    }

    public void play(Uri url){
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this,url);
            mediaPlayer.prepareAsync();//而不是使用prepare()方法//prepare之后播放,异步的不会阻塞UI线程，准备完毕后会调用onPrepared的方法，在那里播放
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pause()
    {
        mediaPlayer.pause();
    }

    public void stop()
    {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
    }

    /*******************************************************
     * 通过定时器和Handler来更新进度条
     ******************************************************/
    TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            if(mediaPlayer==null)
                return;
            if (mediaPlayer.isPlaying() && skbProgress.isPressed() == false) {
                handlerProgress.sendEmptyMessage(0);
            }
        }
    };
    class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {
        int progress;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            // 原本是(progress/seekBar.getMax())*player.mediaPlayer.getDuration()
            this.progress = progress * mediaPlayer.getDuration()
                    / seekBar.getMax();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // seekTo()的参数是相对与影片时间的数字，而不是与seekBar.getMax()相对的数字
            mediaPlayer.seekTo(progress);
        }
    }
    //SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // 当SurfaceView中的Surface被创建的时候被调用
        //在这里我们指定MediaPlayer在当前的Surface中进行播放

        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setOnCompletionListener(this);
//        mediaPlayer.setOnErrorListener(this);
//        mediaPlayer.setOnInfoListener(this);
//        mediaPlayer.setOnSeekCompleteListener(this);
//        mediaPlayer.setOnVideoSizeChangedListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);//TODO 慎重使用
        mediaPlayer.setDisplay(surfaceHolder);

        //然后，我们取得当前Display对象
        currDisplay = this.getWindowManager().getDefaultDisplay();

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    //MediaPlayer.OnCompletionListener
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        // 当MediaPlayer播放完成后触发
        Log.v("Play Over:::", "onComletion called");
        this.finish();
    }

    //MediaPlayer.OnErrorListener
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int whatError, int extra) {
        Log.v("Play Error:::", "onError called");
        switch (whatError) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.v("Play Error:::", "MEDIA_ERROR_SERVER_DIED");
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.v("Play Error:::", "MEDIA_ERROR_UNKNOWN");
                break;
            default:
                break;
        }
        return false;
    }

    //MediaPlayer.OnInfoListener
    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int whatInfo, int extra) {
        // 当一些特定信息出现或者警告时触发
        switch(whatInfo){
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                break;
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                break;
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                break;
        }
        return false;
    }

    //MediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        // 当prepare完成后，该方法触发，在这里我们播放视频

        //首先取得video的宽和高
        vWidth = mediaPlayer.getVideoWidth();
        vHeight = mediaPlayer.getVideoHeight();
//        if (vWidth == 0 && vHeight == 0) {
//            Toast.makeText(this,"该视频不能播放",Toast.LENGTH_SHORT).show();
//            return;
//        }
        if(vWidth > currDisplay.getWidth() || vHeight > currDisplay.getHeight()){
            //如果video的宽或者高超出了当前屏幕的大小，则要进行缩放
            float wRatio = (float)vWidth/(float)currDisplay.getWidth();
            float hRatio = (float)vHeight/(float)currDisplay.getHeight();

            //选择大的一个进行缩放
            float ratio = Math.max(wRatio, hRatio);

            vWidth = (int)Math.ceil((float)vWidth/ratio);
            vHeight = (int)Math.ceil((float)vHeight/ratio);

            //设置surfaceView的布局参数
            svVedio.setLayoutParams(new LinearLayout.LayoutParams(vWidth, vHeight));
        }
        //开始播放
        mediaPlayer.start();
    }

    //MediaPlayer.OnSeekCompleteListener
    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    //MediaPlayer.OnVideoSizeChangedListener
    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {

    }

    //MediaPlayer.OnBufferingUpdateListener
    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int bufferingProgress) {
        skbProgress.setSecondaryProgress(bufferingProgress);
        int currentProgress=skbProgress.getMax()*mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration();
        Log.e(currentProgress+"% play", bufferingProgress + "% buffer");
    }
}

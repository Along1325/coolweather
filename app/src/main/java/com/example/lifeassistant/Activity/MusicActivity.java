package com.example.lifeassistant.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lifeassistant.R;
import com.example.lifeassistant.util.ActivityCollector;
import com.example.lifeassistant.util.MusicAdapter;
import com.example.lifeassistant.util.MusicName;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView musicText;//用于显示歌名

    private List<MusicName> musicList =  new ArrayList<>();//获取歌曲的列表
    private MediaPlayer player = new MediaPlayer();
    private String filePath = "Music/";

    private Handler handler;
    private String musicNameHandle;//接收传递回来的信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_music);

        Button home = (Button) findViewById(R.id.nav_button);
        musicText = (TextView) findViewById(R.id.title_city);
        Button play = (Button) findViewById(R.id.play);
        Button pause = (Button) findViewById(R.id.pause);
        Button stop = (Button) findViewById(R.id.stop);
        TextView exit = (TextView) findViewById(R.id.title_update_time);

        musicText.setText("音乐播放器");
        exit.setText("退出");

        if (ContextCompat.checkSelfPermission(MusicActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MusicActivity.this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        } else {
            getMusicFile();
            getName();
        }

        /**
         * 获取自定义适配器
         */
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.music_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        MusicAdapter adapter = new MusicAdapter(musicList,handler);
        recyclerView.setAdapter(adapter);

        home.setOnClickListener(this);
        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        exit.setOnClickListener(this);
    }

    /**
     * 读取sd卡上指定目录下的歌曲名
     * @return
     */
    public void getMusicFile() {

        File f = new File(Environment.getExternalStorageDirectory(),"Music");
        if (!f.exists()) {
            Log.d("MusicActivityLog","文件夹不存在");
        }

        int i = 0;
        File[] subFiles = f.listFiles();
        for (File subFile : subFiles) {
            MusicName musicName = new MusicName(subFile.getName());//获取歌曲名
            musicList.add(musicName);//添加到列表上
        }

    }

    /**
     * 传入歌曲名，初始化MediaPlayer
     * @param musicName
     */
    private void initMediaPlayer(String musicName) {

        try {
            if (player.isPlaying()){
                player.stop();
                player = new MediaPlayer();
            }
            File file = new File(Environment.getExternalStorageDirectory(),filePath+musicName);
            player.setDataSource(file.getPath());//指定文件的路径
            player.prepare();//让MediaPlayer进入准备状态
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 权限处理
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getMusicFile();
                    getName();
                } else {
                    Toast.makeText(this,"拒绝权限将无法使用程序",Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        if (player != null) {
            player.stop();
            player.release();
        }
    }

    private void getName(){
        handler = new Handler(){
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        musicNameHandle = null;
                        musicNameHandle = msg.getData().getString("musicName");
                        musicText.setText(musicNameHandle);
                        initMediaPlayer(musicNameHandle);
                        if (!player.isPlaying()) {
                            player.start();//开始播放
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_button:
                Intent intent = new Intent(MusicActivity.this,WeatherActivity.class);
                startActivity(intent);
                break;
            case R.id.play:
                if (!player.isPlaying()) {
                    player.start();
                }
                break;
            case R.id.pause:
                if (player.isPlaying()) {
                    player.pause();//暂停播放
                }
                break;
            case R.id.stop:
                if (player.isPlaying()) {
                    player.reset();//停止播放
                    initMediaPlayer(musicNameHandle);
                }
                break;
            case R.id.title_update_time:
                ActivityCollector.finishAll();
                break;
            default:
                break;
        }
    }
}

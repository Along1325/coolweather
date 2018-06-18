package com.example.lifeassistant.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.lifeassistant.R;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        * 先从SharedPreferences文件中读取缓存数据
        * 如果不为null就说明之前已经请求过天气数据，没必要再让用户再次选择城市，而是直接跳转到WeatherActivity
        * 否则，跳转到ChooseActivity选择城市
        * */
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString("weather",null) != null) {
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this,ChooseActivity.class);
            intent.putExtra("code","main");
            startActivity(intent);
            finish();
        }
    }
}

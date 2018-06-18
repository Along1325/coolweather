package com.example.lifeassistant.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lifeassistant.R;
import com.example.lifeassistant.random.JokeColorRandom;
import com.example.lifeassistant.random.JokePageRandom;
import com.example.lifeassistant.util.ActivityCollector;
import com.example.lifeassistant.util.HttpUtil;
import com.example.lifeassistant.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class JokeActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 随机页数
     */
    private JokePageRandom PageRandom;
    private int page;
    private JokeColorRandom colorRandom;
    private int color;

    /**
     * 定义各个控件
     */
    public SwipeRefreshLayout jokeSwipeRefresh;
    private ScrollView jokeLayout;
    private TextView jokeText;
    private LinearLayout jokeContent;
    private ImageView jokeBingImage;
    private Button navButton;
    private TextView exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_joke);
        //初始化控件
        jokeSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.joke_swipe_refresh);
        jokeSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        jokeLayout = (ScrollView) findViewById(R.id.joke_layout);
        jokeText = (TextView) findViewById(R.id.title_city);
        jokeContent = (LinearLayout) findViewById(R.id.joke_List);
        jokeBingImage = (ImageView) findViewById(R.id.joke_bing_img);
        navButton = (Button) findViewById(R.id.nav_button);
        exit = (TextView) findViewById(R.id.title_update_time);

        navButton.setOnClickListener(this);
        exit.setText("退出");
        exit.setOnClickListener(this);

        SharedPreferences jokePrefs = PreferenceManager.getDefaultSharedPreferences(this);

        jokeText.setText("笑话大全");
        /**
         * 初始化页数
         */
        PageRandom = new JokePageRandom(page);
        page = PageRandom.getPage();

        /**
         * 初始化颜色
         */
        colorRandom = new JokeColorRandom(color);
        /**
         * 刷新笑话页数
         */
        jokeSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                PageRandom.setPage(page);
                page = PageRandom.getPage();
                requestJoke(page);
            }
        });

        //获取笑话
        requestJoke(page);

        String jokeBingPic = jokePrefs.getString("joke_bing_pic",null);
        if (jokeBingPic != null) {
            Glide.with(this).load(jokeBingPic).into(jokeBingImage);
        } else {
            loadJokeBingPic();
        }

    }

    /**
     * 请求笑话信息
     */
    private void requestJoke(int page) {
        String jokeUrl = "http://japi.juhe.cn/joke/content/list.from?key=5def38c9e920bade32edd9a6d869a345&page="+page +"&pagesize=20&sort=asc&time=1418745237";
        HttpUtil.sendOkHttpRequest(jokeUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(JokeActivity.this,"获取笑话失败",Toast.LENGTH_SHORT).show();
                        jokeSwipeRefresh.setRefreshing(false);
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseText = response.body().string();
                final JSONArray joke = Utility.handleRootResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (joke != null) {
                            showJokeInfo(joke);//显示笑话列表
                        } else {
                            Toast.makeText(JokeActivity.this,"获取笑话失败",Toast.LENGTH_SHORT).show();
                        }
                        jokeSwipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        //获取图片
        loadJokeBingPic();
    }

    private void showJokeInfo(JSONArray joke) {

        jokeContent.removeAllViews();
        for (int i = 0;i < joke.length();i++) {
            try {
                JSONObject jsonObject = (JSONObject) joke.get(i);
                View view = LayoutInflater.from(this).inflate(R.layout.joke_text_item,jokeContent,false);
                TextView dataText = (TextView) view.findViewById(R.id.data_text);
                dataText.setText(jsonObject.getString("content").toString());//将解析好的字符串显示到界面
                colorRandom.setSelectColor(color);
                color = colorRandom.getSelectColor();
                dataText.setTextColor(color);//设置字体颜色
                TextView dataTime = (TextView) view.findViewById(R.id.data_time);
                dataTime.setText(jsonObject.getString("updatetime"));//显示更新时间
                ImageView imageView = (ImageView) findViewById(R.id.joke_image_view);//分割线
                jokeContent.addView(view);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        jokeLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 获取图片
     */
    private void loadJokeBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String jokeBingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(JokeActivity.this).edit();
                editor.putString("joke_bing_pic",jokeBingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(JokeActivity.this).load(jokeBingPic).into(jokeBingImage);
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_button:
                Intent intent = new Intent(JokeActivity.this,WeatherActivity.class);
                startActivity(intent);
                break;
            case R.id.title_update_time:
                ActivityCollector.finishAll();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}

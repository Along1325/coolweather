package com.example.lifeassistant.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lifeassistant.R;
import com.example.lifeassistant.gson.Forecast;
import com.example.lifeassistant.gson.Weather;
import com.example.lifeassistant.service.AutoUpdateService;
import com.example.lifeassistant.util.ActivityCollector;
import com.example.lifeassistant.util.HttpUtil;
import com.example.lifeassistant.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {
    //滑动控件
    private ScrollView weatherLayout;

    /**
     * 显示天气信息的一些控件
     */
    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;//图片控件——每日一图

    public SwipeRefreshLayout swipeRefresh;//下拉刷新控件
    public String mWeatherId;//天气的id

    public DrawerLayout mDrawerLayout;//侧边栏
    private NavigationView navView;//侧边栏页面
    private Button homeBt;//按钮——点击出现侧边栏

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);//活动管理器
        /**
         * 做一个系统版本号的判断
         * 只有版本号大于或等于21，才会执行后面的代码
         */
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        //初始化各控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navView = (NavigationView) findViewById(R.id.nav_view);
        homeBt = (Button) findViewById(R.id.nav_button);
        navView.setCheckedItem(R.id.web_item);

        /**
         * 设置监听器
         */
        titleCity.setOnClickListener(this);//选择城市
        homeBt.setOnClickListener(this);//侧边栏
        titleUpdateTime.setOnClickListener(this);//退出程序

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);//读取缓存数据
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;//获取天气的id
            showWeatherInfo(weather);
        } else {
                //无缓存时去服务器查询天气
                mWeatherId = getIntent().getStringExtra("weather_id");
                weatherLayout.setVisibility(View.INVISIBLE);//现将ScrollView隐藏
                requestWeather(mWeatherId);
            }

        /**
         * 刷新天气
         */
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWeatherId = getIntent().getStringExtra("weather_id");
                requestWeather(mWeatherId);
            }
        });

        /**
         * 获取每日一图
         */
        String bingPic = prefs.getString("bing_pic",null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }

        /**
         * 侧边栏的选择菜单监听
         */
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.login_item:
                        intent = new Intent(WeatherActivity.this,Login.class);
                        startActivity(intent);
                        break;
                    case R.id.web_item:
                        intent = new Intent(WeatherActivity.this,WebActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.music_item:
                        intent = new Intent(WeatherActivity.this,MusicActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.joke_item:
                        intent = new Intent(WeatherActivity.this,JokeActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.weather_item:
                        mDrawerLayout.closeDrawers();
                        break;
                    case R.id.exit_item:
                        ActivityCollector.finishAll();
                        break;
                    default:
                        break;
                }
                mDrawerLayout.closeDrawers();//关闭侧边栏
                return true;
            }
        });

    }


    /*
    * 根据天气id请求城市天气信息
    * */
    public void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=c8a8d341b4db4064886597a5f4322493";
        this.mWeatherId = weatherId;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status) ) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);//将天气数据存储
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        //用于表示刷新时间结束，并隐藏刷新进度条
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /*
    * 处理并且展示Weather实体类中的数据
    * */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "°C";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.weather_forecast_item,forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }

        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        /**
         * 后台服务
         */
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /*
    * 加载必应每日一图
    * */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_city:
                Intent intent = new Intent(WeatherActivity.this,ChooseActivity.class);
                intent.putExtra("code","weather");
                startActivity(intent);
                break;
            case R.id.nav_button:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.title_update_time:
                ActivityCollector.finishAll();
                break;
        }
    }
}

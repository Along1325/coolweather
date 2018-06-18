package com.example.lifeassistant.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lifeassistant.R;
import com.example.lifeassistant.db.City;
import com.example.lifeassistant.db.County;
import com.example.lifeassistant.db.Province;
import com.example.lifeassistant.util.ActivityCollector;
import com.example.lifeassistant.util.HttpUtil;
import com.example.lifeassistant.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseActivity extends AppCompatActivity {

    /**
     * 设置选择的类型参数，用于判断选中的是:省、市、县
     */
    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    //进度条
    private ProgressDialog progressDialog;

    /**
     * 标题栏控件
     */
    private TextView titleText;

    private Button backButton;

    private ListView listView;

    /**
     *适配器，用于显示列表
     */
    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_weather_choose);

        /**
         * 获取控件的实例
         */
        titleText = (TextView) findViewById(R.id.title_text);
        backButton = (Button) findViewById(R.id.back_button);
        listView = (ListView) findViewById(R.id.list_view);

        /**
         * 初始化ArrayAdapter
         */
        adapter = new ArrayAdapter<>(ChooseActivity.this,android.R.layout.simple_expandable_list_item_1,dataList);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);//获取省的id
                    queryCities();//查询市
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);//获取市的id
                    queryCounties();//查询县
                } else if (currentLevel == LEVEL_COUNTY) {
                    //当选中的级别为县时，根据当前的id查询天气数据
                    String weatherId = countyList.get(position).getWeatherId();
                    Intent intent = new Intent(ChooseActivity.this,WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);//将天气id传递到WeatherActivity中
                    startActivity(intent);
                }
            }
        });

        /**
         * 返回按钮
         * 监听
         */
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {
                    //返回查询城市列表
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    //返回查询省列表
                    queryProvinces();
                }
            }
        });
        //默认查询省
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到，再去服务器上查询
     */

    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);//将返回按钮隐藏
        provinceList = DataSupport.findAll(Province.class);//查询数据库
        if (provinceList.size() > 0) {
            dataList.clear();//清空dataList上存在的数据
            /**
             * 遍历数据
             */
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();//动态更新listView
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;//设置选中的级别为省
        } else {
            /**
             * 没有缓存时到服务器上查询
             */
            String address = "http://guolin.tech/api/china";//传入的网址
            queryFromServer(address,"province");//传入网址和类型
        }
    }

    /**
     * 查询省内所有的城市，优先从数据库查询，如果没有查询到，再去服务器上查询
     */

    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());//根据获得的selectedProvince获取选择的省的名字
        backButton.setVisibility(View.VISIBLE);//设置为可用
        //从数据库查询市
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);

        if (cityList.size() > 0) {
            dataList.clear();//清空listView上的数据
            for (City city : cityList) {
                dataList.add(city.getCityName());//获取市的名字
            }
            adapter.notifyDataSetChanged();//更新listView
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;//设置选中的级别为市
        } else {
            /**
             * 数据库中没有数据时，从服务器进行查询
             */
            int provinceCode = selectedProvince.getProvinceCode();//获取省的id，用于查询该省的所有市
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address,"city");//查询
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到，再去服务器上查询
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);//设置为可用
        countyList = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address,"county");
        }
    }

    /**
     * 将传入的地址和类型从服务器上查询省市县数据
     */

    private void queryFromServer(String address, final String type) {

        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                //通过runOnUiThread()方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseText = response.body().string();
                boolean result = false;

                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }

                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });

    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(ChooseActivity.this);
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }

        progressDialog.show();

    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}

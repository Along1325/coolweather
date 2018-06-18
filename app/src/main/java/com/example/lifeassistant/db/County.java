package com.example.lifeassistant.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/6/2 0002.
 */

public class County extends DataSupport {

    private int id;

    private String countyName;//记录当前县的名字

    private String weatherId;//记录当前所对应的天气id

    private int cityId;//记录当前县所属市的id

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

}

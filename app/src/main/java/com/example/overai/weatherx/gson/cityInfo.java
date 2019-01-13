package com.example.overai.weatherx.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by overAI on 2018/12/20.
 */


public class cityInfo {
    @SerializedName("city")
    public String cityname;

    @SerializedName("cityId")
    public String cityId;

    @SerializedName("parent")
    public String parent;

    @SerializedName("updateTime")
    public String time;
}

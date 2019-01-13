package com.example.overai.weatherx.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by overAI on 2018/12/19.
 */

public class data {
    @SerializedName("shidu")
    public String shidu;

    @SerializedName("pm25")
    public String pm25;

    @SerializedName("wendu")
    public String wendu;
}

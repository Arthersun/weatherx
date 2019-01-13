package com.example.overai.weatherx.util;

import com.example.overai.weatherx.gson.weather;
import com.google.gson.Gson;

/**
 * Created by overAI on 2018/12/19.
 */

public class Utility {
    public static weather handleWeatherRespnse(String response){
        try{
            return new Gson().fromJson(response,weather.class);

        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

}

package com.example.overai.weatherx;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.overai.weatherx.gson.weather;
import com.example.overai.weatherx.util.HttpUtil;
import com.example.overai.weatherx.util.Utility;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class Weatherx extends AppCompatActivity {


    private TextView title_city;
    private TextView title_update_time;
    private TextView wendu;
    private TextView shidu;
    private TextView pm252;
    private TextView date1;
    private Button refr;
    private Button favor1;
    private List<String> favorlist = new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ShiActivity.instance!=null)            //如果adddairy活动正在进行，就把添加活动摧毁！
        {
            ShiActivity.instance.finish();
        }
        if (MainActivity.instance!=null)            //如果adddairy活动正在进行，就把主活动摧毁！
        {
            MainActivity.instance.finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weatherx);
        title_city = (TextView)findViewById(R.id.title_city);
        wendu=(TextView)findViewById(R.id.wendu);
        shidu = (TextView)findViewById(R.id.shidu);
        pm252 = (TextView)findViewById(R.id.pm252);
        date1 = (TextView)findViewById(R.id.date1);
        title_update_time = (TextView)findViewById(R.id.title_update_time);
        SharedPreferences prefs= getSharedPreferences("cityinfo",MODE_PRIVATE);
        final String weatherid = getIntent().getStringExtra("weather_id");
        String weatherString = prefs.getString(weatherid,null);

        if (weatherString!=null)    //如果传进来的weather_id在sharedprefs中有值，则直接解析这个值
        {
            weather weather = Utility.handleWeatherRespnse(weatherString);
            showWeatherInfo(weather);
        }
        else {                      //如果没有缓存，则去服务器查询天气
            requestWeather(weatherid);
        }
        SharedPreferences pref = getSharedPreferences("favorcity",MODE_PRIVATE);
        String favor = pref.getString("favorcity","");
        String[] aa = favor.split(",");
        favor1 = (Button)findViewById(R.id.favor);
        for (int i=0;i<aa.length ;i++)
        {
            if(weatherid.equals(aa[i]))
            {
                favor1.setText("取消关注");
            }
        }
        favor1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean fav = false;
                String Text = favor1.getText().toString();
                if (Text.equals("取消关注")) {
                    favor1.setText("设为关注");
                }
                if (Text.equals("设为关注")) {
                    favor1.setText("取消关注");
                    fav = true;
                }
                SharedPreferences pref = getSharedPreferences("favorcity",MODE_PRIVATE);
                String favor = pref.getString("favorcity","");
                String[] aa = favor.split(",");
                SharedPreferences.Editor editor = getSharedPreferences("favorcity", MODE_PRIVATE).edit();
                for (int i=0;i<aa.length ;i++)
                {
                    favorlist.add(aa[i]);
                }
                if (fav) {
                    editor.clear();
                    String favor2 = favor + ',' + weatherid;
                    editor.putString("favorcity", favor2);
                    editor.apply();
                }
                else{
                    int flag=0;
                    for (int i=0;i<aa.length ;i++)
                    {
                        if(weatherid.equals(aa[i]))
                        {
                            flag=i;
                        }
                    }
                    favorlist.remove(flag);
                    favorlist.remove(0);
                    String a;
                    String b="";
                    for (String favor1:favorlist)
                    {
                        a=','+favor1;
                        b=b+a;
                    }
                    editor.clear();
                    editor.putString("favorcity", b);
                    editor.apply();
                }
                favorlist.clear();
            }
        });

        refr = (Button)findViewById(R.id.refr);
        //点刷新按钮会请求数据
        refr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog =new ProgressDialog(Weatherx.this);
                progressDialog.setTitle("在线数据查询中");
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(true);
                progressDialog.show();
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                    requestWeather(weatherid);
                        try {
                            Thread.sleep(1000);//让他显示2秒后，取消ProgressDialog
                        } catch (InterruptedException e) {
// TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
                    }
                });
                t.start();
            }
        });
    }

    public void requestWeather(final String weatherId){
//        String a;
        String weatherUrl = "http://t.weather.sojson.com/api/weather/city/"+weatherId;
//        String weatherUrl = "http://t.weather.sojson.com/api/weather/city/101030100";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {

            @Override
            public void onResponse(Call call,Response response) throws IOException {
                final String responseText = response.body().string();
                final weather weather = Utility.handleWeatherRespnse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null){
                            if (weather.status==200) {
                                SharedPreferences.Editor editor = getSharedPreferences("cityinfo",MODE_PRIVATE).edit();
                                editor.putString(weather.cityInfo.cityId, responseText);
                                editor.apply();
                                showWeatherInfo(weather);
                            }
                            else {
                                Intent intent = new Intent(Weatherx.this,MainActivity.class);
                                intent.putExtra("iderror",1);
                                startActivity(intent);

                            }
                        }
                        else {
                            Toast.makeText(Weatherx.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Weatherx.this,"获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });

    }

    public void onBackPressed() {
        super.onBackPressed();//注释掉这行,back键不退出activity
        Intent intent = new Intent(Weatherx.this,MainActivity.class);
        startActivity(intent);
    }

    private void showWeatherInfo(weather weather){
        String cityname = weather.cityInfo.cityname;
        String parent = weather.cityInfo.parent;
        String updateTime = weather.cityInfo.time;
        String shidu1 = weather.data.shidu;
        String wendu1 = weather.data.wendu+"C";
        String date = weather.date;
        date1.setText(date);
        title_update_time.setText("数据更新时间："+updateTime);
        title_city.setText(parent+cityname);
        shidu.setText("湿度："+shidu1);
        wendu.setText("温度："+wendu1);

        String pm251 = "无";
        try{
            pm251 = weather.data.pm25;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            pm252.setText("pm25："+pm251);
            Log.d("cityname",cityname);
            Log.d("time",updateTime);
            Log.d("shidu1",shidu1);
            Log.d("wendu1",wendu1);
        }





    }
}


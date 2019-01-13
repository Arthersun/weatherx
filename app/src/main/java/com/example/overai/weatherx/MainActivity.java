package com.example.overai.weatherx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.overai.weatherx.gson.weather;
import com.example.overai.weatherx.util.HttpUtil;
import com.example.overai.weatherx.util.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static MainActivity instance;
    public static List<String> pcitys = new ArrayList();
    public ListView listView;
    public Button select;
    public EditText cityid;
    public ListView favorcity;
    public static List<String> favorlist = new ArrayList();
    public static List<String> favornamelist = new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pcitys.clear();

        select = (Button) findViewById(R.id.select);

        //无论 JSON 来自本地还是网络获取，都要先将 JSON 转成 String ；
        //需要一个 JSON 解析类对象将JSON的字符串转成 JsonArray ，前提是我们知道 JSON 中只有纯数组；
        //循环遍历 JsonArray ，并用 GSON 解析成相应的对象。

        sendRequestWithOkHttp();

        listView  = (ListView)findViewById(R.id.citylist);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this,ShiActivity.class);
                intent.putExtra("pid",i+1);
                startActivity(intent);
            }
        });

        cityid = (EditText)findViewById(R.id.cityid);
        cityid.setHint("输入城市id进行查询");

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String cityids = cityid.getText().toString();
                if (cityids.length()!=9)
                    Toast.makeText(MainActivity.this,"城市id非法,请检查位数",Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent(MainActivity.this, Weatherx.class);
                    intent.putExtra("weather_id", cityids);
                    startActivity(intent);
                }
            }
        });
        if (getIntent().getIntExtra("iderror",0)==1)
        {
            Toast.makeText(MainActivity.this,"不存在此城市id,请重新输入",Toast.LENGTH_SHORT).show();
        }
        favorcity = (ListView)findViewById(R.id.favorcity);
        SharedPreferences pref = getSharedPreferences("favorcity",MODE_PRIVATE);
        String favor = pref.getString("favorcity","");
        String[] aa = favor.split(",");
        favorlist.clear();
        favornamelist.clear();
        for (int i = 0 ; i <aa.length ; i++) {
            favorlist.add(aa[i]);
        }
        SharedPreferences pre= getSharedPreferences("cityinfo",MODE_PRIVATE);
        favorlist.remove(0);            //把“”去掉
        for (String favorcode:favorlist)       //检查，写入缓存
        {

            if (pre.getString(favorcode,"").equals(""))
                requestfavorname(favorcode);        //怀疑是request的原因，可以改为prefs试一下
        }

        for (String favorcode:favorlist)       //读出缓存中的数据，然后写城市信息。
        {
            final String responseText = pre.getString(favorcode,"");    //从缓存中读输数据，这样不会出现request响应慢的问题
            final weather weather = Utility.handleWeatherRespnse(responseText);
            favornamelist.add(weather.cityInfo.cityname);
        }

        ArrayAdapter<String> adapter =new ArrayAdapter<String>(
                MainActivity.this,android.R.layout.simple_list_item_1,favornamelist);

        favorcity.setAdapter(adapter);

        favorcity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String a = favorlist.get(i);
                Intent intent = new Intent(MainActivity.this,Weatherx.class);
                intent.putExtra("weather_id",a);
                startActivity(intent);
            }
        });
    }

    private void sendRequestWithOkHttp()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //这个地方有一个缺点，就是city.json没有存在本地，其实可以访问一次存在本地缓存中的。
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url("http://10.0.2.2/city.json").build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    parseJSONWithJSONObject(responseData);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJSONWithJSONObject(String jsonData){
        try{
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i=0;i<jsonArray.length();i++)
            {
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                int id = jsonObject.getInt("id");
                int pid = jsonObject.getInt("pid");
                String cityname = jsonObject.getString("city_name");
                String citycode = jsonObject.getString("city_code");
                if (pid==0)
                {
                    pcitys.add(cityname);
                }
            }
            Message message = new Message();
            message.obj = pcitys;
            handler.sendMessage(message);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            ArrayAdapter<String> adapter =new ArrayAdapter<String>(
                    MainActivity.this,android.R.layout.simple_list_item_1,pcitys);
            Log.d("citys",pcitys.toString());

            listView.setAdapter(adapter);
//            listview单击进入编辑查看页面
            }
        };


    //用于把关注列表中有，但是缓存中没有的城市信息写入缓存
    private void requestfavorname(String favorcode)
    {
        final SharedPreferences city = getSharedPreferences("favorcity",MODE_PRIVATE);

//        String a;
        final String weatherUrl = "http://t.weather.sojson.com/api/weather/city/"+favorcode;
//        String weatherUrl = "http://t.weather.sojson.com/api/weather/city/101030100";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final weather weather = Utility.handleWeatherRespnse(responseText);
                SharedPreferences.Editor editor = getSharedPreferences("cityinfo",MODE_PRIVATE).edit();
                editor.putString(weather.cityInfo.cityId, responseText);
                editor.apply();
                }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });

    }
}





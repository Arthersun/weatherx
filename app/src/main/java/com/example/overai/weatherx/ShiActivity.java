package com.example.overai.weatherx;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShiActivity extends AppCompatActivity {

    public static ShiActivity instance;
    public static List<String> codes =  new ArrayList();
    public static List<String> citynames = new ArrayList();
    public ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shi);
        sendRequestWithOkHttp();
    }

    private void sendRequestWithOkHttp()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

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

    public void onBackPressed() {
        codes.clear();
        citynames.clear();
         super.onBackPressed();//注释掉这行,back键不退出activity
    }

    private void parseJSONWithJSONObject(String jsonData){
        try{
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i=0;i<jsonArray.length();i++)
            {
                int parent = getIntent().getIntExtra("pid",0);
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                int id = jsonObject.getInt("id");
                int pid = jsonObject.getInt("pid");
                String cityname = jsonObject.getString("city_name");
                String citycode = jsonObject.getString("city_code");
                if (pid==parent||(citycode.length()==9&&id==parent))
                {
                    codes.add(citycode);
                    citynames.add(cityname);
                }
            }
            Message message = new Message();
            message.obj = codes;
            message.obj = citynames;
            handler.sendMessage(message);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            ArrayAdapter<String> adapter =new ArrayAdapter<String>(
                    ShiActivity.this,android.R.layout.simple_list_item_1,citynames);
            listView  = (ListView)findViewById(R.id.citylist);
            listView.setAdapter(adapter);
//            listview单击进入编辑查看页面
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(ShiActivity.this,Weatherx.class);
                    String code = codes.get(i);
                    intent.putExtra("weather_id",code);
                    codes.clear();
                    citynames.clear();
                    startActivity(intent);
                }
            });
        }
    };




}






package com.example.wenboyin.cityguide;

import android.app.Activity;
import android.content.Intent;
import android.media.ExifInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.wenboyin.cityguide.R;

import java.io.IOException;


public class PotoInfoActivity extends Activity {

    private String path;
    private EditText changdu;
    private EditText kuandu;
    private EditText tupianjiaodu;
    private EditText jingdu;
    private EditText weidu;
    private EditText haibagaodu;
    private Button btSaveInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_info);

        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        //Toast.makeText(PotoInfoActivity.this,path,Toast.LENGTH_LONG).show();


        //Control initialization
        initView();

        getInfo(path);

        //Toast.makeText(PotoInfoActivity.this,path,Toast.LENGTH_LONG).show();

    }

    private void initView() {
        kuandu = (EditText) findViewById(R.id.kuandu);
        changdu = (EditText) findViewById(R.id.changdu);
        tupianjiaodu = (EditText) findViewById(R.id.jiaodu);
        jingdu = (EditText) findViewById(R.id.jingdu);
        weidu = (EditText) findViewById(R.id.weidu);
        haibagaodu = (EditText) findViewById(R.id.haiba_gaodu);
        btSaveInfo = (Button) findViewById(R.id.bt_saveinfo);
        btSaveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    //Save information
                    ExifInterface exifInterface = new ExifInterface(path);
                    exifInterface.setAttribute(ExifInterface.TAG_IMAGE_WIDTH,String.valueOf(kuandu.getText()));
                    exifInterface.setAttribute(ExifInterface.TAG_IMAGE_LENGTH,String.valueOf(changdu.getText()));
                    exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION,String.valueOf(tupianjiaodu.getText()));
                    exifInterface.setAttribute(ExifInterface.TAG_GPS_ALTITUDE,String.valueOf(jingdu.getText()));
                    exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE,String.valueOf(weidu.getText()));
                    exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,String.valueOf(haibagaodu.getText()));
                    exifInterface.saveAttributes();
                    Toast.makeText(PotoInfoActivity.this,"保存成功",Toast.LENGTH_LONG).show();
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void getInfo(String path) {
        //Toast.makeText(PotoInfoActivity.this,"1",Toast.LENGTH_LONG).show();
        try {
            //Toast.makeText(PotoInfoActivity.this,"2",Toast.LENGTH_LONG).show();
            ExifInterface exifInterface = new ExifInterface(path);
            String guangquan = exifInterface.getAttribute(ExifInterface.TAG_APERTURE);
            String shijain = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            String baoguangshijian = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
            String jiaoju = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
            String chang = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            String kuan = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
            String moshi = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
            String zhizaoshang = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
            String iso = exifInterface.getAttribute(ExifInterface.TAG_ISO);
            String jiaodu = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
            String baiph = exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
            String altitude_ref = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF);
            String altitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
            String latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String latitude_ref = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String longitude_ref = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            String longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String timestamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
            String processing_method = exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);

            //Toast.makeText(PotoInfoActivity.this,kuan,Toast.LENGTH_LONG).show();


            // Convert latitude and longitude format
            double lat = score2dimensionality(latitude);
            double lon = score2dimensionality(longitude);


            kuandu.setText(kuan);
            changdu.setText(chang);
            tupianjiaodu.setText(jiaodu);
            jingdu.setText(String.valueOf(lat));
            weidu.setText(String.valueOf(lon));
            haibagaodu.setText(altitude_ref);

//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append("光圈 = " + guangquan + "\n").append("时间 = " + shijain + "\n")
//                    .append("曝光时长 = " + baoguangshijian + "\n").append("焦距 = " + jiaoju + "\n")
//                    .append("长 = " + chang + "\n").append("宽 = " + kuan + "\n").append("型号 = " + moshi + "\n")
//                    .append("制造商 = " + zhizaoshang + "\n").append("ISO = " + iso + "\n").append("角度 = " + jiaodu + "\n")
//                    .append("白平衡 = " + baiph + "\n").append("海拔高度 = " + altitude_ref + "\n")
//                    .append("GPS参考高度 = " + altitude + "\n").append("GPS时间戳 = " + timestamp + "\n")
//                    .append("GPS定位类型 = " + processing_method + "\n").append("GPS参考经度 = " + latitude_ref + "\n")
//                    .append("GPS参考纬度 = " + longitude_ref + "\n").append("GPS经度 = " + lat + "\n")
//                    .append("GPS经度 = " + lon + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double score2dimensionality(String latitude) {
        double dimensionality = 0.0;
        if (null == latitude) {
            return dimensionality;
        }

        // Use , to divide the value into 3 parts
        String[] split = latitude.split(",");
        for (int i = 0; i < split.length; i++) {

            String[] s = split[i].split("/");

            double v = Double.parseDouble(s[0]) / Double.parseDouble(s[1]);

            dimensionality = dimensionality + v / Math.pow(60, i);
        }
        return dimensionality;
    }
}

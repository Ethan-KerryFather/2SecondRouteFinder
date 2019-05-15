package com.suwon.location;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class GoogleRouteAPI extends Thread {


    public interface EventListener{
        void onApiResult(String result);
        void onApiFailed();
    }

    private Handler handler;
    private EventListener eventlistener;
    private String apikey;
    private LatLng startpoint;
    private LatLng endpoint;
    private Context context;

    public GoogleRouteAPI(Context context,LatLng startpoint,LatLng endpoint,EventListener eventListener){
        this.context=context;
        this.handler=new Handler(Looper.getMainLooper());
        this.startpoint=startpoint;
        this.endpoint=endpoint;
        this.eventlistener=eventListener;
        this.apikey=context.getResources().getString(R.string.google_api_key);
    }

    @Override
    public void run() {
        HttpsURLConnection httpsURLConnection=null;

        try {
            URL url=new URL("https://maps.googleapis.com/maps/api/directions/json?"+
                    "origin="+startpoint.latitude+","+startpoint.longitude+
                    "&destination="+endpoint.latitude+","+endpoint.longitude+
                    "&mode=transit"+
                    "&language=ko"+
                    "&key="+apikey);

            httpsURLConnection=(HttpsURLConnection)url.openConnection();

            if(httpsURLConnection.getResponseCode()==httpsURLConnection.HTTP_OK){
                InputStream inputstream=httpsURLConnection.getInputStream();  //inputstream에 httpurlconnection과 연결된 inputstream을바이트 단위로 가져오고
                InputStreamReader inputstreamreader=new InputStreamReader(inputstream, "UTF-8");//utf-8로 변환
                BufferedReader bufferedreader=new BufferedReader(inputstreamreader);//한줄씩 문자열 데이터 반환

                String line;
                StringBuilder stringbuilder=new StringBuilder();


                while((line=bufferedreader.readLine())!=null){
                    stringbuilder.append(line);
                }

                bufferedreader.close();
                String result=stringbuilder.toString();
                onApiResult(result);
            }
            else{
                onApiFailed();

            }
        } catch (Exception e) {
            e.printStackTrace();}

        finally {if(httpsURLConnection!=null){
                httpsURLConnection.disconnect();}
            }
        }

    //네트워크 조회 코드가 구현될 run메소드

    private void onApiResult(final String result){
            if(eventlistener!=null){
           handler.post(new Runnable() {        //mainactivity와 연결된 handler가 run()안의 코드를 메인스레드를 이용해서
               //실행될 수 있도록 만듭니다
               @Override
               public void run() {
                   eventlistener.onApiResult(result);
               }
           });
        }
    }

    private void onApiFailed(){
        if(eventlistener!=null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    eventlistener.onApiFailed();
                }
            });
        }
    }

}

package com.suwon.location;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends Activity {
    private static final String TAG = "I WNAT";
    private static final String MAP_BUNDLE_KEY = "MapBundleKey";
    private MapView mapView;
    private Location lastknownlocation;
    private GoogleMap map;
    boolean ispermissionallowed = true;
    private static final int REQUEST_CODE_1 = 111;
    private static final long INTERVAL_TIME = 500;
    private static final long FASTEST_INTERVAL_TIME = 1000;
    private String[] needpermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private Context mainview;
    private int picknum = 1;
    TextView textview,timezone;
    Button navigate,marker;
    public boolean buttondoubleclicked=false;
    public LatLng nowlocation;
    ScrollView scrollView;
    private AdView mAdView;
    Switch aSwitch;
    private final double degreesPerRadian = 180.0 / Math.PI;
    Button copybutton;
    boolean isnavegateon=false;



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        toastshow("환영합니다");
        //        //permission 배열안에 무엇이 있는지 확인합니다
        Log.i(TAG, grantResults[0] + "\n" + grantResults[1]);
        //grantresult안에 무엇이 있는지 logcat으로 확인합니다
        //grantresult[0],[1]에는 각각 0,0이 반환 되었습니다. PackageManager.PERMISSION_GREANTED의 값 0 과 같습니다
        //즉 권한이 승인되었음을 뜻합니다

        switch (requestCode) {
            case REQUEST_CODE_1:
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {//만약 권한을 허용받지 못했다면
                        ispermissionallowed = false;
                        break;
                        //하나라도 못받으면 실패입니다.
                        //fine location 권한은 정밀한 위치값
                        //course location 권한은 대략의 위치값 이므로 둘중 하나라도 없으면 제대로 위치데이터를 쓰기 어려워요
                    }
                }

                if(ispermissionallowed==true){
                    recreate();
                }

                if (ispermissionallowed == false) {
                    finish();
                }
                break;//swich문을 탈출합니다
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        for (String permission : needpermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {//권한 중 하나라도 승인 받지 못하면
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(this, needpermissions, REQUEST_CODE_1);
                }
                break;//이미 needpermission 배열에 있는 권한을 모두 신청했으므로 반복문을 탈출합니다.
            }
        }



        Bundle mapviewBundle = null; //저장된 mapview 상태 정보를 담을 bundle변수
        if (savedInstanceState != null) {
            mapviewBundle = savedInstanceState.getBundle(MAP_BUNDLE_KEY); //키로 mapview의 상태정보를 찾아서 저장
        }


        MobileAds.initialize(this, "ca-app-pub-1292397612141589~4661340834");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        timezone=(TextView)findViewById(R.id.timezone);


        textview=(TextView)findViewById(R.id.textview);
        textview.setMovementMethod(new ScrollingMovementMethod());
        copybutton=(Button)findViewById(R.id.imagebutton);

        copybutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String routestring=textview.getText().toString();
                ClipboardManager clipboard=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip=ClipData.newPlainText("id",routestring);
                clipboard.setPrimaryClip(clip);
                toastshow("경로가 복사되었습니다!");
            }
        });

        Typeface typeface=getResources().getFont(R.font.downloadedfont);
        aSwitch=(Switch)findViewById(R.id.switch1);


        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked==true){
                    toastshow("네비게이션 모드로 전환되었습니다!");
                   isnavegateon=true;
                    updateMylocation();
                    setParticularLocation();
            }
                else if(isChecked==false){
                    isnavegateon=false;
                    updateMylocation();
                    toastshow("일반모드로 전환되었습니다!");
                }
            }
        });


        textview.setTypeface(typeface);


        marker=(Button)findViewById(R.id.marker);
        marker.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                map.clear();
                textview.setText("목적지를 2초간 꾸욱 눌러주세요");
                timezone.setText(null);
            }
        });

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(mapviewBundle);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                try {
                    if ((ActivityCompat.checkSelfPermission(getApplicationContext(), needpermissions[0]) != PackageManager.PERMISSION_GRANTED)
                            || ActivityCompat.checkSelfPermission(getApplicationContext(), needpermissions[1]) != PackageManager.PERMISSION_GRANTED) {
                        toastshow("at least 1 permission is not allowed");
                    } else if ((ActivityCompat.checkSelfPermission(getApplicationContext(), needpermissions[0]) == PackageManager.PERMISSION_GRANTED)
                            && ActivityCompat.checkSelfPermission(getApplicationContext(), needpermissions[1]) == PackageManager.PERMISSION_GRANTED) {

                    }
                } catch (Exception e) {
                    toastshow("exception about permission borned");
                    e.printStackTrace();
                }

                getMylocation();

            }
        });
        LocationRequest request = new LocationRequest();
        FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            toastshow("permission error");
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(request, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                nowlocation=new LatLng(location.getLatitude(), location.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
            }
        }, null);


    }

    public void getMylocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            toastshow("permission error");
            return;
        } else if (ispermissionallowed == true) {

            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            setParticularLocation();


        } //is permission allowed? true area
        else {
            toastshow("really exception borned");
        }
    }

    public void setParticularLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            toastshow("permission error");
            return;
        }
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                map.clear();
                MarkerOptions pickmarker = new MarkerOptions();
                pickmarker.position(latLng);

                pickmarker.title("최종 목적지");
                map.addMarker(pickmarker);
                getroute(latLng);

            }
        });


    }

    public void updateMylocation() {
        LocationRequest locationrequest = new LocationRequest();
        locationrequest.setInterval(INTERVAL_TIME);
        locationrequest.setFastestInterval(FASTEST_INTERVAL_TIME);
        locationrequest.setPriority(locationrequest.PRIORITY_NO_POWER);// 추가 전력 소모 없이 최상의 전력
        FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            toastshow("permission error");
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationrequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if(isnavegateon==true)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17));

            }
        }, null);
    }

    public void onSaveInstanceState(Bundle outstate) {
        super.onSaveInstanceState(outstate);
        Bundle mapBundle = outstate.getBundle(MAP_BUNDLE_KEY);

        if (mapBundle == null) {
            mapBundle = new Bundle();
            outstate.putBundle(MAP_BUNDLE_KEY, mapBundle);
        }
        mapView.onSaveInstanceState(mapBundle);
    }

    public void getroute(final LatLng endpoint) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Location lastlocation=location;
                LatLng startpoint=new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());
                final GoogleRouteAPI googleRouteAPI=new GoogleRouteAPI(MainActivity.this, startpoint, endpoint,
                        new GoogleRouteAPI.EventListener() {
                    @Override
                    public void onApiResult(String result) {
                        route route=GoogleRouteResultParser.getroute(result);
                        showroute(route);
                    }

                    @Override
                    public void onApiFailed() {
                        toastshow("api calling failed");
                    }
                });

                googleRouteAPI.start();
            }
        });
    }



    public void showroute(route route){
        if(route!=null) {


           timezone.setText("출발\n"+route.departuretime+
                   "\n도착\n"+route.arrivaltime);



            if(route.routetime!=null)
            textview.setText("목적지까지는 " + route.routetime + "이 소요될 예정입니다\n");


            PolylineOptions polylineOptions=new PolylineOptions().color(Color.BLUE).width(5);
            for (step step : route.steps) {
                {

                    if (step == route.steps.get(0))
                        textview.append("먼저 " + step.distance+"m를"+" 걸어서 이동하십시오\n");
                    else if(step==route.steps.get(route.steps.size()-1)){
                        textview.append("마지막으로 "+step.distance+" m를"+" 걸어서 이동하십시오\n");
                    }



                    boolean isprinted=true;
                    if ((step.transitname!=null)&&(step.transitstopnumber!=0)) {
                        textview.append("" + step.transitname + "을 타시고 "+step.transitstopnumber + "정거장을 이동하십시오\n");

                    }



                    if ((step.startlocation != null) && (step.endlocation != null)) {
                        MarkerOptions startpoint = new MarkerOptions();
                        startpoint.position(step.startlocation);
                        startpoint.title("출발");
                        startpoint.visible(true);
                        map.addMarker(startpoint);

                        MarkerOptions endpoint = new MarkerOptions();
                        endpoint.position(step.endlocation);
                        endpoint.title("경유");
                        endpoint.visible(true);
                        map.addMarker(endpoint);
                        polylineOptions.add(step.startlocation).add(step.endlocation);
                        map.addPolyline(polylineOptions);


                    }
                }//for

                if (step==route.steps.get(route.steps.size()-1)){
                    textview.append("도착!");}
                }




            }

        }




    @Override
    protected  void onResume(){
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    public void toastshow(String string){
        Toast toast=Toast.makeText(this, string, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.CENTER,0, 0);
        toast.show();

    }



}

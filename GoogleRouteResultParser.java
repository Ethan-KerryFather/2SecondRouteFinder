package com.suwon.location;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GoogleRouteResultParser {

    public static route getroute(String result){
        route route=new route();
        Log.i("result", ""+result);

        try{
            JSONObject root=new JSONObject(result);
            JSONArray routes=root.getJSONArray("routes");
            JSONObject routes1=routes.getJSONObject(0);

            JSONArray legs=routes1.getJSONArray("legs");
            JSONObject legs1=legs.getJSONObject(0);


            JSONObject duration=legs1.getJSONObject("duration");

            JSONObject arrival=legs1.getJSONObject("arrival_time");
            route.timezone=arrival.getString("time_zone");
            route.arrivaltime=arrival.getString("text");

            JSONObject departure=legs1.getJSONObject("departure_time");
            route.departuretime=departure.getString("text");


            String routetime=duration.getString("text");
            route.routetime=routetime;


            route.steps=new ArrayList<>();
            JSONArray steps=legs1.getJSONArray("steps");



            if(legs1.has("overview_polyline")){
            JSONObject polyline=legs1.getJSONObject("overview_polyline");
            //폴리라인
            route.overviewpolyline=polyline.getString("points");
                String points=polyline.getString("points");
                Log.i("poly", ""+points);}





            for(int i=0;i<steps.length();i++){
                step step=new step();

                JSONObject routestep=steps.getJSONObject(i);






                String travelmode=routestep.getString("travel_mode");


                JSONObject startlocation=routestep.getJSONObject("start_location");
                JSONObject endlocation=routestep.getJSONObject("end_location");
                JSONObject distance=routestep.getJSONObject("distance");




                if(routestep.has("distance")){
                    step.distance=distance.getDouble("value");
                }


                if(routestep.has("start_location")){
                    LatLng startpoint=new LatLng(startlocation.getDouble("lat"), startlocation.getDouble("lng"));
                    step.startlocation=startpoint;
                }

                if(routestep.has("end_location")){
                    LatLng endpoint=new LatLng(endlocation.getDouble("lat"), endlocation.getDouble("lng"));
                    step.endlocation=endpoint;
                }


                if(travelmode.equals("TRANSIT")&&routestep.has("transit_details")){
                    JSONObject transitdetails=routestep.getJSONObject("transit_details");

                    if(transitdetails.has("num_stops")){
                        int numstops=transitdetails.getInt("num_stops");
                        step.transitstopnumber=numstops;
                    }

                    JSONObject line=transitdetails.getJSONObject("line");
                    if(line.has("short_name")){
                        String shortname=line.getString("short_name");
                        step.transitname=shortname;
                    }
                }
                route.steps.add(step);
            }
        }catch (JSONException e){
            e.printStackTrace();
            return null;
        }
        return route;
    }
}

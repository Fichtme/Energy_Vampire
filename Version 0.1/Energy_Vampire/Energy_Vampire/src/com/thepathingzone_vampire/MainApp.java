package com.thepathingzone_vampire;

import java.io.IOException;
import java.net.URI;

import org.json.JSONArray;
import org.json.JSONObject;

import com.codebutler.android_websockets.SocketIOClient;

import android.app.Application;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class MainApp extends Application{
	
		public static int team;
		public static Boolean gameActive = false;
		
		public static SocketIOClient ioWebSocket;
		public static String address = "http://mediawerf.dyndns.org:7080";
		public static Boolean canConnect = false;
		public static int connectStatus = 2;
		public static Thread connection;
		
		LocationManager GPS;
		LocationManager locationManager;
		LocationListener listener;
		
		public void startTracking()
		{
			
		Criteria criteria = new Criteria();
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
    	GPS = (LocationManager) getSystemService(LOCATION_SERVICE);	    	
    	String provider;
    	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    provider = locationManager.getBestProvider(criteria, false);
	    
	    listener = new LocationListener() {
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				
			}
			
			@Override
			public void onProviderEnabled(String provider) {
					
			}
			
			@Override
			public void onProviderDisabled(String provider) {	
			}
			
			@Override
			public void onLocationChanged(Location location) {
				JSONObject gpsData = new JSONObject();
				JSONArray arguments = new JSONArray();
				
				try {
					gpsData.put("lat", location.getLatitude());
					gpsData.put("lng", location.getLongitude());
					arguments.put(gpsData);
					ioWebSocket.emit("updateLocation", arguments);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	    
	    locationManager.requestLocationUpdates(provider, 100, 1, listener );
	}
	
	public void stopTracking()
	{
		locationManager.removeUpdates(listener);
	}
	
	public boolean init() {
		connectStatus = 0;
		
		connection =  new Thread() {
			@Override
			public void run() {
				try {
					socketCientConnection();
					ioWebSocket.connect();
				} catch (Exception e) {
					Log.e("exeption", "" + e);
					MainApp.connectStatus = 2;
				}
			}};
		connection.start();
		//check Connection
		while(connectStatus == 0){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return canConnect;	
	}
	
	public boolean closeInit(){
		try {
			connection = null;
			ioWebSocket.disconnect();
			return false;
		} catch (IOException e) {
			return true;
		}
	}
	
	public void socketCientConnection()
	{
		ioWebSocket = new SocketIOClient(URI.create(address), new SocketIOClient.Handler() {
			String tag = "socketClientConnection";
					
			@Override
			public void onConnect() {
				Long tsLong = System.currentTimeMillis()/1000;
				String ts = "MobileClient" + tsLong.toString();
				MainApp.canConnect = true;  
		        MainApp.connectStatus = 1;
		        
		        JSONObject registerData = new JSONObject();
				JSONArray arguments = new JSONArray();
				
				try {
					registerData.put("nickname", ts);
					arguments.put(registerData);
					ioWebSocket.emit("register", arguments);
				} catch (Exception e) {
					e.printStackTrace();
				}
		         
		    }

		    @Override
		    public void on(String event, JSONArray arguments) {
		      
		    }

		    @Override
		    public void onDisconnect(int code, String reason) {
		        Log.d(tag, String.format("Disconnected! Code: %d Reason: %s", code, reason));  
		    }

		    @Override
		    public void onError(Exception error) {
		    	Log.d(tag, error + "");
		        MainApp.connectStatus = 2;  
		    }
		});
	}
}

package com.fourthwardcoder.android.freezewarning;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FusedLocationService implements
LocationListener,
GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener {

	private static final long INTERVAL = 1000 * 30;
	private static final long FASTEST_INTERVAL = 1000 * 5;
	private static final long ONE_MIN = 1000 * 60;
	private static final long REFRESH_TIME = ONE_MIN * 5;
	private static final float MINIMUM_ACCURACY = 50.0f;
	private static final int POLL_INTERVAL = 1000 * 15; // 15 seconds
	Activity locationActivity;
	private LocationRequest locationRequest;
	private GoogleApiClient googleApiClient;
	private Location location;
	private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
	
	private static final String TAG = "FusedLocationService";

	public FusedLocationService(Activity locationActivity) {
		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(INTERVAL);
		locationRequest.setFastestInterval(FASTEST_INTERVAL);
		this.locationActivity = locationActivity;

		googleApiClient = new GoogleApiClient.Builder(locationActivity)
		.addApi(LocationServices.API)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.build();

		if (googleApiClient != null) {
			googleApiClient.connect();
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		
		/*
		Location currentLocation = fusedLocationProviderApi.getLastLocation(googleApiClient);
		if (currentLocation != null && currentLocation.getTime() > REFRESH_TIME) {
			location = currentLocation;
		} else {
			fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
			// Schedule a Thread to unregister location listeners
			Executors.newScheduledThreadPool(1).schedule(new Runnable() {
				@Override
				public void run() {
					fusedLocationProviderApi.removeLocationUpdates(googleApiClient,
							FusedLocationService.this);
				}
			}, ONE_MIN, TimeUnit.MILLISECONDS);
		}
		*/
		Log.d(TAG,"!!!! Inside onConnected. Send to WeatherPollService !!!!");
		LocationRequest locationRequest = LocationRequest.create()
		        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
	            .setFastestInterval(POLL_INTERVAL)
	            .setInterval(POLL_INTERVAL)
	            .setSmallestDisplacement(75.0F);
		
	    PendingIntent pendingIntent = PendingIntent.getService(locationActivity, 0,
	            new Intent(locationActivity, WeatherPollService.class),
	            PendingIntent.FLAG_UPDATE_CURRENT);
	    
	    LocationServices.FusedLocationApi.requestLocationUpdates(
	    		googleApiClient, locationRequest, pendingIntent);
	    
	    Log.d(TAG,"End onConnected");
	    
	    /*
		//Set up alarm
		AlarmManager alarmManager = (AlarmManager)locationActivity.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		
			//Start the alarm. Fire the Pending Intent "pi" when the alarm goes off
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pendingIntent);
			*/
	}

	@Override
	public void onLocationChanged(Location location) {
		//if the existing location is empty or
		//the current location accuracy is greater than existing accuracy
		//then store the current location
		Log.d(TAG,"Locaiton changed!!");
		if (null == this.location || location.getAccuracy() < this.location.getAccuracy()) {
			this.location = location;
			//if the accuracy is not better, remove all location updates for this listener
			if (this.location.getAccuracy() < MINIMUM_ACCURACY) {
				fusedLocationProviderApi.removeLocationUpdates(googleApiClient, this);
			}
		}
	}

	public Location getLocation() {
		return this.location;
	}

	@Override
	public void onConnectionSuspended(int i) {

	}


	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}


}

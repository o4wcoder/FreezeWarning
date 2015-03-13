package com.fourthwardcoder.android.freezewarning;



import java.io.IOException;
import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderApi;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class WeatherPollService extends IntentService implements Constants{

	/**************************************************************************/
	/*                            Constants                                   */
	/**************************************************************************/
	private static final String TAG = "WeatherPollService";
	private static final int POLL_INTERVAL = 1000 * 15; // 15 seconds
	
	/**************************************************************************/
	/*                            Local Data                                  */
	/**************************************************************************/
	private NotifySettings notifySettings;
	
	public WeatherPollService() {
		super(TAG);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.i(TAG,"Received intent: " + intent);
		
		ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		
		//Verify that the network is available
		@SuppressWarnings("deprecation")
		boolean isNetworkAvailable = cm.getBackgroundDataSetting() 
		&& cm.getActiveNetworkInfo() != null;
		
		if(!isNetworkAvailable)
			return;
	
		// Acquire a reference to the system Location Manager
		//LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		 //Location location = (Location) intent.getExtras().get(LocationClient.KEY_LOCATION_CHANGED);
		final Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
		
		if(location != null) {
			Log.d(TAG,"location long " + location.getLongitude() + " lat: " + location.getLatitude());
			
			WeatherGetter wg = new WeatherGetter(location);
			
			
			try {
				ArrayList<WeatherData> weatherDataList = wg.getWeatherDataList(this);
				
				if(weatherDataList != null) {
					
					Log.e(TAG,"Got data " + weatherDataList.get(0).getHour());
				
				    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				    .setSmallIcon(android.R.drawable.ic_dialog_info)
				    .setContentTitle("Freeze Warning")
				    .setContentText(weatherDataList.get(0).getHour());
				   
				    //call page to display hourly data after notification is pressed
				    Intent i = new Intent(this,HourlyWeatherActivity.class);
				      i.putParcelableArrayListExtra(EXTRA_WEATHER_DATA_LIST, weatherDataList);
				      //i.putExtra(EXTRA_IS_FAHRENHEIT,notifySettings.isTempFahrenheit());
				    PendingIntent resultPendingIntent =
				    	    PendingIntent.getActivity(
				    	    this,
				    	    0,
				    	    i,
				    	    PendingIntent.FLAG_UPDATE_CURRENT
				    	);
				    mBuilder.setContentIntent(resultPendingIntent);
				    
				    int mNotificaitonId = 001;
				    
				    NotificationManager mNotifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
				    mNotifyMgr.notify(mNotificaitonId,mBuilder.build());
				    Log.d(TAG,"Send notification");
				    
				}
			} 
			catch (IOException e) 
			{
				Log.e(TAG,"!!Error with getting the Weather Data List");
				e.printStackTrace();
			}
		}
			
			
			
		/*
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == status) {
			Log.e(TAG,"Success in opening google play services");
			FusedLocationService fusedLocationService = new FusedLocationService(this);
		} else {
			Log.e(TAG,"Failed to open google play");
		}
		

*/
	}
	
	public static void setServiceAlarm(Context context, NotifySettings notifySettings) {
		
		
		//Construct pending intent that will start WeatherPollService
		Intent i = new Intent(context, WeatherPollService.class);
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
		
		//Set up alarm
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		
		if(notifySettings.isSwitchEnabled()) {
			//Start the alarm. Fire the Pending Intent "pi" when the alarm goes off
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pi);
		}
		else {
			//Cancel the alarm
			alarmManager.cancel(pi);
			pi.cancel();
		}
		
	}
	
	public static boolean isServiceAlarmOn(Context context) {
		
		Intent i = new Intent(context, WeatherPollService.class);
		
		//PendingIntent is only used for setting the alarm, 
		//so a null PendingIntent means the alarm is not set 
		PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
		
		return pi != null;
	}

}

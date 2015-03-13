package com.fourthwardcoder.android.freezewarning;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;
import android.widget.ToggleButton;


public class FreezeWarningSetupFragment extends Fragment implements Constants {

	/*********************************************************************/
	/*                        Constants                                  */
	/*********************************************************************/
	private static final String TAG = "FreezeWarningSetupFragment";
	private static final String FILENAME = "freezewarning.json";
	private static final String SWITCH_STATE = "switchstate";
	private static final String HOUR = "hour";
	private static final String MIN = "min";
	private static final String TEMP_FAHRENHEIT = "tempfahrenheit";
	private static final String ENDPOINT = "https://api.forecast.io/forecast/";
	private static final String API_KEY = "ae77cb21f8ace2b90472182a1de5fc58/";



	/*********************************************************************/
	/*                         Local Data                                */
	/*********************************************************************/
	private Button setButton;
	private TimePicker notifyTimePicker;
	private NotifySettings notifySettings;
	private Switch notifySwitch;
	private RadioButton fahrenheitRadioButton;
	private RadioButton celsiusRadioButton;
	private FusedLocationService fusedLocationService;
	private ArrayList<WeatherData> weatherDataList;
	private boolean fahrenheitTemp = true;

	/*********************************************************************/
	/*                       Override Methods                            */
	/*********************************************************************/
	@Override
	public void onCreate(Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);

		notifySettings = new NotifySettings(true,true,8,0);

		//Load saved settings
		loadSettings();

		/*
		if(isGooglePlayServicesAvailable()) {
			Log.d(TAG,"Google Services are Available!");
			fusedLocationService = new FusedLocationService(getActivity());

		}
		else {
			Log.d(TAG,"FAILED! No Google Services");
		}
		*/
		
		//retain the instance on rotation
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.fragment_freeze_warning_setup, container, false);

		//Set up Time Picker for setting notification time
		notifyTimePicker = (TimePicker)view.findViewById(R.id.notifyTimePicker);
		notifyTimePicker.setCurrentHour(notifySettings.getHour());
		notifyTimePicker.setCurrentMinute(notifySettings.getMin());
		
		//Set up listener for Time Picker
		notifyTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				// TODO Auto-generated method stub
				Log.d(TAG,"Time hour: " + hourOfDay + " minute: " + minute);
				notifySettings.setHour(hourOfDay);
				notifySettings.setMin(minute);
				Log.d(TAG,"Time milli: " + notifySettings.getMillisecondTime());

			}

		});

		//Setup "Set Notification" Button
		setButton = (Button)view.findViewById(R.id.setButton);
		setButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveSettings();

				if(isGooglePlayServicesAvailable()) {
					Log.d(TAG,"Google Services are Available!");
					fusedLocationService = new FusedLocationService(getActivity());

				}
				else {
					Log.d(TAG,"FAILED! No Google Services");
				}
				
				/*
				if(fusedLocationService != null) {
					Location location = fusedLocationService.getLocation();
					Log.d(TAG,"Latitude: " + location.getLatitude() + " Longitude: "+ location.getLongitude());
					
					//Start up WeatherPollService
					//Intent i = new Intent(getActivity(),WeatherPollService.class);
					//getActivity().startService(i);
					//WeatherPollService.setServiceAlarm(getActivity(), notifySettings);
					//new DownloadWeatherDataTask().execute(location);
				}
				*/
				Toast toast = Toast.makeText(getActivity().getApplicationContext(),
						R.string.toast_text, Toast.LENGTH_SHORT);
				toast.show();

			}

		});
		
		//Get Temperature Radio Buttons
		fahrenheitRadioButton = (RadioButton)view.findViewById(R.id.farenheitRadioButton);
		celsiusRadioButton = (RadioButton)view.findViewById(R.id.celsiusRadioButton);
		
		if(notifySettings.isTempFahrenheit()) {
			fahrenheitRadioButton.setChecked(true);
		    celsiusRadioButton.setChecked(false);
		}
		else {
			fahrenheitRadioButton.setChecked(false);
		    celsiusRadioButton.setChecked(true);
		}

		//Set up Notification Switch
		notifySwitch = (Switch) view.findViewById(R.id.notifySwitch);
		notifySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//Turn components on/off and save settings
				setWidgetVisibility(isChecked);
				notifySettings.setSwitchEnabled(isChecked);
				saveSettings();
				
				//Turn off Notification
				//WeatherPollService.setServiceAlarm(getActivity(), notifySettings);
			}
		});

		notifySwitch.setChecked(notifySettings.isSwitchEnabled());

		setWidgetVisibility(notifySwitch.isChecked());

		return view;
	}


	/******************************************************************************/
	/*                              Private Methods                               */
	/******************************************************************************/
	private void loadSettings() {

		BufferedReader reader = null;

		try {
			Log.d(TAG,"Inside loadSettings()");
			//Open and read the file into a StringBuilder
			InputStream in = getActivity().getApplicationContext().openFileInput(FILENAME);
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder jsonString = new StringBuilder();

			String line = null;
			while((line = reader.readLine()) != null) {
				//Line breaks are omitted and irrelevant
				jsonString.append(line);	
			}
			//Parse the JSON using JSONTokener
			JSONObject obj = (JSONObject) new JSONTokener(jsonString.toString()).nextValue();

			Log.d(TAG,"Loaded: " + obj.toString());
			notifySettings.setSwitchEnabled(obj.getBoolean(SWITCH_STATE));
			notifySettings.setHour(obj.getInt(HOUR));
			notifySettings.setMin(obj.getInt(MIN));
			notifySettings.setTempFahrenheit(obj.getBoolean(TEMP_FAHRENHEIT));

		}
		catch (FileNotFoundException e) {
			//Ignore this on; it happens when starting fresh
			Log.d(TAG,"No JSon File");
		}
		catch (IOException e) {
			Log.e(TAG,"Error reading JSON data");
		}
		catch(JSONException ex) {
			Log.e(TAG,"Error saving JSON settings");
		} finally {
			if(reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

	}

	private void saveSettings() {
		
		//Set state of Temp RadioButtons
		if(fahrenheitRadioButton.isChecked())
			notifySettings.setTempFahrenheit(true);
		else
			notifySettings.setTempFahrenheit(false);
		
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(SWITCH_STATE, notifySettings.isSwitchEnabled());
			jsonObj.put(HOUR, notifySettings.getHour());
			jsonObj.put(MIN, notifySettings.getMin());
            jsonObj.put(TEMP_FAHRENHEIT, notifySettings.isTempFahrenheit());
			//Write the file to disk
			Writer writer = null;
			try {
				OutputStream out = getActivity().getApplicationContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
				writer = new OutputStreamWriter(out);
				writer.write(jsonObj.toString());
				Log.d(TAG,"JSON Output: " +jsonObj.toString());
			}
			finally {
				if(writer != null)
					writer.close();
			}

		}
		catch (IOException ex) {
			Log.e(TAG,"Error writing JSON data");
		}
		catch(JSONException ex) {
			Log.e(TAG,"Error saving JSON settings");
		}
	}

	private void setWidgetVisibility(boolean set) {
		setButton.setEnabled(set);
		notifyTimePicker.setEnabled(set);
		fahrenheitRadioButton.setEnabled(set);
		celsiusRadioButton.setEnabled(set);
	}

	private boolean isGooglePlayServicesAvailable() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
		if (ConnectionResult.SUCCESS == status) {
			return true;
		} else {
			GooglePlayServicesUtil.getErrorDialog(status, getActivity(), 0).show();
			return false;
		}
	}

	/***************************************************************************/
	/*                               Inner Classes                             */
	/***************************************************************************/
	private class DownloadWeatherDataTask extends AsyncTask<Location,Void,Void> {

		String fullWeatherData;
		Location currentLocation;

		@Override
		protected Void doInBackground(Location... locations) {
			currentLocation = locations[0];
			Log.d(TAG,"In task Latitude: " + currentLocation.getLatitude() + " Longitude: "+ currentLocation.getLongitude());

			WeatherGetter wg = new WeatherGetter(currentLocation);
			try {
				weatherDataList = wg.getWeatherDataList(getActivity().getApplicationContext());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			

			try {
				fullWeatherData = wg.getFullUrlString();
				Log.d(TAG,fullWeatherData);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			try {
				
				//Get Address of current location
				Address currentAddress = null;
				if(Geocoder.isPresent()) {
					Geocoder gcd = new Geocoder(getActivity().getApplicationContext());

					List<Address> addresses = gcd.getFromLocation(currentLocation.getLatitude(),
							currentLocation.getLongitude(),1);
					Log.d(TAG,"Size of address " + addresses.size());	
					if(addresses.size() > 0)
						currentAddress = addresses.get(0);
				}
				
				Log.d(TAG,"Parsing JSON String");
				//JSONArray jArray = new JSONArray(fullWeatherData);

				//Parse the JSON using JSONTokener
				JSONObject obj = (JSONObject) new JSONTokener(fullWeatherData).nextValue();

				//Get Hourly Data
				String hourly = obj.getString(TAG_HOURLY);
				Log.d(TAG,"HOURLY: " + hourly);

				//Pull out data section of hourly data
				JSONObject obj2 = (JSONObject) new JSONTokener(hourly).nextValue();
				JSONArray hArray = obj2.getJSONArray(TAG_DATA);

				//Pull out hourly data for the number of hours to check. Usually 24.
				weatherDataList = new ArrayList<WeatherData>(HOURS_TO_CHECK);
				for(int i = 0; i < HOURS_TO_CHECK; i++ ){
					JSONObject jObject = hArray.getJSONObject(i);
					WeatherData wData = new WeatherData(currentLocation,currentAddress,jObject);
					weatherDataList.add(i, wData);
				}
               
			}
			catch (JSONException e) {
				Log.e(TAG,e.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
*/
			return null;
		}

		protected void onPostExecute(Void v) {
			Log.d(TAG,"In on PostExecute!");
			Intent i = new Intent(getActivity(),HourlyWeatherActivity.class);
			//i.putExtra(EXTRA_WEATHER_DATA_LIST, weatherDataList);
		      i.putParcelableArrayListExtra(EXTRA_WEATHER_DATA_LIST, weatherDataList);
		      i.putExtra(EXTRA_IS_FAHRENHEIT,notifySettings.isTempFahrenheit());
			startActivity(i);

		}

	}

}

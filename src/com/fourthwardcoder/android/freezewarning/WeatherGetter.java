package com.fourthwardcoder.android.freezewarning;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

public class WeatherGetter implements Constants{
	
	/*********************************************************************/
	/*                        Constants                                  */
	/*********************************************************************/
	private static final String TAG = "WeatherGetter";
	private static final String FILENAME = "freezewarning.json";
	private static final String ENDPOINT = "https://api.forecast.io/forecast/";
    private static final String API_KEY = "ae77cb21f8ace2b90472182a1de5fc58/";
    
    /*********************************************************************/
    /*                         Local Data                                */
    /*********************************************************************/
    private Location location;
    private String strUrl;
    
   
    public WeatherGetter(Location location){
        this.location = location;
        strUrl = ENDPOINT + API_KEY + String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());
        Log.d(TAG,"URL: " + strUrl);
    }
	/* Method to fetch raw data from a URL and return it as
	 * an array of bytes
	 */
	String getFullUrlString() throws IOException {
		
		//Create URL object from String like "www.google.com"
		URL url = new URL(strUrl);
		
		/* Create connection object pointed at the URL. Since it is an
		 * http URL, cast it to HttpURLConnection
		 */
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		
		try {			
			//Get input stream to connect to endpoint
			InputStream in = connection.getInputStream();
			
			//check if connection can be made
			if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
			}
			
			BufferedReader bReader = new BufferedReader(new InputStreamReader(in, "iso-8859-1"), 8);
			StringBuilder sBuilder = new StringBuilder();
			
			//read from input stream until there is no data left
			String line = null;
			while((line = bReader.readLine()) != null) {
				sBuilder.append(line + "\n");
			}
			in.close();
			
			//return String of output stream
			return sBuilder.toString();
		} finally {
			connection.disconnect();
		}
	}
	
	ArrayList<WeatherData> getWeatherDataList(Context context) throws IOException {
		
		ArrayList<WeatherData> weatherDataList = null;
		
		String  fullWeatherData = getFullUrlString();
		
		try {
			
			//Get Address of current location
			Address currentAddress = null;
			if(Geocoder.isPresent()) {
				Geocoder gcd = new Geocoder(context);

				List<Address> addresses = gcd.getFromLocation(location.getLatitude(),
						location.getLongitude(),1);
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
				WeatherData wData = new WeatherData(location,currentAddress,jObject);
				weatherDataList.add(i, wData);
			}
           
		}
		catch (JSONException e) {
			Log.e(TAG,e.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return weatherDataList;
	}

}

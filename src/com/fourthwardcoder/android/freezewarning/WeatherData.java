package com.fourthwardcoder.android.freezewarning;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Address;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class WeatherData implements Parcelable, Constants{

	/************************************************************/
	/*                       Constants                          */
	/************************************************************/
	private static final String TAG = "WeatherData";
	private static final String CURRENT_LOCATION = "CurrentLocation";
	private char DEGREE_SYMBOL = '\u00B0';
	
	/************************************************************/
	/*                      Local Data                          */
	/************************************************************/
	private Location location;
	private String hourlyTemp;
	private String icon;
	private String unixTime;
	private String fullDateTime;
	private String hour;
	private String city;
	private String zip;
	private String precip;
	private String wind;
	private boolean freezing = false;
	/************************************************************/
	/*                      Constructors                        */
	/************************************************************/
	//public WeatherData(Location location,Address address,String hourlyTemp, String unixTime, String icon) {
	public WeatherData(Location location, Address address, JSONObject jObject) {
		this.location = location;
		
		//Parse JSON data
		try {
			
			//Get Temp, round it to whole number
			hourlyTemp = jObject.getString(TAG_TEMP);
			double dTemp = Double.valueOf(hourlyTemp);
			int iTemp = (int)dTemp;
			hourlyTemp = String.valueOf(iTemp);
			
		    unixTime = jObject.getString(TAG_TIME);
		    icon = jObject.getString(TAG_ICON);
		    
		    //Get Precip percentage
		    precip = jObject.getString(TAG_PRECIP);
		    
		    double dPrecip = Double.valueOf(precip) * 100;
		    int iPrecip = (int)dPrecip;
		    precip = String.valueOf(iPrecip) + "%";
		    Log.d(TAG,"Precip: " + precip);
		    
		    //Get Wind data		    
		    wind = calculateWind(jObject.getString(TAG_WIND_SPEED), jObject.getString(TAG_WIND_BEARNING));
		    if(Integer.valueOf(hourlyTemp) <= FREEZING_TEMP) 
		    	freezing = true;
		    
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			hourlyTemp = "";
			unixTime = "";
			icon = "";
		}
		Date date = new Date(Long.valueOf(unixTime) * 1000L); //times 100 to convert to milliseconds
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM d");
		fullDateTime = sdf.format(date);
		Log.d(TAG,"FullDateTime: " + fullDateTime);
		hour = convertHour(String.valueOf(date.getHours()));

		if(address != null) {
			city = address.getLocality();
			zip = address.getPostalCode();
		}
		else {
			city = CURRENT_LOCATION;
			zip = "";
		}
	}

	/************************************************************/
	/*                     Private Methods                      */
	/************************************************************/
	private String convertHour(String millitaryHour) {
		
		int millHour = Integer.valueOf(millitaryHour);
		
		if(millHour == 12)
			return String.valueOf(millHour) + " PM";
		if(millHour == 0)
			return "12 AM";
		
		if(millHour < 12)
			return String.valueOf(millHour) + " AM";
		else {
			int regHour = millHour % 12;
			return String.valueOf(regHour) + " PM";
			}	
	}
	
	private String calculateWind(String windSpeed, String windBearing) {
		
		

		try {
			double dWindSpeed = Double.valueOf(windSpeed);
			int iWindSpeed = (int)dWindSpeed;

			String strWindSpeed = String.valueOf(iWindSpeed);

			double dWindBearing = Double.valueOf(windBearing);

			String strWindBearing = "N";
			
			if(checkRange(dWindBearing,WIND_NNE))
				strWindBearing = "NNE";
			else if(checkRange(dWindBearing,WIND_NE))
				strWindBearing = "NE";
			else if(checkRange(dWindBearing,WIND_ENE))
				strWindBearing = "ENE";	
			else if(checkRange(dWindBearing,WIND_E))
				strWindBearing = "E";
			else if(checkRange(dWindBearing,WIND_ESE))
				strWindBearing = "ESE";
			else if(checkRange(dWindBearing,WIND_SE))
				strWindBearing = "SE";
			else if(checkRange(dWindBearing,WIND_SSE))
				strWindBearing = "SSE";
			else if(checkRange(dWindBearing,WIND_S))
				strWindBearing = "S";
			else if(checkRange(dWindBearing,WIND_SSW))
				strWindBearing = "SSW";
			else if(checkRange(dWindBearing,WIND_SW))
				strWindBearing = "SW";
			else if(checkRange(dWindBearing,WIND_WSW))
				strWindBearing = "WSW";
			else if(checkRange(dWindBearing,WIND_W))
				strWindBearing = "W";
			else if(checkRange(dWindBearing,WIND_WNW))
				strWindBearing = "WNW";
			else if(checkRange(dWindBearing,WIND_NW))
				strWindBearing = "NW";
			else if(checkRange(dWindBearing,WIND_NNW))
				strWindBearing = "NNW";
			
            return strWindSpeed + " " + strWindBearing;
		}
		catch (NumberFormatException e) {
			return "--";
		}
		
	}
	
	private boolean checkRange(double bearing, double direction) {
		final double RANGE = 11.25;

		if(bearing > (direction - RANGE) && (bearing < (direction) + RANGE))
			return true;
		else
			return false;
	}
	/************************************************************/
	/*                     Public Methods                       */
	/************************************************************/
	public Location getLocation() {
		return location;
	}

	public String getHourlyTemp() {
		return hourlyTemp + DEGREE_SYMBOL + "F";
	}
	
	public String getCelsiusHourlyTemp() {
		
		double dTemp = (Double.valueOf(hourlyTemp) - 32) / 1.8;
		int iTemp = (int)dTemp;
		return String.valueOf(iTemp) + DEGREE_SYMBOL + "C";
	}

	public String getFullDataTime() {
		return fullDateTime;
	}

	public String getIcon() {
		return icon;
	}

	public String getUnixTime() {
		return unixTime;
	}
	
	public String getHour() {
		return hour;
	}
	
    public String getCity() {
		return city;
	}

	public String getZip() {
		return zip;
	}
	
	public boolean isFreezing() {
		return freezing;
	}

	public String getPrecip() {
		return precip;
	}

	public void setPrecip(String precip) {
		this.precip = precip;
	}
	
	public String getWind() {
		return wind;
	}

	public void setWind(String wind) {
		this.wind = wind;
	}
	/*********************************************************************/
	/*                            Parcable Data                          */
	/*********************************************************************/
	protected WeatherData(Parcel in) {
        location = (Location) in.readValue(Location.class.getClassLoader());
        hourlyTemp = in.readString();
        icon = in.readString();
        unixTime = in.readString();
        fullDateTime = in.readString();
        hour = in.readString();
        city = in.readString();
        zip = in.readString();
        freezing = in.readByte() != 0x00;
        precip = in.readString();
        wind = in.readString();
    }

	@Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(location);
        dest.writeString(hourlyTemp);
        dest.writeString(icon);
        dest.writeString(unixTime);
        dest.writeString(fullDateTime);
        dest.writeString(hour);
        dest.writeString(city);
        dest.writeString(zip);
        dest.writeByte((byte) (freezing ? 0x01 : 0x00));
        dest.writeString(precip);
        dest.writeString(wind);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WeatherData> CREATOR = new Parcelable.Creator<WeatherData>() {
        @Override
        public WeatherData createFromParcel(Parcel in) {
            return new WeatherData(in);
        }

        @Override
        public WeatherData[] newArray(int size) {
            return new WeatherData[size];
        }
    };
}

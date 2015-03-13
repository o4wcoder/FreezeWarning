package com.fourthwardcoder.android.freezewarning;

public interface Constants {

	static final String TAG_HOURLY = "hourly";
	static final String TAG_TEMP = "temperature";
	static final String TAG_DATA = "data";
	static final String TAG_TIME = "time";
	static final String TAG_ICON = "icon";
	static final String TAG_SUMMARY = "summary";
	static final String TAG_PRECIP = "precipProbability";
	static final String TAG_WIND_SPEED = "windSpeed";
	static final String TAG_WIND_BEARNING = "windBearing";
	
	//Wind Direction Angles
	static final double WIND_N = 0;
	static final double WIND_NNE = 22.5;
	static final double WIND_NE = 45;
	static final double WIND_ENE = 67.5;
	static final double WIND_E = 90;
	static final double WIND_ESE = 112.5;
	static final double WIND_SE = 135;
	static final double WIND_SSE = 157.5;
	static final double WIND_S = 180;
	static final double WIND_SSW = 202.5;
	static final double WIND_SW = 225;
	static final double WIND_WSW = 247.5;
	static final double WIND_W = 270;
	static final double WIND_WNW = 292.5;
	static final double WIND_NW = 315;
	static final double WIND_NNW = 337.5;
	
	static final int FREEZING_TEMP = 32;
	
	static final int HOURS_TO_CHECK = 24;
	
	
	//Extra Keys
	static final String EXTRA_WEATHER_DATA_LIST = "com.fourthwardcoder.android.freezewarning.weatherdata_list";
	static final String EXTRA_IS_FAHRENHEIT = "com.fourthwardcoder.android.freezewarning.is_fahrenheit";
	
}

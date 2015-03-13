package com.fourthwardcoder.android.freezewarning;

import java.util.Calendar;

public class NotifySettings {
	
	private boolean switchEnabled;
	private boolean tempFahrenheit;
	private int hour = 0;
	private int min = 0;
	
	public NotifySettings(boolean enabled, boolean tempFahrenheit, int hour, int min) {
		this.switchEnabled = enabled;
		this.tempFahrenheit = tempFahrenheit;
		this.hour = hour;
		this.min = min;
	}

	public boolean isTempFahrenheit() {
		return tempFahrenheit;
	}

	public void setTempFahrenheit(boolean tempFahrenheit) {
		this.tempFahrenheit = tempFahrenheit;
	}

	public boolean isSwitchEnabled() {
		return switchEnabled;
	}

	public void setSwitchEnabled(boolean enabled) {
		this.switchEnabled = enabled;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}
	
	public long getMillisecondTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, min);
		
		return calendar.getTimeInMillis();
	}
	

}

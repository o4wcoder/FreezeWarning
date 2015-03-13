package com.fourthwardcoder.android.freezewarning;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HourlyWeatherFragment extends Fragment implements Constants{

	/***********************************************************************/
	/*                             Constants                               */
	/***********************************************************************/
	private static final String TAG = "HourlyWeatherFragment";

	/***********************************************************************/
	/*                            Local Data                               */
	/***********************************************************************/
	ArrayList<WeatherData> weatherDataList;
	HourlyDataListAdapter adapter;
	TextView dateTextView;
	boolean isFahrenheit;
	//holds position of header in listview
	private int headerPosition = 0;

	/***********************************************************************/
	/*                           Override Methods                          */
	/***********************************************************************/
	@Override
	public void onCreate(Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);

		Log.d(TAG,"Inside onCreate");

		Intent intent = getActivity().getIntent();
		weatherDataList = intent.getParcelableArrayListExtra(EXTRA_WEATHER_DATA_LIST);
		isFahrenheit = intent.getBooleanExtra(EXTRA_IS_FAHRENHEIT, true);

		


		//retain the instance on rotation
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.fragment_hourly_weather, container, false);

		//TextView cityTextView = (TextView)view.findViewById(R.id.cityTextView);
		//TextView timeTextView = (TextView)view.findViewById(R.id.timeTextView);

		dateTextView = (TextView)view.findViewById(R.id.dateTextView);

		if(weatherDataList != null)
		{
			WeatherData wData = weatherDataList.get(0);
			if(wData != null)
			{
				//cityTextView.setText(wData.getCity());
				getActivity().getActionBar().setTitle("Hourly-" + wData.getCity() + " (" + wData.getZip() + ")");

				dateTextView.setText(wData.getFullDataTime());

				/* Need to add header for the day change. Duplicate the 12 AM data block
				 * and this position will be the header
				 */
				ArrayList<WeatherData> modList = new ArrayList<WeatherData>(HOURS_TO_CHECK + 1);
				
				for(int i = 0; i< weatherDataList.size();i++) {
					
					WeatherData weatherData = weatherDataList.get(i);
					
					if(weatherData.getHour().equals("12 AM")) {
						WeatherData headerData = weatherData;
						modList.add(headerData);
						headerPosition = i;
					}
					modList.add(weatherData);
				}
				
				
				ListView listView = (ListView)view.findViewById(R.id.hourlyDataList);		
				adapter = new HourlyDataListAdapter(modList,listView);
				listView.setAdapter(adapter);
			}
		}
		else {
			dateTextView.setText("No Data");
		}


		return view;
	}

	/***********************************************************/
	/*                     Inner Classes                        */
	/***********************************************************/
	
	//Class to hold different views of the listview. This helps
	//it run smoothly when scrolling
	private static class ViewHolder {

		public TextView timeTextView;
		public TextView precipTextView;
		public TextView windTextView;
		public TextView tempTextView;
		public TextView dateTextView;
	}

	//Adapter to show Crime specific data in the list
	private class HourlyDataListAdapter extends ArrayAdapter<WeatherData> {

		private static final int FREEZING_ROW = 0;
		private static final int NOT_FREEZING_ROW = 1;
		private static final int HEADER_ROW = 2;
		
		ArrayList<WeatherData> hourlyData;
		ListView listView;
		
		//Constructor
		public HourlyDataListAdapter(ArrayList<WeatherData> hourlyData, ListView listView) {
			super(getActivity(), 0, hourlyData);
			
			this.hourlyData = hourlyData;
			this.listView = listView;
			
		        		
		}

		//Override method needed from multiple layouts in listview
		//Determines the type of layout to display in the row
		@Override
		public int getItemViewType(int position) {
			
			
			if(position == headerPosition)
				return HEADER_ROW;
			else if(hourlyData.get(position).isFreezing())
			    return FREEZING_ROW;
			else
				return NOT_FREEZING_ROW;
		}
		//Override method needed from multiple layouts in listview
		//Returns how many different layouts the listview can have
		@Override
		public int getViewTypeCount() {
			return 3;
		}
		//Override getView to return a view inflated from the custom
		//layout and inflated with Crime Data
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
            int rowType = getItemViewType(position);
            
            //Log.e(TAG,"VP: " + listView.getFirstVisiblePosition() +": " + getItem(listView.getFirstVisiblePosition()).getHour() + " hp: " + headerPosition);
        
			//If we weren't given a view, inflate one
            if (convertView == null) {
            	holder = new ViewHolder();

            	if(rowType == HEADER_ROW) {
            		convertView = getActivity().getLayoutInflater().inflate(R.layout.header_data_list_item,null);
            		holder.dateTextView = (TextView)convertView.findViewById(R.id.headerDateTextView);
            		holder.dateTextView.setText(getItem(position).getFullDataTime());
            	    
            	}
            	else {
            		//Select between normal row layout, or freezing row layout.
            		if(rowType == FREEZING_ROW)
            			convertView = getActivity().getLayoutInflater().inflate(R.layout.freezing_hourly_data_list_item,null);
            		else 
            			convertView = getActivity().getLayoutInflater().inflate(R.layout.hourly_data_list_item,null);

            		//Get views in row and store in ViewHolder
            		holder.timeTextView = (TextView)convertView.findViewById(R.id.timeTextView);
            		holder.precipTextView = (TextView)convertView.findViewById(R.id.precipTextView);
            		holder.windTextView = (TextView)convertView.findViewById(R.id.windTextView);
            		holder.tempTextView = (TextView)convertView.findViewById(R.id.tempTextView);
            	}
            	convertView.setTag(holder);
            }
			else {
				holder = (ViewHolder)convertView.getTag();
			}


            if(rowType != HEADER_ROW) {
            	//Set List Data
            	holder.timeTextView.setText(getItem(position).getHour());
            	holder.precipTextView.setText(getItem(position).getPrecip());
            	holder.windTextView.setText(getItem(position).getWind());	

            	if(isFahrenheit)
            		holder.tempTextView.setText(getItem(position).getHourlyTemp());
            	else
            		holder.tempTextView.setText(getItem(position).getCelsiusHourlyTemp());	
            }
            
            //Update Date at top of screen depending on what time is at the top of list
            if(listView.getFirstVisiblePosition() > headerPosition)
            	dateTextView.setText(getItem(headerPosition).getFullDataTime());
            else
            	dateTextView.setText(getItem(listView.getFirstVisiblePosition()).getFullDataTime());
            	
            return convertView;
		}

	}

}

package com.fourthwardcoder.android.freezewarning;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class HourlyWeatherActivity extends SingleFragmentActivity implements Constants {


	@Override
	protected Fragment createFragment() {
		// TODO Auto-generated method stub
		return new HourlyWeatherFragment();
	}

}

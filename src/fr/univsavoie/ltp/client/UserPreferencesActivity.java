package fr.univsavoie.ltp.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

public class UserPreferencesActivity extends PreferenceActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.user_preferences);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK) 
		{
			Intent result = new Intent("Complete");
			setResult(Activity.RESULT_OK, result);
			finish();
			return true;
		}
		return false;
	}
}

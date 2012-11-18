package fr.univsavoie.ltp.client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingsActivity extends Activity
{
	/*
	 * Variables globales
	 */
	SharedPreferences myPrefs;
	SharedPreferences.Editor prefsEditor;
	
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // Create settings activity
        setContentView(R.layout.activity_settings);
        
        // Instance de SharedPreferences pour enregistrer des données dans un fichier
        myPrefs = getSharedPreferences("UserPrefs", MODE_WORLD_READABLE);
        
        // CheckBox show minimap listner
        CheckBox chkBxShowMinimap = (CheckBox) findViewById( R.id.checkBoxShowMiniMap );
        chkBxShowMinimap.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
            	prefsEditor = myPrefs.edit();
            	prefsEditor.putBoolean("DisplayMinimap", isChecked);
            	prefsEditor.commit();
            }
        });
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ( keyCode == KeyEvent.KEYCODE_MENU) {
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
}

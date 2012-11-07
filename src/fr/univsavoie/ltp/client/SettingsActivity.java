package fr.univsavoie.ltp.client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingsActivity extends MainActivity
{
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // Create settings activity
        setContentView(R.layout.activity_settings);
        
        // CheckBox show minimap listner
        CheckBox chkBxShowMinimap = (CheckBox) findViewById( R.id.checkBoxShowMiniMap );
        chkBxShowMinimap.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
            	MainActivity.enableMinimap(isChecked);
            }
        });
    }
}

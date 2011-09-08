package com.jn.android.guvnor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import android.widget.TextView;

public class ShowSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String _TAG = "ShowSettingsActivity";
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.show_settings_layout);
		addPreferencesFromResource(R.xml.preferences);
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    // Unregister the listener whenever a key changes            
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Preference pref = findPreference(key);
		
		Log.v(_TAG, "pref key " + key + " changed");
		SharedPreferences preferences = getSharedPreferences(key, 0);
		preferences.registerOnSharedPreferenceChangeListener(this);

	}
}

package com.jn.android.guvnor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.TextView;

public class ShowSettingsActivity extends PreferenceActivity {
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.show_settings_layout);
		addPreferencesFromResource(R.xml.preferences);
	}
}

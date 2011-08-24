package com.jn.android.guvnor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GuvnorAndroidClientActivity extends Activity implements ResultHandler {
	
	private static final String _TAG="GuvnorClientActivity";
   
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /** Get Service Helper instance, and register the result handler */
        ServiceHelper serviceHelper = ServiceHelper.getInstance();
        serviceHelper.setContext(this);
        serviceHelper.registerResultHandler(this);
    }
    
    /** From ResultHandler */
    public void handleResult(int resultCode) {
    	/** Do something with the results */
    	Log.v(_TAG,"Results received.");
    }
    
    /** From ListActivity */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 0, 0, "Show settings");
		menu.add(Menu.NONE, 1, 1, "Update");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 0: {
				startActivity(new Intent(this, ShowSettingsActivity.class));
				return true;
			}
			case 1: {
				ServiceHelper.getInstance().doService();
			}
		}
		return false;
	}
}
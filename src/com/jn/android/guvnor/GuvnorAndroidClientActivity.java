package com.jn.android.guvnor;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;

public class GuvnorAndroidClientActivity extends ListActivity implements ResultHandler {
	
	private static final String _TAG="GuvnorClientActivity";
	private static final String[] listProjection = new String[] {
			DataProvider.C_PACKAGE_ID,
			DataProvider.C_PACKAGE_TITLE
	};
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /** Get Service Helper instance, and register the result handler */
        ServiceHelper serviceHelper = ServiceHelper.getInstance();
        serviceHelper.setContext(this);
        serviceHelper.registerResultHandler(this);
       
        Cursor cursor = managedQuery(DataProvider.CONTENT_URI_PACKAGE, 
        							listProjection,
        							null,
        							null,
        							DataProvider.C_PACKAGE_ID);
        String[] dataColumns = { DataProvider.C_PACKAGE_TITLE };
        int[] viewIDs = { android.R.id.text1 };
        SimpleCursorAdapter simpleCursorAdapter = 
        		new SimpleCursorAdapter(this, 
        		R.layout.packagelist_item, 
        		cursor, 
        		dataColumns,
        		viewIDs);
        setListAdapter(simpleCursorAdapter);
    }
    
    /** From ResultHandler */
    public void handleResult(int resultCode) {
    	/** Do something with the results */
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
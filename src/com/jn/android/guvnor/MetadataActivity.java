package com.jn.android.guvnor;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class MetadataActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.metadata_layout);
	}

	@Override
	protected void onStart() {
		super.onStart();
		long id = this.getIntent().getExtras().getLong("itemId");
		ContentResolver cr = getContentResolver();
		String[] projection = new String[] { 
				DataProvider.C_METADATA_UUID, 
				DataProvider.C_METADATA_STATE, 
				DataProvider.C_METADATA_CREATED, 
				DataProvider.C_METADATA_LASTCONTRIB, 
				DataProvider.C_METADATA_LASTMODIFIED 
			}; 
		String queryId = Long.toString(id);
		Cursor c = cr.query(DataProvider.CONTENT_URI_METADATA, projection, DataProvider.C_METADATA_FK + "=" + queryId, null, DataProvider.C_METADATA_UUID);
		c.moveToFirst();
	
		TextView uuidView = (TextView)findViewById(R.id.uuidSmall);
		TextView stateView = (TextView)findViewById(R.id.stateSmall);
		TextView createdView = (TextView)findViewById(R.id.createdSmall);
		TextView lastContribView = (TextView)findViewById(R.id.lastContribSmall);
		TextView lastModifiedView = (TextView)findViewById(R.id.lastModifiedSmall);
	
		uuidView.setText(c.getString(0));
		stateView.setText(c.getString(1));
		createdView.setText(c.getString(2));
		lastContribView.setText(c.getString(3));
		lastModifiedView.setText(c.getString(4));
		
		c.close();
	}	

	
}

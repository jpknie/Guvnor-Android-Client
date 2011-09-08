package com.jn.android.guvnor;

import java.util.HashMap;
import java.util.Set;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

/** PackageIdentResolver keeps track of every package's database id mapped BY uuid. */
public class PackageIdentResolver {
	private static final String _TAG = "PackageIdentResolver";
	private static HashMap<String, String> uuidPackageIdMap;
	private static HashMap<String, String> uuidAssetIdMap;
	private static ContentResolver contentResolver;
	private static boolean mapped;
	
	private static class SingletonHolder {
		public static final PackageIdentResolver instance = new PackageIdentResolver();
	}
	
	public PackageIdentResolver() {
		uuidPackageIdMap = new HashMap<String, String>();
		mapped = false;
	}
	
	public static PackageIdentResolver getInstance() {
		return SingletonHolder.instance;
	}
	
	public boolean isMapped() {
		return mapped;
	}
	
	public void setContentResolver(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
	}
	
	public String getIdByUuid(String uuid) {
		return uuidPackageIdMap.get(uuid);
	}
	
	public Set<String> getAssetUuidSet() {
		return null;
	}
	
	public Set<String> getPackageUuidSet() {
		return uuidPackageIdMap.keySet();
	}
	
	public void populateMappings() {
		if(contentResolver == null) return;
		String[] projection = new String[] { DataProvider.C_METADATA_ID, DataProvider.C_METADATA_UUID, DataProvider.C_METADATA_FK };
		Cursor cursor = contentResolver.query(
				DataProvider.CONTENT_URI_METADATA, 
				projection, 
				null,
				null, 
				DataProvider.C_METADATA_ID);
		
		/** if the mappings exist, we clear them before inserting */
		if(uuidPackageIdMap != null) uuidPackageIdMap.clear();
		
		while(cursor.moveToNext()) {
			uuidPackageIdMap.put(cursor.getString(1), cursor.getString(2));
			Log.v(_TAG,"added mapping " + cursor.getString(1) + "->" + cursor.getString(2));
		}
		cursor.close();
		mapped = true;
	}
	
}

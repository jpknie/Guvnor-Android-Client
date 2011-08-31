package com.jn.android.guvnor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Thread;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

class ServiceRequestException extends Throwable {
	
	private String errorMessage;
	
	public ServiceRequestException(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
}

class UnknownItemTypeException extends Throwable {
private String errorMessage;
	
	public UnknownItemTypeException(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
}

public class Processor extends Thread {
	
	private static final String _TAG = "Processor";
	private static HashMap<String, String> uuidPkgKeyMap;
	private static Map<String, ParserHandler> handlers;
	private ContentResolver contentResolver;
	private ParserHandler parserHandler;
	private boolean useGzipCompression;
	private String itemType;
	private Context context;
	private String url;
	
	static {
		handlers = new HashMap<String, ParserHandler>();
		handlers.put("packages", new PackageParserHandler());
	}
	
	public Processor(Context ctx, String url, String itemType, boolean useGzipCompression) {
		this.context = ctx;
		this.url = url;
		this.itemType = itemType;
		this.useGzipCompression = useGzipCompression;
		contentResolver = this.context.getContentResolver();
	}
	
	public void setItemType(String it) throws UnknownItemTypeException {
		itemType = it;
		parserHandler = handlers.get(itemType);
	}
	
	public void setUrl(String address) {
		url = address;
	}
	
	private int insertPackagesToDB( HashMap<String, Package> uuidToPackageMap, HashSet<String> insertedUuids, ArrayList<ContentProviderOperation> operations, int index ) {
		int i = index;
		for(String uuidToInsert: insertedUuids) {
			Package packageToInsert = uuidToPackageMap.get(uuidToInsert);
			operations.add(ContentProviderOperation.newInsert(DataProvider.CONTENT_URI_PACKAGE)
					.withValue(DataProvider.C_PACKAGE_TITLE, packageToInsert.title)
					.withValue(DataProvider.C_PACKAGE_DESCRIPTION, packageToInsert.description)
					.withValue(DataProvider.C_PACKAGE_VERSION, packageToInsert.version)
					.withValue(DataProvider.C_PACKAGE_CHECKINCOMMENT, packageToInsert.checkInComment)
					.build());
			i++;
			operations.add(ContentProviderOperation.newInsert(DataProvider.CONTENT_URI_METADATA)
					.withValueBackReference(DataProvider.C_METADATA_FK, i - 1)
					.withValue(DataProvider.C_METADATA_UUID, packageToInsert.metaData.uuid)
					.withValue(DataProvider.C_METADATA_LASTMODIFIED, packageToInsert.metaData.lastModified)
					.withValue(DataProvider.C_METADATA_STATE, packageToInsert.metaData.state)
					.withValue(DataProvider.C_METADATA_LASTCONTRIB, packageToInsert.metaData.lastContributor)
					.withValue(DataProvider.C_METADATA_CREATED, packageToInsert.metaData.created)
					.build());
			i++;
		}
		return i;
	}
	
	private int updatePackagesToDB( HashMap<String, Package> uuidToPackageMap, HashSet<String> updatedUuids, ArrayList<ContentProviderOperation> operations, int index ) {
		int i = index;
		
		for(String uuidToUpdate: updatedUuids) {		
			Package packageToUpdate = uuidToPackageMap.get(uuidToUpdate);
			String packageId = uuidPkgKeyMap.get(uuidToUpdate);
			operations.add(ContentProviderOperation.newUpdate(DataProvider.CONTENT_URI_PACKAGE)
					.withSelection(DataProvider.C_PACKAGE_ID + "=?", new String[]{String.valueOf(packageId)})
					.withValue(DataProvider.C_PACKAGE_TITLE, packageToUpdate.title)
					.withValue(DataProvider.C_PACKAGE_DESCRIPTION, packageToUpdate.description)
					.withValue(DataProvider.C_PACKAGE_VERSION, packageToUpdate.version)
					.withValue(DataProvider.C_PACKAGE_CHECKINCOMMENT, packageToUpdate.checkInComment)
					.build());
			i++;
			operations.add(ContentProviderOperation.newUpdate(DataProvider.CONTENT_URI_METADATA)
					//.withValueBackReference(DataProvider.C_METADATA_FK, i - 1)
					.withSelection(DataProvider.C_METADATA_UUID + "=?", new String[]{String.valueOf(packageToUpdate.metaData.uuid)})
					.withValue(DataProvider.C_METADATA_LASTMODIFIED, packageToUpdate.metaData.lastModified)
					.withValue(DataProvider.C_METADATA_STATE, packageToUpdate.metaData.state)
					.withValue(DataProvider.C_METADATA_LASTCONTRIB, packageToUpdate.metaData.lastContributor)
					.withValue(DataProvider.C_METADATA_CREATED, packageToUpdate.metaData.created)
					.build());
			i++; 
		}
		return i;
	}
	
	private int deletePackagesFromDB( HashMap<String, String> uuidToPackageMap, HashSet<String> deletedUuids, ArrayList<ContentProviderOperation> operations, int index ) {
		int i = index;
		
		for(String uuidToDelete: deletedUuids) {		
			String packageIdToDelete = uuidToPackageMap.get(uuidToDelete);
			operations.add(ContentProviderOperation.newDelete(DataProvider.CONTENT_URI_METADATA)
					.withSelection(DataProvider.C_METADATA_FK + "=?", new String[]{String.valueOf(packageIdToDelete)}).build());
			i++;
			operations.add(ContentProviderOperation.newDelete(DataProvider.CONTENT_URI_PACKAGE)
					.withSelection(DataProvider.C_PACKAGE_ID + "=?", new String[]{String.valueOf(packageIdToDelete)}).build());
			i++;
		}
		return i;
	}
	
	/** Build database update operation(s) based on package list from server */
	private void updateDatabase ( HashMap<String, Package> packagesFromServer ) {
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		HashSet<String> serverUuidSet = new HashSet<String>(packagesFromServer.keySet());
		HashSet<String> clientUuidSet = new HashSet<String>(uuidPkgKeyMap.keySet());
		
		int index = 0;
		HashSet<String> deletedUuids = new HashSet<String>(clientUuidSet);
		deletedUuids.removeAll(serverUuidSet);
		index = deletePackagesFromDB( uuidPkgKeyMap, deletedUuids, operations, index );
		HashSet<String> updatedUuids = new HashSet<String>(serverUuidSet);
		updatedUuids.retainAll(clientUuidSet);
		index = updatePackagesToDB( packagesFromServer, updatedUuids, operations, index );
		serverUuidSet.removeAll(updatedUuids);
		index = insertPackagesToDB( packagesFromServer, serverUuidSet, operations, index );
		
		try {
			contentResolver.applyBatch(DataProvider.PROVIDER_NAME, operations);
			uuidPkgKeyMap.clear();
			populateUuidToPackageMapping();
		} 
		catch(RemoteException re) {}
		catch(OperationApplicationException oae) {}
	}
	
	private void populateUuidToPackageMapping()
	{
		String[] projection = new String[] { DataProvider.C_METADATA_ID, DataProvider.C_METADATA_UUID, DataProvider.C_METADATA_FK };
		uuidPkgKeyMap = new HashMap<String, String>();
		Cursor cursor = contentResolver.query(
				DataProvider.CONTENT_URI_METADATA, 
				projection, 
				null,
				null, 
				DataProvider.C_METADATA_ID);
		
		while(cursor.moveToNext()) {
			uuidPkgKeyMap.put(cursor.getString(1), cursor.getString(2));
		}
		
		cursor.close();
	}
	
	@Override
	public void run() {
			if(uuidPkgKeyMap == null) {
				populateUuidToPackageMapping();
			}
		
		 	Log.v(_TAG,"Processor runs");
			try {
				String response = RestMethod.doGet(url, useGzipCompression);
				HashMap<String, Package> packages = (HashMap<String, Package>)parserHandler.parse(response);
				updateDatabase(packages);
			}
			catch(ClientProtocolException ce) {
				Log.v(_TAG, "ClientProtocolException");
				
			}
			catch(URISyntaxException use) {
				Log.v(_TAG, "URISyntaxException");

			}
			catch(ParserException pe) {
				Log.v(_TAG, "ParserException");

			} 
			catch(IOException ioe) {
				Log.v(_TAG, "IOException");

			}
		} 
}

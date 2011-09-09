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
	private static Map<String, ParserHandler> handlers;
	
	private ParserHandler parserHandler;
	private boolean useGzipCompression;
	private String itemType;
	private Context context;
	private ContentResolver contentResolver;
	private String url;
	
	static {
		handlers = new HashMap<String, ParserHandler>();
		handlers.put("packages", new PackageParserHandler());
		// handlers.put("assets", new AssetParserHandler())
	}
	
	public Processor(Context ctx, String url, String itemType, boolean useGzipCompression, ContentResolver contentResolver) {
		this.context = ctx;
		this.url = url;
		this.itemType = itemType;
		this.useGzipCompression = useGzipCompression;
		this.contentResolver = contentResolver;
	}
	
	public void setItemType(String it) throws UnknownItemTypeException {
		itemType = it;
		parserHandler = handlers.get(itemType);
	}
	
	public void setUrl(String address) {
		url = address;
	}
	
	private void buildUpdateLists ( ArrayList<PersistentResource> deleteList, 
									ArrayList<PersistentResource> updateList, 
									ArrayList<PersistentResource> insertList,
									HashSet<String> serverUuidSet,
									HashSet<String> clientUuidSet,
									HashMap<String, PersistentResource> resourcesFromServer) {
		
		HashSet<String> deletedUuids = new HashSet<String>(clientUuidSet);
		deletedUuids.removeAll(serverUuidSet);
		
		for(String uuidToDelete: deletedUuids) {
			deleteList.add(Package.buildFromUuid(uuidToDelete));
		}
		
		HashSet<String> updatedUuids = new HashSet<String>(serverUuidSet);
		updatedUuids.retainAll(clientUuidSet);
		
		for(String uuidToUpdate: updatedUuids) {
			updateList.add(resourcesFromServer.get(uuidToUpdate));
		}

		serverUuidSet.removeAll(updatedUuids);
		for(String uuidToInsert: serverUuidSet) {
			insertList.add(resourcesFromServer.get(uuidToInsert));	
		}
		
	}
	
	/** Build database update operation(s) based on package list from server */
	private void updatePackagesToDatabase ( HashMap<String, PersistentResource> packagesFromServer ) {
		HashSet<String> serverUuidSet = new HashSet<String>(packagesFromServer.keySet());
		HashSet<String> clientUuidSet = new HashSet<String>(PackageIdentResolver.getInstance().getPackageUuidSet());
		
		ArrayList<PersistentResource> insertList = new ArrayList<PersistentResource>();
		ArrayList<PersistentResource> deleteList = new ArrayList<PersistentResource>();
		ArrayList<PersistentResource> updateList = new ArrayList<PersistentResource>();
		
		buildUpdateLists(deleteList, updateList, insertList, serverUuidSet, clientUuidSet, packagesFromServer);
		PersistentResourceManager prm = PersistentResourceManager.getInstance();
		prm.handleUpdates(updateList, deleteList, insertList);
	}
	
	@Override
	public void run() {
		 	Log.v(_TAG,"Processor runs");
			try {
				PackageIdentResolver packageIdentResolver = PackageIdentResolver.getInstance();
				packageIdentResolver.setContentResolver(contentResolver);
				packageIdentResolver.populateMappings();
				String response = RestMethod.doGet(url, useGzipCompression);
				HashMap<String, PersistentResource> packages = (HashMap<String, PersistentResource>)parserHandler.parse(response);
				updatePackagesToDatabase(packages);
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

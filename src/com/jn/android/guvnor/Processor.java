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
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
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

class UnknownMediaTypeException extends Throwable {
	private static final String errorMessage = "Given mediatype isn't supported or badly formed";
	public String getErrorMessage() {
		return errorMessage;
	}
}

public class Processor extends Thread {
	
	private static final String _TAG = "Processor";
	private static Map<String, ParserHandler> handlers;
	private Context context;
	private String url;
	private String mediaType;
	private ParserHandler parserHandler;
	private boolean useGzipCompression;
	
	static {
		handlers = new HashMap<String, ParserHandler>();
		handlers.put("application/json", new JSONParserHandler());
	}
	
	public Processor(Context ctx, String url, String mediaType, boolean useGzipCompression) {
		this.context = ctx;
		this.url = url;
		this.mediaType = mediaType;
		this.useGzipCompression = useGzipCompression;
	}
	
	public void setMediaType(String mt) throws UnknownMediaTypeException {
		mediaType = mt;
		parserHandler = handlers.get(mediaType);
	}
	
	public void setUrl(String address) {
		url = address;
	}
	
	@Override
	public void run() {
		try {
			String response = RestMethod.doGet(url, mediaType, useGzipCompression);
			Package[] packages = (Package[])parserHandler.parse(response);
			if(packages.length > 0) {
				
				ContentResolver cr = context.getContentResolver();
				
				for(int i = 0; i < packages.length; i++) {
					ContentValues packageValues = new ContentValues();
					packageValues.put(DataProvider.C_PACKAGE_TITLE, packages[i].title);
					packageValues.put(DataProvider.C_PACKAGE_DESCRIPTION, packages[i].description);
					packageValues.put(DataProvider.C_PACKAGE_CHECKINCOMMENT, packages[i].checkInComment);
					packageValues.put(DataProvider.C_PACKAGE_VERSION, packages[i].version);
					Uri resultUri = cr.insert(DataProvider.CONTENT_URI_PACKAGE, packageValues);
					String packageId = resultUri.getPathSegments().get(1);
					ContentValues metaDataValues = new ContentValues();
					metaDataValues.put(DataProvider.C_METADATA_FK, packageId); /** Make foreign key */
					metaDataValues.put(DataProvider.C_METADATA_CREATED, packages[i].metaData.created);
					metaDataValues.put(DataProvider.C_METADATA_LASTMODIFIED, packages[i].metaData.lastModified);
					metaDataValues.put(DataProvider.C_METADATA_STATE, packages[i].metaData.state);
					metaDataValues.put(DataProvider.C_METADATA_LASTCONTRIB, packages[i].metaData.lastContributor);
					metaDataValues.put(DataProvider.C_METADATA_UUID, packages[i].metaData.uuid);
					Uri insertResultUri = cr.insert(DataProvider.CONTENT_URI_METADATA, metaDataValues);
					Log.v(_TAG, "metadata inserted with uri: " + insertResultUri.toString());
				}
			}
		} 
		catch(ClientProtocolException ce) {
	
		}
		catch(IOException ioe) {
			
		}
		catch(URISyntaxException use) {
		
		}
		catch(ParserException pe) {
			
		}
	}
	
}

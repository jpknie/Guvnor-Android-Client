package com.jn.android.guvnor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Thread;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

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
	
	private String url;
	private String mediaType;
	private ParserHandler parserHandler;
	private boolean useGzipCompression;
	
	static {
		handlers = new HashMap<String, ParserHandler>();
		handlers.put("application/json", new JSONParserHandler());
	}
	
	public Processor(String url, String mediaType, boolean useGzipCompression) {
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
			Log.v(_TAG, "Processor runs");
			String response = RestMethod.doGet(url, mediaType, useGzipCompression);
			parserHandler.parse(response);
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

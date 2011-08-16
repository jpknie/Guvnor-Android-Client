package com.jn.android.guvnor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class RestMethod {
	public static final String _TAG = "RestMethod";
	
	private String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

	public void doGet(String url) {
		try {
			URI uri = new URI(url);
		
		HttpGet getMethod = new HttpGet(uri);
		getMethod.addHeader("Accept","application/json");

		/*
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("username","your user name");
	    	nameValuePairs.add(new BasicNameValuePair("password","your password");
		*/
		//postMethod.setEntity()
		HttpClient hc = new DefaultHttpClient();

			HttpResponse response = hc.execute(getMethod);
			HttpEntity entity = response.getEntity();
			if(entity != null) {
				InputStream inStream = entity.getContent();
				String result = convertStreamToString(inStream);
				Log.v(_TAG, "HttpPostMethodResult" + result);
			}
		} catch(ClientProtocolException e) {
			Log.v(_TAG,"clientprotocolexception");
		}
		catch(IOException ioe) {
			
		}
		catch(URISyntaxException use) {}
	}

}


package com.jn.android.guvnor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class RestMethod {
	public static final String _TAG = "RestMethod";
	public String acceptType;
	
	private static String convertStreamToString(InputStream is) {
      
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

	public static String doGet(String url, String acceptType, boolean useGzip) throws ClientProtocolException, IOException, URISyntaxException {
		try {
			URI uri = new URI(url);
			HttpGet getMethod = new HttpGet(uri);
			getMethod.addHeader("Accept", acceptType);
			if(useGzip)	getMethod.addHeader("Accept-Encoding", "gzip");

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
				Header contentEncoding = response.getFirstHeader("Content-Encoding");
				if(contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
					inStream = new GZIPInputStream(inStream);
				}
				String result = convertStreamToString(inStream);
				return result;
			}
			
		/** Re-throw any exceptions and handle them "upper level" */
		} catch(ClientProtocolException e) {
			throw e;
		}
		catch(IOException ioe) {
			throw ioe;
		}
		catch(URISyntaxException use) {
			throw use;
		}
		return null;
	}

}


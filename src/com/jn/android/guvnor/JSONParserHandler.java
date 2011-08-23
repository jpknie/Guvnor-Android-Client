package com.jn.android.guvnor;

import java.util.ArrayList;
import java.util.LinkedHashSet;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;
import org.json.JSONObject;

public class JSONParserHandler implements ParserHandler {
	private static final String _TAG = "JSONParserHandler";
	private JSONArray jArray;
	
	public JSONParserHandler() {}
	
	public void parse(String content) throws ParserException {
		try {
			Log.v(_TAG,"parser runs");
			JSONObject jo = new JSONObject(content);
			JSONArray packageArray = jo.getJSONArray("package");
			
			int len = packageArray.length();
			if(len == 0) return;
			ArrayList<Package> packageList = new ArrayList<Package>();
			for(int i = 0 ; i < len; i++) {
				Package p = new Package();
				p.assets = new LinkedHashSet<Uri>();
				JSONObject packageObject = (JSONObject)packageArray.get(i);
				p.binaryLink = Uri.parse(packageObject.getString("binaryLink"));
				p.sourceLink = Uri.parse(packageObject.getString("sourceLink"));
				p.title = packageObject.getString("title");
				p.checkInComment = packageObject.getString("checkInComment");
				p.description = packageObject.getString("description");
				p.version = packageObject.getLong("version");
				String assets = packageObject.getString("assets");
				/** This is ugly kludge */
				if(assets.charAt(0) == '[') {	// we have array of assets
					JSONArray arrayOfAssets = packageObject.getJSONArray("assets");
					for(int j = 0; j < arrayOfAssets.length(); j++)
					{
						p.assets.add(Uri.parse(arrayOfAssets.getString(j)));
					}
				}
				else {	/** we have just one (or zero) asset(s) */
					if(assets != null) {
						p.assets.add(Uri.parse(assets));
					}
				}
				packageList.add(p);
			}
			
			for(Package testPackage: packageList) {
				Log.v(_TAG,testPackage.title);
				Log.v(_TAG,testPackage.description);
				Log.v(_TAG,"assets:");
				for(Uri assetUri: testPackage.assets) {
					Log.v(_TAG, assetUri.toString());
				}
			}
		} 
		catch(JSONException je) {
			Log.v(_TAG, "JSONException " + je.getMessage());
		}
	}
	
}

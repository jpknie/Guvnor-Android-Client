package com.jn.android.guvnor;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;
import org.json.JSONObject;

public class PackageParserHandler implements ParserHandler<Package> {
	private static final String _TAG = "PackageParserHandler";
	
	public PackageParserHandler() {}
	
	public Package[] parse(String content) throws ParserException {
		//ArrayList<Package> packageList = new ArrayList<Package>();
		Package[] packageList = null;
		
		try {
			JSONObject jo = new JSONObject(content);
			JSONArray packageArray = jo.getJSONArray("package");
			
			int len = packageArray.length();
			if(len == 0) return null;
			packageList = new Package[len];
			
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
				
				/** Parse metadata */
				JSONObject metadataObject = packageObject.getJSONObject("metadata");
				String created = metadataObject.getString("created");
				String lastModified = metadataObject.getString("lastModified");
				String state = metadataObject.getString("state");
				String uuid = metadataObject.getString("uuid");
				String lastContributor = metadataObject.getString("lastContributor");
				p.addMetaData(uuid, created, lastModified, lastContributor, state);
				
				String assets = packageObject.getString("assets");
				if(assets != null) {
					/** This is ugly kludge */
					if(assets.charAt(0) == '[') {	// we have array of assets
						JSONArray arrayOfAssets = packageObject.getJSONArray("assets");
						for(int j = 0; j < arrayOfAssets.length(); j++) {
							p.assets.add(Uri.parse(arrayOfAssets.getString(j)));
						}
					}
					else {	/** we have just one (or zero) asset(s) */
						p.assets.add(Uri.parse(assets));
					}
				}
				packageList[i] = p;
			}
		} 
		catch(JSONException je) {
			Log.v(_TAG, "JSONException " + je.getMessage());
		}
		return packageList;
	}
	
}

package com.jn.android.guvnor;

import java.util.Date;
import java.util.Set;

import android.content.ContentValues;
import android.net.Uri;

/** Defines package resource for Guvnor */
public class Package {
	public String title;
	public String description;
	public long version;
	public String checkInComment;
	/* public Category category */
	public Uri binaryLink;
	public Uri sourceLink;
	public Set<Uri> assets;
	public Package.PackageMetaData metaData;
	
	public Package() {
		metaData = new Package.PackageMetaData();
	}
	
	public ContentValues toContentValues() {
		ContentValues packageValues = new ContentValues();
		//packageValues.put(getString(R.string.column_package_title), title);
		packageValues.put(DataProvider.C_PACKAGE_TITLE, title);
		packageValues.put(DataProvider.C_PACKAGE_DESCRIPTION, description);
		packageValues.put(DataProvider.C_PACKAGE_CHECKINCOMMENT, checkInComment);
		packageValues.put(DataProvider.C_PACKAGE_VERSION, version);
		return packageValues;
	}
	
	public void addMetaData(String uuid, String created, String lastModified, String lastContributor, String state) {
		metaData.uuid = uuid;
		metaData.created = created;
		metaData.lastModified = lastModified;
		metaData.lastContributor = lastContributor;
		metaData.state = state;
	}
	
	class PackageMetaData {
		public String uuid;
		public String created;
		public String lastModified;
		public String lastContributor;
		public String state;
		public String foreignId;
		
		public ContentValues toContentValues() {
			ContentValues metaDataValues = new ContentValues();
			metaDataValues.put(DataProvider.C_METADATA_FK, foreignId); /** Make foreign key */
			metaDataValues.put(DataProvider.C_METADATA_CREATED, created);
			metaDataValues.put(DataProvider.C_METADATA_LASTMODIFIED, lastModified);
			metaDataValues.put(DataProvider.C_METADATA_STATE, state);
			metaDataValues.put(DataProvider.C_METADATA_LASTCONTRIB, lastContributor);
			metaDataValues.put(DataProvider.C_METADATA_UUID, uuid);
			return metaDataValues;
		}
	}
}

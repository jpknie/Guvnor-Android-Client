package com.jn.android.guvnor;


import java.util.List;
import java.util.Set;

import android.content.ContentProviderOperation;
import android.net.Uri;

/** Defines package resource for Guvnor */
public class Package implements PersistentResource {
	
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
	
	public static Package buildFromUuid(String uuid) {
		Package p = new Package();
		p.addMetaData(uuid, null, null, null, null);
		return p;
	}
	
	public void addMetaData(String uuid, String created, String lastModified, String lastContributor, String state) {
		metaData.uuid = uuid;
		metaData.created = created;
		metaData.lastModified = lastModified;
		metaData.lastContributor = lastContributor;
		metaData.state = state;
	}
	
	public String getUuid() {
		return metaData.uuid;
	}
	
	@Override
	public void addUpdateOperationTo(List<ContentProviderOperation> operationList) {	
		PackageIdentResolver packageIdentResolver = PackageIdentResolver.getInstance();
		String packageId = packageIdentResolver.getIdByUuid(metaData.uuid);
		operationList.add(ContentProviderOperation.newUpdate(DataProvider.CONTENT_URI_PACKAGE)
				.withSelection(DataProvider.C_PACKAGE_ID + "=?", new String[]{String.valueOf(packageId)})
				.withValue(DataProvider.C_PACKAGE_TITLE, title)
				.withValue(DataProvider.C_PACKAGE_DESCRIPTION, description)
				.withValue(DataProvider.C_PACKAGE_VERSION, version)
				.withValue(DataProvider.C_PACKAGE_CHECKINCOMMENT, checkInComment)
				.build());
		operationList.add(ContentProviderOperation.newUpdate(DataProvider.CONTENT_URI_METADATA)
				.withSelection(DataProvider.C_METADATA_UUID + "=?", new String[]{String.valueOf(metaData.uuid)})
				.withValue(DataProvider.C_METADATA_LASTMODIFIED, metaData.lastModified)
				.withValue(DataProvider.C_METADATA_STATE, metaData.state)
				.withValue(DataProvider.C_METADATA_LASTCONTRIB, metaData.lastContributor)
				.withValue(DataProvider.C_METADATA_CREATED, metaData.created)
				.build());
	}

	@Override
	public void addInsertOperationTo(List<ContentProviderOperation> operationList) {
		int i = operationList.size();
		operationList.add(ContentProviderOperation.newInsert(DataProvider.CONTENT_URI_PACKAGE)
				.withValue(DataProvider.C_PACKAGE_TITLE, title)
				.withValue(DataProvider.C_PACKAGE_DESCRIPTION, description)
				.withValue(DataProvider.C_PACKAGE_VERSION, version)
				.withValue(DataProvider.C_PACKAGE_CHECKINCOMMENT, checkInComment)
				.build());
		i++;
		operationList.add(ContentProviderOperation.newInsert(DataProvider.CONTENT_URI_METADATA)
				.withValueBackReference(DataProvider.C_METADATA_FK, i - 1)
				.withValue(DataProvider.C_METADATA_UUID, metaData.uuid)
				.withValue(DataProvider.C_METADATA_LASTMODIFIED, metaData.lastModified)
				.withValue(DataProvider.C_METADATA_STATE, metaData.state)
				.withValue(DataProvider.C_METADATA_LASTCONTRIB, metaData.lastContributor)
				.withValue(DataProvider.C_METADATA_CREATED, metaData.created)
				.build());
		i++;
	}

	@Override
	public void addDeleteOperationTo(List<ContentProviderOperation> operationList) {
		PackageIdentResolver packageIdentResolver = PackageIdentResolver.getInstance();
		String packageIdToDelete = packageIdentResolver.getIdByUuid(metaData.uuid);
		operationList.add(ContentProviderOperation.newDelete(DataProvider.CONTENT_URI_METADATA)
				.withSelection(DataProvider.C_METADATA_FK + "=?", new String[]{String.valueOf(packageIdToDelete)}).build());
		operationList.add(ContentProviderOperation.newDelete(DataProvider.CONTENT_URI_PACKAGE)
				.withSelection(DataProvider.C_PACKAGE_ID + "=?", new String[]{String.valueOf(packageIdToDelete)}).build());
	}

	class PackageMetaData {
		public String uuid;
		public String created;
		public String lastModified;
		public String lastContributor;
		public String state;
		public String foreignId;
	}
}

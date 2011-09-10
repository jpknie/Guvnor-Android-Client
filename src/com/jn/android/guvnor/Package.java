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
	public Package.PackageMetadata metadata;
	
	public Package() {
		metadata = new Package.PackageMetadata();
	}
	
	public static Package buildFromUuid(String uuid) {
		Package p = new Package();
		p.addMetaData(uuid, null, null, null, null);
		return p;
	}
	
	public void addMetaData(String uuid, String created, String lastModified, String lastContributor, String state) {
		metadata.uuid = uuid;
		metadata.created = created;
		metadata.lastModified = lastModified;
		metadata.lastContributor = lastContributor;
		metadata.state = state;
	}
	
	public String getUuid() {
		return metadata.uuid;
	}
	
	@Override
	public void addUpdateOperationTo(List<ContentProviderOperation> operationList) {	
		PackageIdentResolver packageIdentResolver = PackageIdentResolver.getInstance();
		String packageId = packageIdentResolver.getIdByUuid(metadata.uuid);
		operationList.add(ContentProviderOperation.newUpdate(DataProvider.CONTENT_URI_PACKAGE)
				.withSelection(DataProvider.C_PACKAGE_ID + "=?", new String[]{String.valueOf(packageId)})
				.withValue(DataProvider.C_PACKAGE_TITLE, title)
				.withValue(DataProvider.C_PACKAGE_DESCRIPTION, description)
				.withValue(DataProvider.C_PACKAGE_VERSION, version)
				.withValue(DataProvider.C_PACKAGE_CHECKINCOMMENT, checkInComment)
				.build());
		operationList.add(ContentProviderOperation.newUpdate(DataProvider.CONTENT_URI_METADATA)
				.withSelection(DataProvider.C_METADATA_UUID + "=?", new String[]{String.valueOf(metadata.uuid)})
				.withValue(DataProvider.C_METADATA_LASTMODIFIED, metadata.lastModified)
				.withValue(DataProvider.C_METADATA_STATE, metadata.state)
				.withValue(DataProvider.C_METADATA_LASTCONTRIB, metadata.lastContributor)
				.withValue(DataProvider.C_METADATA_CREATED, metadata.created)
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
				.withValue(DataProvider.C_METADATA_UUID, metadata.uuid)
				.withValue(DataProvider.C_METADATA_LASTMODIFIED, metadata.lastModified)
				.withValue(DataProvider.C_METADATA_STATE, metadata.state)
				.withValue(DataProvider.C_METADATA_LASTCONTRIB, metadata.lastContributor)
				.withValue(DataProvider.C_METADATA_CREATED, metadata.created)
				.build());
		i++;
	}

	@Override
	public void addDeleteOperationTo(List<ContentProviderOperation> operationList) {
		PackageIdentResolver packageIdentResolver = PackageIdentResolver.getInstance();
		String packageIdToDelete = packageIdentResolver.getIdByUuid(metadata.uuid);
		operationList.add(ContentProviderOperation.newDelete(DataProvider.CONTENT_URI_METADATA)
				.withSelection(DataProvider.C_METADATA_FK + "=?", new String[]{String.valueOf(packageIdToDelete)}).build());
		operationList.add(ContentProviderOperation.newDelete(DataProvider.CONTENT_URI_PACKAGE)
				.withSelection(DataProvider.C_PACKAGE_ID + "=?", new String[]{String.valueOf(packageIdToDelete)}).build());
	}

	class PackageMetadata {
		public String uuid;
		public String created;
		public String lastModified;
		public String lastContributor;
		public String state;
		public String foreignId;
	}
}

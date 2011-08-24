package com.jn.android.guvnor;

import java.util.Date;
import java.util.Set;
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
	
	class PackageMetaData {
		public String uuid;
		public Date created;
		public Date lastModified;
		public String lastContributor;
		public String state;
	}
}

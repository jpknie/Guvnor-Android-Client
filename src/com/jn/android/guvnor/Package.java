package com.jn.android.guvnor;

import java.util.Date;
import java.util.Set;
import android.net.Uri;

/** Defines package resource for Guvnor */
public class Package {
	private String title;
	private String description;
	private long version;
	private String checkInComment;
	/* private Category category */
	private Uri binaryLink;
	private Uri sourceLink;
	private Set<Uri> assets;
	private Package.PackageMetaData metaData;
	
	class PackageMetaData {
		private String uuid;
		private Date created;
		private Date lastModified;
		private String lastContributor;
		private String state;
	}
}

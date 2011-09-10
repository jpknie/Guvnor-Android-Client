package com.jn.android.guvnor;

import java.util.List;

import android.content.ContentProviderOperation;

/** This interface describes resource which is able to provide update, delete and insert operations for ContentProvider */
public interface PersistentResource {
		public void addUpdateOperationTo(List<ContentProviderOperation> operationList);
		public void addInsertOperationTo(List<ContentProviderOperation> operationList);
		public void addDeleteOperationTo(List<ContentProviderOperation> operationList);
}

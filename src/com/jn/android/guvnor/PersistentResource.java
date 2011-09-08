package com.jn.android.guvnor;

import java.util.ArrayList;

import android.content.ContentProviderOperation;

/** This interface describes resource which is able to provide update, delete and insert operations for ContentProvider */
public interface PersistentResource {
		public void addUpdateOperationTo(ArrayList<ContentProviderOperation> operationList);
		public void addInsertOperationTo(ArrayList<ContentProviderOperation> operationList);
		public void addDeleteOperationTo(ArrayList<ContentProviderOperation> operationList);
}

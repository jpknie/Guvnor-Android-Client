package com.jn.android.guvnor;

import java.util.ArrayList;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.RemoteException;

/** PersistentResourceManager is responsible to update, delete and insert PersistentResources into ContentProvider */
public class PersistentResourceManager {
	private static ContentResolver contentResolver;
	private static ArrayList<ContentProviderOperation> updateOperations;
	
	private static class SingletonHolder {
		public static final PersistentResourceManager instance = new PersistentResourceManager();
	}
	
	public PersistentResourceManager() {
	}
	
	public static void setContentResolver(ContentResolver cr) {
		contentResolver = cr;
	}
	
	public static PersistentResourceManager getInstance() {
		return SingletonHolder.instance;
	}
	
	public static void handleUpdates ( ArrayList<PersistentResource> updateList, 
								ArrayList<PersistentResource> deleteList, 
								ArrayList<PersistentResource> insertList ) {
		if(updateOperations == null) updateOperations = new ArrayList<ContentProviderOperation>();
		updateOperations.clear();
		deletePersistentResources(deleteList);
		updatePersistentResources(updateList);
		insertPersistentResources(insertList);
		try {
			contentResolver.applyBatch(DataProvider.PROVIDER_NAME, updateOperations);
			PackageIdentResolver.getInstance().populateMappings();
		}
		catch(RemoteException re) {}
		catch(OperationApplicationException oae) {}
	}
	
	private static void insertPersistentResources( ArrayList<PersistentResource> resourcesToInsert ) {	
		for(PersistentResource resourceToInsert: resourcesToInsert) {		
			resourceToInsert.addInsertOperationTo(updateOperations);
		}
	}
	
	private static void updatePersistentResources( ArrayList<PersistentResource> resourcesToUpdate ) {
		for(PersistentResource resourceToUpdate: resourcesToUpdate) {		
			resourceToUpdate.addUpdateOperationTo(updateOperations);
		}
	}
	
	private static void deletePersistentResources( ArrayList<PersistentResource> resourcesToDelete ) {
			for(PersistentResource resourceToDelete: resourcesToDelete) {		
				resourceToDelete.addDeleteOperationTo(updateOperations);
			}	
	}
	
}

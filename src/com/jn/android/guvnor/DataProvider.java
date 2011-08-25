package com.jn.android.guvnor;

import java.util.ArrayList;

import android.net.Uri;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.UriMatcher;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.SQLException;
import android.text.TextUtils;

import android.util.Log;

public class DataProvider extends ContentProvider {
	
	/** Database related constants */
	private static final String DATABASE_NAME = "guvnor_data.db";
	private static final int DATABASE_VERSION = 1;
	private static final String PACKAGE_TABLE_NAME = "packages";
	private static final String METADATA_TABLE_NAME = "metadata";
	
	/** Content provider constants */
	public static final String _TAG = "ContentProvider";
	public static final String PROVIDER_NAME = "com.jn.android.guvnor.dataprovider";
	public static final Uri CONTENT_URI_PACKAGE = Uri.parse("content://" + PROVIDER_NAME + "/packages");
	public static final Uri CONTENT_URI_METADATA = Uri.parse("content://" + PROVIDER_NAME + "/metadata");
	public static final String _ID = "_id";
	
	/** For package table */
	public static final int PACKAGES = 1;
	public static final int PACKAGE_ID = 2;
	
	/** For metadata table */
	public static final int METADATA = 2;
	public static final int METADATA_ID = 3;

	public static final UriMatcher uriMatcher;
	
	/** Package table constants */
	public static final String C_PACKAGE_ID = "_id";
	public static final String C_PACKAGE_TITLE = "title";
	public static final String C_PACKAGE_DESCRIPTION = "description";
	public static final String C_PACKAGE_CHECKINCOMMENT = "checkInComment";
	public static final String C_PACKAGE_VERSION = "version";
	
	public static final String C_METADATA_ID = "_id";
	public static final String C_METADATA_CREATED = "created";
	public static final String C_METADATA_LASTCONTRIB = "lastContributor";
	public static final String C_METADATA_LASTMODIFIED = "lastModified";
	public static final String C_METADATA_STATE = "state";
	public static final String C_METADATA_UUID = "uuid";
	public static final String C_METADATA_FK = "fkId"; /** Foreign key => package table */
	
	public static final String STATUS_COLUMN = "status";
	public static final String RESULT_COLUMN = "result";
	
	/** Status flags for resources */
	enum States {
		STATE_POSTING,
		STATE_UPDATING,
		STATE_DELETING
	}
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "packages", PACKAGES);
		uriMatcher.addURI(PROVIDER_NAME, "packages/#", PACKAGE_ID);
		uriMatcher.addURI(PROVIDER_NAME, "metadata", METADATA);
		uriMatcher.addURI(PROVIDER_NAME, "metadata/#", METADATA_ID); 
	}
	
	private SQLiteDatabase guvnorDB;
	
	@Override
	public boolean onCreate() {
		Context context = getContext();
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		guvnorDB = dbHelper.getWritableDatabase();
		
		/** Foreign keys are not supported by default, so turn them on */
		if (!guvnorDB.isReadOnly()) {
	        guvnorDB.execSQL("PRAGMA foreign_keys = ON;");
	    }
		return (guvnorDB == null) ? false: true;
	}
	
	@Override
	public String getType(Uri uri) {
		switch(uriMatcher.match(uri)) {
			case PACKAGES: 
				return "vnd.android.cursor.dir/vnd.dataprovider.packages";
			case PACKAGE_ID:
				return "vnd.android.cursor.item/vnd.dataprovider.packages";
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(PACKAGE_TABLE_NAME);
		
		if(uriMatcher.match(uri) == PACKAGE_ID) {
			sqlBuilder.appendWhere(C_PACKAGE_ID + " = " + uri.getPathSegments().get(1));
		}
		if(sortOrder == null || sortOrder == "") {
				sortOrder = C_PACKAGE_ID;
		}
		
		Cursor cursor = sqlBuilder.query(guvnorDB, 
				projection, 
				selection, 
				selectionArgs, 
				null, 
				null, 
				sortOrder);

		cursor.setNotificationUri(getContext().getContentResolver(), uri);	
		return cursor;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count = 0;
		
		switch(uriMatcher.match(uri)) {
			case PACKAGES:
				count = guvnorDB.update(PACKAGE_TABLE_NAME, values, selection, selectionArgs);
				break;	
			
			case PACKAGE_ID:
				String packageId = uri.getPathSegments().get(1);
				count = guvnorDB.update(PACKAGE_TABLE_NAME, 
						values, 
						C_PACKAGE_ID
						+ " = " 
						+ packageId
						+ (!TextUtils.isEmpty(selection) ? " AND " + "( " + selection + ") " : ""),
						selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Illegal URI " + uri);
				
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch(uriMatcher.match(uri)) {
			case PACKAGES: {
				long rowID = guvnorDB.insert(PACKAGE_TABLE_NAME, "", values);
				if(rowID > 0) {
					Uri resultUri = ContentUris.withAppendedId(CONTENT_URI_PACKAGE, rowID);
					getContext().getContentResolver().notifyChange(resultUri, null);
					return resultUri;
				}
			} break;
			case METADATA: {
				long rowID = guvnorDB.insert(METADATA_TABLE_NAME, "", values);
				if(rowID > 0) {
					Uri resultUri = ContentUris.withAppendedId(CONTENT_URI_METADATA, rowID);
					getContext().getContentResolver().notifyChange(resultUri, null);
					return resultUri;
				}
			} break;
			default:
				throw new IllegalArgumentException("Illegal URI: " + uri);		
		}
			
		throw new SQLException("Failed to insert row into " + uri);
	}
	
	@Override 
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		switch(uriMatcher.match(uri)) {
			case PACKAGES:
				count = guvnorDB.delete(PACKAGE_TABLE_NAME, selection, selectionArgs);
			break;
			case PACKAGE_ID:
				String subjectId = uri.getPathSegments().get(1);
				count = guvnorDB.delete(PACKAGE_TABLE_NAME, _ID 
				+ " = " 
				+ subjectId 
				+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""),
				selectionArgs);
				break;
			default:
				throw new IllegalArgumentException( "Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	//@Override
	/*
	public int bulkInsert(Uri uri, ContentValues[] values) {
		
	}
	*/

	/*
	public long insertMetadata(Package.PackageMetaData metaData) {
		ContentValues values = new ContentValues();
		values.put("uuid", metaData.uuid);
		values.put("created", metaData.created);
		values.put("lastModified", metaData.lastModified);
		values.put("lastContributor", metaData.lastContributor);
		values.put("state", metaData.state);
		return guvnorDB.insert(METADATA_TABLE_NAME, null, values);
	}
	*/
	
	static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase sqldb) {
			
			/** Create package table */
			sqldb.execSQL("CREATE TABLE " + PACKAGE_TABLE_NAME + "( " 
			+ C_PACKAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "			
			+ C_PACKAGE_TITLE + " TEXT, "
			+ C_PACKAGE_DESCRIPTION + " TEXT, "
			+ C_PACKAGE_CHECKINCOMMENT + " TEXT, "
			+ C_PACKAGE_VERSION + " INTEGER "
			+ ");"
			);
			
			/** Create metadata table */
			sqldb.execSQL("CREATE TABLE " + METADATA_TABLE_NAME + "( " 
			+ C_METADATA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ C_METADATA_UUID + " TEXT, " 
			+ C_METADATA_CREATED + " TEXT, " 
			+ C_METADATA_LASTMODIFIED + " TEXT, "
			+ C_METADATA_LASTCONTRIB + " TEXT, "
			+ C_METADATA_STATE + " TEXT, " 
			+ C_METADATA_FK + " INTEGER, "
			+ "FOREIGN KEY " 
			+ "(" + C_METADATA_FK + ")"
			+ " REFERENCES " + PACKAGE_TABLE_NAME + "(" + C_PACKAGE_ID + ")"
			+ " ON DELETE CASCADE "
			+ ");"
			);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase sqldb, int oldVersion, int newVersion) {
			Log.w(_TAG, "Upgrading database from version " + oldVersion + " to version " + newVersion);
			sqldb.execSQL("DROP TABLE IF EXISTS " + PACKAGE_TABLE_NAME);
			sqldb.execSQL("DROP TABLE IF EXISTS " + METADATA_TABLE_NAME);
			onCreate(sqldb);
		}
	}
}
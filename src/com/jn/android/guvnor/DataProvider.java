package com.android.jn.restapp;

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
	private static final String DATABASE_NAME = "subjects.db";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_NAME = "subjects";
	
	/** Content provider constants */
	public static final String TAG = "ContentProvider";
	public static final String PROVIDER_NAME = "com.android.jn.restapp.dataprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "subjects");
	public static final String _ID = "_id";
	public static final int SUBJECTS = 1;
	public static final int SUBJECT_ID = 2;
	public static final UriMatcher uriMatcher;
	
	/** Define some columns (ID column is not defined explicitly) */
	public static final String COLUMN1 = "column1";
	public static final String COLUMN2 = "column2";
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
		uriMatcher.addURI(PROVIDER_NAME, "subjects", SUBJECTS);
		uriMatcher.addURI(PROVIDER_NAME, "subjects/#", SUBJECT_ID);
	}
	
	private SQLiteDatabase subjectDB;
	
	@Override
	public boolean onCreate() {
		Context context = getContext();
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		//subjectDB
		subjectDB = dbHelper.getWritableDatabase();
		return (subjectDB == null) ? false: true;
	}
	
	@Override
	public String getType(Uri uri) {
		switch(uriMatcher.match(uri)) {
			case SUBJECTS: 
				return "vnd.android.cursor.dir/vnd.dataprovider.subjects";
			case SUBJECT_ID:
				return "vnd.android.cursor.item/vnd.dataprovider.subjects";
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(TABLE_NAME);
		
		if(uriMatcher.match(uri) == SUBJECT_ID) {
			sqlBuilder.appendWhere(_ID + " = " + uri.getPathSegments().get(1));
		}
		if(sortOrder == null || sortOrder == "") {
				sortOrder = COLUMN1;
		}
		
		Cursor cursor = sqlBuilder.query(subjectDB, 
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
			case SUBJECTS:
				count = subjectDB.update(TABLE_NAME, values, selection, selectionArgs);
				break;	
			
			case SUBJECT_ID:
				String subjectId = uri.getPathSegments().get(1);
				count = subjectDB.update(TABLE_NAME, 
						values, 
						_ID 
						+ " = " 
						+ subjectId
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
		long rowID = subjectDB.insert(TABLE_NAME, "", values);
		if(rowID > 0) {
			Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(resultUri, null);
			return resultUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}
	
	@Override 
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		switch(uriMatcher.match(uri)) {
			case SUBJECTS:
				count = subjectDB.delete(TABLE_NAME, selection, selectionArgs);
			break;
			case SUBJECT_ID:
				String subjectId = uri.getPathSegments().get(1);
				count = subjectDB.delete(TABLE_NAME, _ID 
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
	
	static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase sqldb) {
			sqldb.execSQL("CREATE TABLE " + TABLE_NAME + "( " 
			+ _ID + " INTEGER PRIMARY KEY,"			
			+ COLUMN1 + " TEXT,"
			+ COLUMN2 + " TEXT," 
			+ STATUS_COLUMN + " INTEGER," 
			+ RESULT_COLUMN + " INTEGER" + ");"
			);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase sqldb, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to version " + newVersion);
			sqldb.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(sqldb);
		}
	}
}
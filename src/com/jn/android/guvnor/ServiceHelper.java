package com.jn.android.guvnor;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/** 
 * The BinderCallback is an interface which is passed in an Intent for Service and it's
 * method gets called with some resultcode when Service has completed it's task
 */

interface ResultHandler {
	public void handleResult(int resultCode);
}
interface BinderCallback {
	public void doSomething(int resultCode);
}

public class ServiceHelper {
	
	private static RestService restService;
	private static Context context;
	private static ResultHandler resultHandler;
	
	static class ServiceCallback implements BinderCallback, Parcelable {
		
		public ServiceCallback() {
		}
		
		private ServiceCallback(Parcel in) {
		}
		
		/** From Parcelable */
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
		}
		
		public static final Parcelable.Creator<BinderCallback> CREATOR = new Parcelable.Creator<BinderCallback>() {
			public BinderCallback createFromParcel(Parcel in) {
				return new ServiceCallback(in);
			}
			
			public BinderCallback[] newArray(int size) {
				return new ServiceCallback[size];
			}
		};
		
		/** From BinderCallback */
		@Override
		public void doSomething(int resultCode) {	
			/** Pass the results to some handler */
			resultHandler.handleResult(resultCode);
		}	
	}
	
	private static class SingletonHolder {
		public static final ServiceHelper instance = new ServiceHelper();
	}
	
	public ServiceHelper() {
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	public void registerResultHandler(ResultHandler resultHandler) {
		this.resultHandler = resultHandler;
	}
	
	public static ServiceHelper getInstance() {
		return SingletonHolder.instance;
	}
	
	/** this method will start the service */
	public void doService() {
		if(restService == null) {
			restService = new RestService();
			restService.onCreate();
		}
		Intent intent = new Intent(context, RestService.class);
		intent.putExtra(RestService.CALLBACK_ID, new ServiceCallback());
		context.startService(intent);
	}
}

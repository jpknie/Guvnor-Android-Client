package com.jn.android.guvnor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Thread;
import java.net.URI;
import java.net.URISyntaxException;

import android.net.Uri;
import android.util.Log;


public class Processor extends Thread {
	private static final String _TAG = "Processor";
	
	private String url;
	
	public Processor(String url) {
		super("Processor");
		this.url = url;
	}
	
	@Override
	public void run() {
	}
	
}

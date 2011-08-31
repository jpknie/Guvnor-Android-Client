package com.jn.android.guvnor;

import java.util.ArrayList;
import java.util.HashMap;

class ParserException extends Throwable {
	
}

interface ParserHandler<T> {
	//public ArrayList<> parse(String content) throws ParserException;
	public T parse(String content) throws ParserException;
}

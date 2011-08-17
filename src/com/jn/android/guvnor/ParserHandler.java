package com.jn.android.guvnor;

class ParserException extends Throwable {
	
}

interface ParserHandler {
	public void parse(String content) throws ParserException;
}

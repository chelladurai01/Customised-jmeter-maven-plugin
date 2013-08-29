package com.lazerycode.jmeter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Encoder {
	
	public static void main(String[] args) {
		String thePath = "E:/Program Files/IBM/SDP/runtimes/base";
		try {
			thePath = URLEncoder.encode(thePath, "UTF-8");
			System.out.println("$$$"+thePath);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}

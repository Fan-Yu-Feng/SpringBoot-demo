package com.yohong.base.reflect;

public class ErrorTest {
	
	public static void main(String[] args) {
		errorTest();
		
	}
	
	private static void errorTest() {
		String x = "xxx";
		int i = Integer.parseInt(x);
		System.out.println("i = " + i);
	}
}

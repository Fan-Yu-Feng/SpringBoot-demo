package com.yohong.base.reflect;

import java.util.Scanner;

class Main {
public static void main(String[]args) {
	Scanner sc = new Scanner(System.in);
	System.out.println("输入F的值为:%d");

    int F=sc.nextInt();
	int C =5*(F-32)/9;
	System.out.println("Celsius="+C);
	
}}


package com.yohong.base.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class Reflect {
	
	
	public static void main(String[] args) throws Exception {
		reflectNewInstance();
		
		getConstruct();
	
	}
	
	
	/**
	 * 反射创建对象
	 */
	public static void reflectNewInstance(){
		try {
			Class<Student> studentClass = (Class<Student>) Class.forName("com.yohong.base.reflect.Student");
			
			// method 1
			Student student = studentClass.newInstance();
			
			// method 2
			Student student1 = studentClass.getConstructor().newInstance();
			System.out.println("student1 = " + student1);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
		         InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
		
	}
	
	/**
	 * 反射获取类的构造器
	 */
	public static void getConstruct() throws Exception {
		Class<Student> studentClass = (Class<Student>) Class.forName("com.yohong.base.reflect.Student");
		
		// 所有公共的构造方法
		Constructor<?>[] constructors = studentClass.getConstructors();
		// 打印获取到的构造器
		Arrays.stream(constructors).forEach(System.out::println);
		
		// 所有构造方法
		Constructor<?>[] declaredConstructors = studentClass.getDeclaredConstructors();
		Arrays.stream(declaredConstructors).forEach(System.out::println);
		
	}
	
	
	
}

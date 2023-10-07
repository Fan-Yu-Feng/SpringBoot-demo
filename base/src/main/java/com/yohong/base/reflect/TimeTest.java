package com.yohong.base.reflect;


/**
 * 为什么发射这么慢 <a href="https://stackoverflow.com/questions/1392351/java-reflection-why-is-it-so-slow">...</a>
 *
 * Normal instaniation took: 7ms
 * Reflecting instantiation took:50ms
 *
 */
public class TimeTest {
		
		public static long timeDiff(long old) {
			return System.currentTimeMillis() - old;
		}
		
		public static void main(String args[]) throws Exception {
			
			long numTrials = (long) Math.pow(10, 7);
			
			long millis;
			
			millis = System.currentTimeMillis();
			
			for (int i = 0; i < numTrials; i++) {
				new Student();
			}
			System.out.println("Normal instaniation took: "
					                   + timeDiff(millis) + "ms");
			
			millis = System.currentTimeMillis();
			
			Class<Student> c =Student.class;
			
			for (int i = 0; i < numTrials; i++) {
				c.newInstance();
			}
			
			System.out.println("Reflecting instantiation took:"
					                   + timeDiff(millis) + "ms");
		}
		
		
}

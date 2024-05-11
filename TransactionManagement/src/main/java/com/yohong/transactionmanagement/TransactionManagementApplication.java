package com.yohong.transactionmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author fanyufeng
 */
@SpringBootApplication
public class TransactionManagementApplication {
	
	ThreadLocal<String> map = new ThreadLocal<>();
	public static void main(String[] args) {
		SpringApplication.run(TransactionManagementApplication.class, args);
	}

}

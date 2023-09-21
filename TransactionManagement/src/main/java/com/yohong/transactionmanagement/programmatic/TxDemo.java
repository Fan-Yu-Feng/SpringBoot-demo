package com.yohong.transactionmanagement.programmatic;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class TxDemo {
	
	private final TransactionTemplate transactionTemplate;
	
	public TxDemo(PlatformTransactionManager transactionManager) {
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		
		// the transaction settings can be set here explicitly if so desired
		this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
		this.transactionTemplate.setTimeout(30); // 30 seconds
		// and so forth...
	}
	
	public void saveUser(){
		//设置事务的各种属性;可以猜测TransactionTemplate应该是实现了TransactionDefinition
		transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		transactionTemplate.setTimeout(30000);
		
		//执行事务 将业务逻辑封装在TransactionCallback中
		transactionTemplate.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(TransactionStatus transactionStatus) {
				//....   业务代码
				System.out.println(" insert user info  ");
				int x = 1 / 0;
				return null;
			}
		});
		
		
		
	}
	
}

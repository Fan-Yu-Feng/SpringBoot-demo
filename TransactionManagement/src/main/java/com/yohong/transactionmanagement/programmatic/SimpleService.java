package com.yohong.transactionmanagement.programmatic;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class SimpleService {
	
	// single TransactionTemplate shared amongst all methods in this instance
	private final TransactionTemplate transactionTemplate;
	
	// use constructor-injection to supply the PlatformTransactionManager
	
	public SimpleService(PlatformTransactionManager transactionManager) {
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		
		// the transaction settings can be set here explicitly if so desired
		this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
		this.transactionTemplate.setTimeout(30); // 30 seconds
		// and so forth...
	}
	public Object someServiceMethod() {
		return transactionTemplate.execute(new TransactionCallback() {
			// the code in this method runs in a transactional context
			public Object doInTransaction(TransactionStatus status) {
				System.out.println(" insert user info  ");
				int x = 1 / 0;
				return null;
			}
		});
	}
	
	
	public Object doSomeThing(){
		
		// If there is no return value, you can use the convenient TransactionCallbackWithoutResult class with an anonymous class, as follows:
		
		
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				// updateOperation1();
				// updateOperation2();
			}
		});
		
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			// Code within the callback can roll the transaction back by calling the setRollbackOnly() method on the supplied TransactionStatus object, as follows:
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					// updateOperation1();
					// updateOperation2();
				} catch (Exception ex) {
					status.setRollbackOnly();
				}
			}
		});
		
		
		
		return null;
	}
	
}
/*
 * University of Warsaw
 * Concurrent Programming Course 2020/2021
 * Java Assignment
 * 
 * Author: Konrad Iwanicki (iwanicki@mimuw.edu.pl)
 */
package cp1.base;

/**
 * The transaction manager interface your
 * solution has to implement.
 * 
 * @author Konrad Iwanicki (iwanicki@mimuw.edu.pl)
 */
public interface TransactionManager {
	
	public void startTransaction(
	) throws
		AnotherTransactionActiveException;
	
	public void operateOnResourceInCurrentTransaction(
			ResourceId rid,
			ResourceOperation operation
	) throws
		NoActiveTransactionException,
		UnknownResourceIdException,
		ActiveTransactionAborted,
		ResourceOperationException,
		InterruptedException;
	
	public void commitCurrentTransaction(
	) throws
		NoActiveTransactionException,
		ActiveTransactionAborted;
	
	public void rollbackCurrentTransaction();

	public boolean isTransactionActive();

	public boolean isTransactionAborted();

}

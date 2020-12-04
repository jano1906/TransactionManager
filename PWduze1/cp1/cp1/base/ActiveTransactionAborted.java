/*
 * University of Warsaw
 * Concurrent Programming Course 2020/2021
 * Java Assignment
 * 
 * Author: Konrad Iwanicki (iwanicki@mimuw.edu.pl)
 */
package cp1.base;

public final class ActiveTransactionAborted extends Exception {

	private static final long serialVersionUID = 8594417458242113185L;

	@Override
	public String getMessage() {
		return "The active transaction has been aborted";
	}
		
}

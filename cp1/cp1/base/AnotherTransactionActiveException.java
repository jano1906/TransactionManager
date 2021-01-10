/*
 * University of Warsaw
 * Concurrent Programming Course 2020/2021
 * Java Assignment
 * 
 * Author: Konrad Iwanicki (iwanicki@mimuw.edu.pl)
 */
package cp1.base;

public final class AnotherTransactionActiveException extends Exception {

	private static final long serialVersionUID = 3173108237647474233L;

	@Override
	public String getMessage() {
		return "A previous transaction is still active";
	}
	
}

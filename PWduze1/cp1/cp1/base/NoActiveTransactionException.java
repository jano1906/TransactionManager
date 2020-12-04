/*
 * University of Warsaw
 * Concurrent Programming Course 2020/2021
 * Java Assignment
 * 
 * Author: Konrad Iwanicki (iwanicki@mimuw.edu.pl)
 */
package cp1.base;

public final class NoActiveTransactionException extends Exception {

	private static final long serialVersionUID = 2697158598896966444L;

	@Override
	public String getMessage() {
		return "No active transaction";
	}
	
}

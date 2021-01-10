/*
 * University of Warsaw
 * Concurrent Programming Course 2020/2021
 * Java Assignment
 * 
 * Author: Konrad Iwanicki (iwanicki@mimuw.edu.pl)
 */
package cp1.base;

public final class UnknownResourceIdException extends Exception {

	private static final long serialVersionUID = -4892523993266526100L;

	private final ResourceId rid;
	
	public UnknownResourceIdException(ResourceId rid) {
		this.rid = rid;
	}

	public ResourceId getResourceId() {
		return this.rid;
	}
	
	@Override
	public String getMessage() {
		return "Unknown resource " + this.rid;
	}
	
}

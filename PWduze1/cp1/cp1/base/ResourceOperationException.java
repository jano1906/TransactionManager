/*
 * University of Warsaw
 * Concurrent Programming Course 2020/2021
 * Java Assignment
 * 
 * Author: Konrad Iwanicki (iwanicki@mimuw.edu.pl)
 */
package cp1.base;

public class ResourceOperationException extends Exception {

	private static final long serialVersionUID = 3548336797383876251L;

	private final ResourceId rid;
	private final String opName;
	
	public ResourceOperationException(
			ResourceId rid,
			ResourceOperation op
	) {
		this.rid = rid;
		this.opName = op.toString();
	}
	
	public ResourceId getResourceId() {
		return this.rid;
	}

	public String getOperationName() {
		return this.opName;
	}

	@Override
	public String getMessage() {
		return "Operation \"" + this.opName +
				"\" on resource " + this.rid +
				" has failed";
	}
	
}

/*
 * University of Warsaw
 * Concurrent Programming Course 2020/2021
 * Java Assignment
 * 
 * Author: Konrad Iwanicki (iwanicki@mimuw.edu.pl)
 */
package cp1.base;

public class Resource {

	private final ResourceId id;
	
	public Resource(ResourceId id) {
		this.id = id;
	}
	
	public final ResourceId getId() {
		return this.id;
	}
	
	public final void apply(ResourceOperation o) throws ResourceOperationException {
		o.execute(this);
	}
	
	public final void unapply(ResourceOperation o) {
		o.undo(this);
	}

	@Override
	protected final Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

}

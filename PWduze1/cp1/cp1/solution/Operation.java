package cp1.solution;

import cp1.base.Resource;
import cp1.base.ResourceOperation;
import cp1.base.ResourceOperationException;

public class Operation {
    private final Resource resource;
    private final ResourceOperation op;

    public Operation(Resource resource, ResourceOperation op){
        this.resource = resource;
        this.op = op;
    }

    public void execute() throws ResourceOperationException {
        synchronized (resource) {
            op.execute(resource);
        }
    }
    public void undo(){
        synchronized (resource) {
            op.undo(resource);
        }
    }
}

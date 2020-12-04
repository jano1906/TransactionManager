package cp1.solution;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import cp1.base.ResourceOperation;
import cp1.base.ResourceOperationException;

public class Transaction {
    private final ArrayList<Operation> operations;
    private final AtomicBoolean isAborted;
    private final CountDownLatch rollBackLatch;
    private final long time;


    public void add(Operation op) throws ResourceOperationException {
        op.execute();
        operations.add(op);
    }
    public Transaction(long time){
        this.operations = new ArrayList<>();
        this.time = time;
        this.isAborted = new AtomicBoolean();
        this.rollBackLatch = new CountDownLatch(1);
    }
    public CountDownLatch abort(){
        isAborted.set(true);
        return rollBackLatch;
    }
    public boolean isAborted(){
        return isAborted.get();
    }
    public void rollBack(){
        for(int i=operations.size()-1;i>=0;i--)
            operations.get(i).undo();
        operations.clear();
        rollBackLatch.countDown();
    }
}

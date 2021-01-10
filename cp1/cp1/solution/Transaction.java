package cp1.solution;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import cp1.base.ResourceOperationException;

public class Transaction implements Comparable<Transaction>{
    private final ArrayList<Operation> operations;
    private final AtomicBoolean isAborted;
    private final Thread myThread;
    private final long time;
    private final long id;

    public void add(Operation op) throws ResourceOperationException {
        op.execute();
        operations.add(op);
    }
    public Transaction(long time){
        this.operations = new ArrayList<>();
        this.time = time;
        this.isAborted = new AtomicBoolean();
        this.id = Thread.currentThread().getId();
        this.myThread = Thread.currentThread();
    }
    public void abort(){
        myThread.interrupt();
        isAborted.set(true);
    }
    public boolean isAborted(){
        return isAborted.get();
    }
    public void rollBack(){
        for(int i=operations.size()-1;i>=0;i--)
            operations.get(i).undo();
        operations.clear();
    }

    //t1 > t2 iff it's less likely to be aborted
    @Override
    public int compareTo(Transaction other) {
        if(this.time < other.time)
            return 1;
        if(this.time > other.time)
            return -1;
        if(this.id < other.id)
            return 1;
        return -1;
    }
}

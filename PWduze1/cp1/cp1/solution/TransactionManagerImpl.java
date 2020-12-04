package cp1.solution;

import cp1.base.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class TransactionManagerImpl implements TransactionManager {
    private final ConcurrentHashMap<Thread, Transaction> transactions;
    private final ConcurrentHashMap<ResourceId, Resource> resources;
    private final LocalTimeProvider localTimeProvider;


    public TransactionManagerImpl(Collection<Resource> resources,
                                  LocalTimeProvider ltp){
        this.owners = new ConcurrentHashMap<>();
        this.transactions = new ConcurrentHashMap<>();
        this.resources = new ConcurrentHashMap<>();
        for(Resource r : resources)
            this.resources.put(r.getId(), r);
        this.localTimeProvider = ltp;
    }

    @Override
    public void startTransaction() throws AnotherTransactionActiveException {
        Thread t = Thread.currentThread();
        if(transactions.containsKey(t))
            throw new AnotherTransactionActiveException();
        transactions.put(t, new Transaction(localTimeProvider.getTime()));
    }
// --------------------------------------------------
    private final ConcurrentHashMap<ResourceId, Thread> owners;
    private Semaphore mutex;

    @Override
    public void operateOnResourceInCurrentTransaction(ResourceId rid, ResourceOperation operation) throws NoActiveTransactionException, UnknownResourceIdException, ActiveTransactionAborted, ResourceOperationException, InterruptedException {
        if(!isTransactionActive())
            throw new NoActiveTransactionException();
        if(!resources.containsKey(rid))
            throw new UnknownResourceIdException(rid);
        if(isTransactionAborted())
            throw new ActiveTransactionAborted();

        //TODO ------------------------------------------------------
        Thread t = Thread.currentThread();
        Operation op = new Operation(resources.get(rid), operation);

        mutex.acquire();
        if(!owners.containsKey(rid) || owners.get(rid) == t) {
            owners.putIfAbsent(rid, t);
            transactions.get(t).add(op);
            mutex.release();
        }
        
    }

    @Override
    public void commitCurrentTransaction() throws NoActiveTransactionException, ActiveTransactionAborted {
        Thread t = Thread.currentThread();
        if(!isTransactionActive())
            throw new NoActiveTransactionException();
        if(isTransactionAborted())
            throw new ActiveTransactionAborted();
        transactions.remove(t);
    }

    @Override
    public void rollbackCurrentTransaction() {
        Thread t = Thread.currentThread();
        if(!transactions.containsKey(t))
            return;
        transactions.get(t).rollBack();
        transactions.remove(t);
    }

    @Override
    public boolean isTransactionActive() {
        Thread t = Thread.currentThread();
        return transactions.containsKey(t);
    }

    @Override
    public boolean isTransactionAborted() {
        Thread t = Thread.currentThread();
        return transactions.get(t).isAborted();
    }
}
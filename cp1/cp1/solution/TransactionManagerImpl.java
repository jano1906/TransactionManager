package cp1.solution;

import cp1.base.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class TransactionManagerImpl implements TransactionManager {

    private final ConcurrentHashMap<Long, Transaction> transactions;
    private final ConcurrentHashMap<ResourceId, Resource> resources;
    private final LocalTimeProvider localTimeProvider;

    private final ConcurrentHashMap<ResourceId, Transaction> owners;
    private final ConcurrentHashMap<Transaction, ResourceId> wishes;
    private final ConcurrentHashMap<ResourceId, Queue<Transaction>> resourceQueues;
    private final ConcurrentHashMap<Transaction, Semaphore> lobbys;
    private final Semaphore mutex;

    private void clearData(Long t) {
        Transaction currTransaction = transactions.get(t);
        boolean toInterrupt = Thread.interrupted();

        for(ResourceId rid : owners.keySet()){
            if(owners.get(rid) == null)
                continue;
            if(owners.get(rid).equals(currTransaction)) {
                owners.remove(rid);
                try{
                    mutex.acquireUninterruptibly(); //przekazanie sekcji krytycznej
                    Transaction transactionToAwake;
                    do {
                        transactionToAwake = resourceQueues.get(rid).poll();
                    } while(transactionToAwake!= null && transactionToAwake.isAborted());
                    if(transactionToAwake == null) {
                        mutex.release();
                        continue;
                    }
                    Semaphore lobby = lobbys.get(transactionToAwake);
                    if(lobby == null)
                        continue;
                    lobby.release();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        wishes.remove(currTransaction);
        lobbys.remove(currTransaction);
        transactions.remove(t);
        if(toInterrupt)
            Thread.currentThread().interrupt();
    }

    private void solveDeadLockIfOccurred(Set<Transaction> visited, Transaction curr){
        visited.add(curr);
        if(!wishes.containsKey(curr))
            return;
        ResourceId wish = wishes.get(curr);
        Transaction owner = owners.get(wish);
        if(owner.isAborted())
            return;
        if(visited.contains(owner)){
            Transaction toAbort = Collections.min(visited);
            toAbort.abort();
            Semaphore lobby = lobbys.get(toAbort);
            if(lobby!=null)
                lobby.release();
            return;
        }
        solveDeadLockIfOccurred(visited, owner);
    }

    public TransactionManagerImpl(Collection<Resource> resources,
                                  LocalTimeProvider ltp){
        this.transactions = new ConcurrentHashMap<>();
        this.resources = new ConcurrentHashMap<>();
        this.wishes = new ConcurrentHashMap<>();
        this.owners = new ConcurrentHashMap<>();
        this.resourceQueues = new ConcurrentHashMap<>();
        this.lobbys = new ConcurrentHashMap<>();
        this.mutex = new Semaphore(1, true);
        this.localTimeProvider = ltp;

        for(Resource r : resources) {
            this.resources.put(r.getId(), r);
            this.resourceQueues.put(r.getId(), new ArrayDeque<>());
        }
    }

    @Override
    public void startTransaction() throws AnotherTransactionActiveException {
        long t = Thread.currentThread().getId();
        if(transactions.containsKey(t))
            throw new AnotherTransactionActiveException();
        Transaction transaction = new Transaction(localTimeProvider.getTime());
        transactions.put(t, transaction);
        lobbys.put(transaction, new Semaphore(0, true));
    }

    @Override
    public void operateOnResourceInCurrentTransaction(ResourceId rid, ResourceOperation operation) throws NoActiveTransactionException, UnknownResourceIdException, ActiveTransactionAborted, ResourceOperationException, InterruptedException {
        if(!isTransactionActive())
            throw new NoActiveTransactionException();
        if(!resources.containsKey(rid))
            throw new UnknownResourceIdException(rid);
        if(isTransactionAborted())
            throw new ActiveTransactionAborted();


        long t = Thread.currentThread().getId();
        Transaction currTransaction = transactions.get(t);
        Operation op = new Operation(resources.get(rid), operation);

        mutex.acquireUninterruptibly();
        if(!owners.containsKey(rid) || owners.get(rid).equals(currTransaction)) {
            owners.putIfAbsent(rid, currTransaction);
            currTransaction.add(op);
            mutex.release();
            return;
        }

        wishes.put(currTransaction, rid);
        resourceQueues.get(rid).add(currTransaction);
        solveDeadLockIfOccurred(new HashSet<>(), currTransaction);
        mutex.release();
        Semaphore lobby = lobbys.get(currTransaction);
        if(lobby == null)
            return;
        lobby.acquireUninterruptibly();
        if(currTransaction.isAborted()) {
            return;
        }
        owners.put(rid, currTransaction);
        wishes.remove(currTransaction);
        currTransaction.add(op);
        mutex.release();
    }

    @Override
    public void commitCurrentTransaction() throws NoActiveTransactionException, ActiveTransactionAborted {
        long t = Thread.currentThread().getId();
        if(!isTransactionActive())
            throw new NoActiveTransactionException();
        if(isTransactionAborted())
            throw new ActiveTransactionAborted();
        clearData(t);
    }

    @Override
    public void rollbackCurrentTransaction() {
        long t = Thread.currentThread().getId();
        if(!transactions.containsKey(t))
            return;
        transactions.get(t).rollBack();
        clearData(t);
    }

    @Override
    public boolean isTransactionActive() {
        long t = Thread.currentThread().getId();
        return transactions.containsKey(t);
    }

    @Override
    public boolean isTransactionAborted() {
        long t = Thread.currentThread().getId();
        if(!transactions.containsKey(t))
            return false;
        return transactions.get(t).isAborted();
    }
}
/*
 * University of Warsaw
 * Concurrent Programming Course 2020/2021
 * Java Assignment
 * 
 * Author: Konrad Iwanicki (iwanicki@mimuw.edu.pl)
 */
package cp1.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cp1.base.ResourceId;
import cp1.base.ResourceOperation;
import cp1.base.ResourceOperationException;
import cp1.base.TransactionManager;
import cp1.base.UnknownResourceIdException;
import cp1.solution.TransactionManagerFactory;
import cp1.base.ActiveTransactionAborted;
import cp1.base.NoActiveTransactionException;
import cp1.base.AnotherTransactionActiveException;
import cp1.base.LocalTimeProvider;
import cp1.base.Resource;


/**
 * A "smoke test" for your implementation.
 * 
 * @author Konrad Iwanicki (iwanicki@mimuw.edu.pl)
 */
public class Transactions {
	
	private final static long BASE_WAIT_TIME = 500;
	
	public static void main(String[] args) {
		// Set up resources.
		ResourceImpl r1 = new ResourceImpl(ResourceIdImpl.generate());
		ResourceImpl r2 = new ResourceImpl(ResourceIdImpl.generate());
		ResourceImpl r3 = new ResourceImpl(ResourceIdImpl.generate());
		ResourceImpl r4 = new ResourceImpl(ResourceIdImpl.generate());
		List<Resource> resources =
				Collections.unmodifiableList(
						Arrays.asList(r1, r2, r3, r4)
				);

		// Set up a transaction manager.
		TransactionManager tm =
				TransactionManagerFactory.newTM(
						resources,
						new LocalTimeProviderImpl()
				);

		// Set up threads operation on the resources.
		ArrayList<Thread> threads = new ArrayList<Thread>();
		threads.add(
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(1 * BASE_WAIT_TIME);
						} catch (InterruptedException e) {
						}
						try {
							tm.startTransaction();
						} catch (AnotherTransactionActiveException e) {
							throw new AssertionError(e);
						}
						if (! tm.isTransactionActive()) {
							throw new AssertionError("Failed to start a transaction");
						}
						try {
							tm.operateOnResourceInCurrentTransaction(
									r1.getId(),
									ResourceOpImpl.get()
							);
							Thread.sleep(4 * BASE_WAIT_TIME);
							tm.commitCurrentTransaction();
							if (tm.isTransactionActive()) {
								throw new AssertionError("Failed to commit a transaction");
							}
						} catch (InterruptedException |
								ActiveTransactionAborted |
								NoActiveTransactionException |
								UnknownResourceIdException |
								ResourceOperationException e) {
							throw new AssertionError(e);
						} finally {
							tm.rollbackCurrentTransaction();
						}
					}
				})
		);
		threads.add(
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(1 * BASE_WAIT_TIME);
						} catch (InterruptedException e) {
							throw new AssertionError(e);
						}
						try {
							tm.startTransaction();
						} catch (AnotherTransactionActiveException e) {
							throw new AssertionError(e);
						}
						if (! tm.isTransactionActive()) {
							throw new AssertionError("Failed to start a transaction");
						}
						try {
							tm.operateOnResourceInCurrentTransaction(
									r2.getId(),
									ResourceOpImpl.get()
							);
							Thread.sleep(6 * BASE_WAIT_TIME);
							tm.operateOnResourceInCurrentTransaction(
									r3.getId(),
									ResourceOpImpl.get()
							);
							Thread.sleep(1 * BASE_WAIT_TIME);
						} catch (InterruptedException |
								ActiveTransactionAborted |
								NoActiveTransactionException |
								ResourceOperationException |
								UnknownResourceIdException e) {
							throw new AssertionError(e);
						} finally {
							tm.rollbackCurrentTransaction();
							if (tm.isTransactionActive()) {
								throw new AssertionError("Failed to rollback a transaction");
							}
						}
					}
				})
		);
		threads.add(
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(2 * BASE_WAIT_TIME);
						} catch (InterruptedException e) {
							throw new AssertionError(e);
						}
						try {
							tm.startTransaction();
						} catch (AnotherTransactionActiveException e) {
							throw new AssertionError(e);
						}
						if (! tm.isTransactionActive()) {
							throw new AssertionError("Failed to start a transaction");
						}
						if (tm.isTransactionAborted()) {
							throw new AssertionError("Invalid transaction state");
						}
						try {
							tm.operateOnResourceInCurrentTransaction(
									r2.getId(),
									ResourceOpImpl.get()
							);
							Thread.sleep(1 * BASE_WAIT_TIME);
							tm.operateOnResourceInCurrentTransaction(
									r1.getId(),
									ResourceOpImpl.get()
							);
							tm.commitCurrentTransaction();
							if (tm.isTransactionActive()) {
								throw new AssertionError("Failed to commit a transaction");
							}
						} catch (InterruptedException |
								ActiveTransactionAborted |
								NoActiveTransactionException |
								ResourceOperationException |
								UnknownResourceIdException e) {
							throw new AssertionError(e);
						} finally {
							tm.rollbackCurrentTransaction();
						}
					}
				})
		);
		// Start the threads and wait for them to finish.
		for (Thread t : threads) {
			t.start();
		}
		try {
			for (Thread t : threads) {
				t.join(10 * BASE_WAIT_TIME);
			}			
		} catch (InterruptedException e) {
			throw new AssertionError("The main thread has been interrupted");
		}
		// Check the results.
		expectResourceValue(r1, 2);
		expectResourceValue(r2, 1);
		expectResourceValue(r3, 0);
	}
	
	private final static void expectResourceValue(ResourceImpl r, long val) {
		if (r.getValue() != val) {
			throw new AssertionError(
					"For resource " + r.getId() +
					", expected value " + val +
					", but got value " + r.getValue()
			);
		}
	}
	
	// ---------------------------------------------------------
	// -                                                       -
	// -     Sample implementations of the base interfaces     -
	// -                                                       -
	// ---------------------------------------------------------
	
	private static final class LocalTimeProviderImpl implements LocalTimeProvider {
		@Override
		public long getTime() {
			return System.currentTimeMillis();
		}
	}
	
	private static final class ResourceIdImpl implements ResourceId {
		private static volatile int next = 0;
		
		public static synchronized ResourceId generate() {
			return new ResourceIdImpl(next++);
		}
		
		private final int value;
		
		private ResourceIdImpl(int value) {
			this.value = value;
		}
		@Override
		public int compareTo(ResourceId other) {
			if (! (other instanceof ResourceIdImpl)) {
				throw new RuntimeException("Comparing incompatible resource IDs");
			}
			ResourceIdImpl second = (ResourceIdImpl)other;
			return Integer.compare(this.value, second.value);
		}
		@Override
		public boolean equals(Object obj) {
			if (! (obj instanceof ResourceIdImpl)) {
				return false;
			}
			ResourceIdImpl second = (ResourceIdImpl)obj;
			return this.value == second.value;
		}
		@Override
		public int hashCode() {
			return Integer.hashCode(this.value);
		}
		@Override
		public String toString() {
			return "R" + this.value;
		}
	}
	
	private static final class ResourceImpl extends Resource {
		private volatile long value = 0;
		public ResourceImpl(ResourceId id) {
			super(id);
		}
		public void incValue() {
			long x = this.value;
			++x;
			this.value = x;
		}
		public void decValue() {
			long x = this.value;
			--x;
			this.value = x;
		}
		public long getValue() {
			return this.value;
		}
	}
	
	private static final class ResourceOpImpl extends ResourceOperation {
		private final static ResourceOpImpl singleton = new ResourceOpImpl();
		public static ResourceOperation get() {
			return singleton;
		}
		private ResourceOpImpl() {
		}
		@Override
		public String toString() {
			return "OP_" + super.toString();
		}
		@Override
		public void execute(Resource r) {
			if (! (r instanceof ResourceImpl)) {
				throw new AssertionError("Unexpected resource type " +
						r.getClass().getCanonicalName());
			}
			((ResourceImpl)r).incValue();
		}
		@Override
		public void undo(Resource r) {
			if (! (r instanceof ResourceImpl)) {
				throw new AssertionError("Unexpected resource type " +
						r.getClass().getCanonicalName());
			}
			((ResourceImpl)r).decValue();
		}
	}
}

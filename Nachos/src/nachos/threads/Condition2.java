package nachos.threads;

//Linkedlist for a queue
import java.util.LinkedList;

import nachos.machine.Lib;
import nachos.machine.Machine;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @se8
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
		
	    boolean bStatus = Machine.interrupt().disable();
		
	//    queueWaitList.add(KThread.currentThread());
	    conditionQueue.waitForAccess(KThread.currentThread());
		conditionLock.release();
	    
	    KThread.sleep(); //makes Thread go to sleep atomically
	    
		conditionLock.acquire();
	    Machine.interrupt().restore(bStatus);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		
	//	if (!queueWaitList.isEmpty())
	//    {
	    boolean bStatus = Machine.interrupt().disable();     
	//        KThread thread = queueWaitList.removeFirst();
	    KThread thread = conditionQueue.nextThread();
	    if (thread != null)
	    {
	    	conditionLock.release();
	        thread.ready();
	        conditionLock.acquire();
	    }
	    Machine.interrupt().restore(bStatus);
	//    }
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		
		/*
		 * while (!queueWaitList.isEmpty()) { wake(); }
		 */
		
		//Implementation for ThreadQueue
		boolean bStatus = Machine.interrupt().disable();
		for (KThread kT = conditionQueue.nextThread(); kT != null; kT = conditionQueue.nextThread()) {
			conditionLock.release();
			kT.ready();
			conditionLock.acquire();
		}
		Machine.interrupt().restore(bStatus);
		
    }

    private Lock conditionLock;
    
    //Creates a new LinkedList
    //private LinkedList <KThread> queueWaitList = new LinkedList<KThread>();
    
    //Create a ThreadQueue
    private ThreadQueue conditionQueue = ThreadedKernel.scheduler.newThreadQueue(false);
}

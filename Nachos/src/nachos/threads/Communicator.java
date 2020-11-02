package nachos.threads;

import nachos.machine.*;

import java.util.*; 

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
	/**
	 * Allocate a new communicator.
	 */
	public Communicator() {
		lock = new Lock();
		listenWait = new Condition2(lock);
		speakWait = new Condition2(lock);
		listenReceive = new Condition2(lock);
		speakSend = new Condition2(lock);
		listenerNext = false;
		speakerNext = false;
		received = false;
	}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 *
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 *
	 * @param	word	the integer to transfer.
	 */

	public void speak(int word) {
		lock.acquire();
		while(speakerNext){ 
			//Add speaker to waiting queue
			speakWait.sleep();
		}

		//Prevent other speakers from speaking
		speakerNext = true;

		holder = word;
		
		while(!listenerNext || !received){
			listenReceive.wake(); //wake up a partner
			speakSend.sleep(); //put speaker to queue
		}

		listenerNext = false; //set it to false so other listener can get to the recievingQueue
		speakerNext = false; //set it to false so other speaker can get to the sendingQueue
		received = false;
		speakWait.wake(); //wake up a waiting speaker
		listenWait.wake();
		lock.release();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return
	 * the <i>word</i> that thread passed to <tt>speak()</tt>.
	 *
	 * @return	the integer transferred.
	 */    
	public int listen() {
		lock.acquire();
		while(listenerNext){
			listenWait.sleep();
		}

		listenerNext = true;
		//the process below is inaccessible to other listeners
		//as long listenerNext is true

		while(!speakerNext){ //no speaker, go into loop
			listenReceive.sleep(); //set this thread to be the first thread to receive a message
		}

		//1 speaker sending message
		speakSend.wake(); // wake up the sleeping speaker
		received = true;
		lock.release();
		return holder;
	}

	private Condition2 	speakWait; //speaker wait queue
	private Condition2 	speakSend; //speaker is sending word
	private Condition2 	listenWait; //listener wait queue
	private Condition2 	listenReceive;	//listener receiving word
	
	private boolean 	listenerNext; //next listener
	private boolean 	speakerNext; //next speaker
	private boolean 	received; //received message
	
	private int 		holder;
	private Lock 		lock;
}
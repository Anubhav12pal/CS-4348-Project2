// CS-4348 Project 2
// Anubhav Pal, axp200092

import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;

// The Project class
public class Project2 {
	
	// fixed variables, showing workers and customers
	private static int maxWorkers = 3;
	private static int maxCustomers = 50;
	private static int internalCapacity = 10;
	
	// All the semaphores used
	private static Semaphore maxCapacity;
	private static Semaphore workers;
	private static Semaphore servingLimit;
	private static Semaphore mutex;
	private static Semaphore waitForCustNumber;
	private static Semaphore waitForWorkerNumber;
	private static Semaphore waitForTaskNumber;
	private static Semaphore waitForWorkToBeFinished[] = new Semaphore[50];
	private static Semaphore Scales;
	private static Semaphore signal;
	
	// Project variables of different classes
	private static int workerNum;
	private static int custNum;
	private static int actionNeeded;
	
	
	// The main method
	public static void main(String args[]) {
		
		
		// Declaring the customer and Worker Threads
		Thread Customer[] = new Thread[maxCustomers];
		Thread Worker[] = new Thread[maxWorkers];
		
		// Initializing the Semaphores used
		maxCapacity = new Semaphore(internalCapacity, true);
		workers = new Semaphore(3);
		servingLimit = new Semaphore(3, true);
		mutex = new Semaphore(1, true);
		waitForCustNumber = new Semaphore(0, true);
		waitForWorkerNumber = new Semaphore(0, true);
		waitForTaskNumber = new Semaphore(0, true);
		signal = new Semaphore(1, true);
		Scales = new Semaphore(1, true);
		
		for(int i = 0; i  <50; i++) {
			waitForWorkToBeFinished[i] = new Semaphore(0, true);
		}
		
		
		// Starting all the worker threads
		for(int i = 0; i  < maxWorkers; i++) {
			Worker[i] = new Thread(new PostalWorker(i));
			Worker[i].start();
		}
		
		// Starting all the Customer threads
		for(int i = 0; i < maxCustomers; i++) {
			Customer[i] = new Thread(new Customer(i));
			Customer[i].start();
			
			
		}
		
		// Once the Customers have finished their work, they join to the min thread
		for(int i = 0; i < maxCustomers; i++) {
			
			try {
				Customer[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Customer " + i + " joined");
		}
		
		// As the Workers might have to run infinitely, after serving the 50 customers the program terminates
		System.exit(0);
		
		
		
	}
	
	// The Customer class, implements runnable to start threads
	public static class Customer implements Runnable{
		
		// The class variables
		private  int customerNumber;
		private  int customerAction;
		private int workerNum;
		
		// Constructor
		public Customer(int customerNumber) {
			
			this.customerNumber = customerNumber;
			Random r = new Random();
			this.customerAction = r.nextInt(3) + 1;
		}
		
		
		// Starts the thread
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			// Prints when the Customer threads i s created
			System.out.println("Customer " + customerNumber + " created");

			
			// Allows only 10 customer to enter the office, rest wait
			try {
				maxCapacity.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("Customer " + this.customerNumber + " entered the post office.");
			
			// There are only 3 workers so there is a serving limit of 3, this limits that
			try {
				servingLimit.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Tells that the customer is ready to be served
			try{
				signal.acquire(); 
				}catch (InterruptedException e){}
			
			// Sets the Project2 variables to the classes variables, this allows the worker to access the variables
			Project2.custNum = this.customerNumber;
			Project2.actionNeeded = this.customerAction;
			
			// The worker was waiting for the customer Number, we release that semaphore as we saved the variables
			// signal(waitForCustNumber
			waitForCustNumber.release();
			
			// This now waits for the worker number, after the worker starts serving the customer
			// wait(waitForWorkerNumber)
			try {
				waitForWorkerNumber.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// This gives the current customer its worker number
			this.workerNum = Project2.workerNum;
			
			// Customer asks a randomly generated task
			switch(customerAction) {
			
			case 1:
				System.out.println("Customer " + this.customerNumber + " asks postal worker " + this.workerNum + " to buy stamps.");
				break;
				
			case 2:
				System.out.println("Customer " + this.customerNumber + " asks postal worker " + this.workerNum + " to mail a letter.");
				break;
			
			case 3:
				System.out.println("Customer " + this.customerNumber + " asks postal worker " + this.workerNum + " to mail a package.");
				break;
			
			}
			
			// Once we have the task number we release this so that the worker can work on it
			// signal(waitForTaskNumber)
			waitForTaskNumber.release();
			
			//The worker finishes the current workers task and signals the customer
			// Till then the customer waits
			try {
				waitForWorkToBeFinished[this.customerNumber].acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// When the worker finishes the, the next customer can come 
			servingLimit.release();
			
			// Printing the customer finished his task
			switch(customerAction) {
			
			case 1:
				System.out.println("Customer " + this.customerNumber + " finished buying stamps.");
				break;
				
			case 2:
				System.out.println("Customer " + this.customerNumber + " finished mailing a letter.");
				break;
			
			case 3:
				System.out.println("Customer " + this.customerNumber + " finished mailing a package.");
				break;
				
			}
			
			
			// Customer leaves the office
	        System.out.println("Customer " + this.customerNumber + " leaves the post office.");
	
	        // Additional space becomes available inside the Post Office
			maxCapacity.release();
			
			
			
			
		
	}
}
		
		
	// The Postal Worker Class
	public static class PostalWorker implements Runnable{
		
		// Class Variables
		private  int workerNumber;
		private int custAction;
		private int custNum;
		
		// Constructor
		public PostalWorker(int workerNumber) {
			
			this.workerNumber = workerNumber;
			
		}

		// Starts the Worker thread
		@Override
		public void run() {
		// TODO Auto-generated method stub
			
			// Prints the worker threads has started
			System.out.println("Postal worker  " + workerNumber + " created");

			// The worker works and serves all the customers
			while(true) {
				
			// There are only three workers
			try {
				workers.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Waits for the customer to send the Custoemr number
			try {
				waitForCustNumber.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Helps in maintaining mutual exclusion
			try {
				mutex.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			this.custNum = Project2.custNum;
			this.custAction = Project2.actionNeeded;
			
			// Prints the worker serving the customer
			System.out.println("Postal worker " + this.workerNumber + " serving customer " + this.custNum);
			Project2.workerNum = this.workerNumber;
			
			// The worker knows that the customer is ready
			signal.release();

			// Tells the Customer that the worker number is ready
			waitForWorkerNumber.release();
			
			// Gets the task number after the customer thread did it's work
			try {
				waitForTaskNumber.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Releases mutex
			mutex.release();
			
			// Performs task as needed
			switch(this.custAction) {
			
			case 1:
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				break;
				
			case 2:
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			case 3:
				
				// scales can be only used once at a time
				try {
					Scales.acquire();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				System.out.println("Scales in use by postal worker " + this.workerNumber);
				
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("Scales released by postal worker " + this.workerNumber);
				// Scales released
				Scales.release();
				break;
			}
			
			// Prints the worker did it's work
			System.out.println("Postal worker " + this.workerNumber + " finished serving customer " + this.custNum);
			
			// Tells the particular customer that the work has been done
			waitForWorkToBeFinished[this.custNum].release();
			
			//worker becomes available
			workers.release();
			}
			
			
		}
			
	}
	
	}
		

	
	
	



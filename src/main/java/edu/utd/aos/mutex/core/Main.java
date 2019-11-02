package edu.utd.aos.mutex.core;
import edu.utd.aos.mutex.exception.MutexException;
import edu.utd.aos.mutex.references.MutexConfigHolder;
import edu.utd.aos.mutex.references.MutexReferences;
import edu.utd.com.aos.nodes.Host;

/**
 * Main entry point for the program.
 * 
 * @author pankaj
 *
 */
public class Main {

	/**
	 * Main method.
	 * @param args
	 */
	public static void main(final String[] args) {
		try {
			initialize();
			start();
		}catch(final Exception e) {
			System.exit(MutexReferences.CONST_CODE_ERROR);
		}
	}
	
	/**
	 * To start executing the algorithm.
	 * @throws MutexException Catch all exception thrown during execution.
	 */
	private static void start() throws MutexException {
		Mutex.start();		
	}

	/**
	 * Initial setup.
	 * @throws MutexException
	 */
	private static void initialize() throws MutexException {
		MutexConfigHolder.initialize();
		Host.initialize();
	}
}
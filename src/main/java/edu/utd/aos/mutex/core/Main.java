package edu.utd.aos.mutex.core;
import org.tinylog.Logger;

import edu.utd.aos.mutex.exception.MutexException;
import edu.utd.aos.mutex.references.MutexConfigHolder;
import edu.utd.aos.mutex.references.MutexReferences;
import edu.utd.com.aos.nodes.Host;

public class Main {

	public static void main(final String[] args) {
		Logger.info("Hello World!");
		try {
			initialize();
			start();
		}catch(final Exception e) {
			System.exit(MutexReferences.CONST_CODE_ERROR);
		}
	}
	
	private static void start() throws MutexException {
		Mutex.start();		
	}

	private static void initialize() throws MutexException {
		MutexConfigHolder.initialize();
		Host.initialize();
	}
}
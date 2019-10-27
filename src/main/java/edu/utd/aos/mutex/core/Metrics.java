package edu.utd.aos.mutex.core;

import java.util.ArrayList;

import org.tinylog.Logger;

import edu.utd.com.aos.nodes.Host;
import edu.utd.com.aos.nodes.Host.Node;

public class Metrics {
	private static int sentMsg = 0;
	private static int recMsg = 0;
	private static int criticalSectionSent = 0;
	private static int criticalSectionRec = 0;
	private static long startTime;
	private static long endTime;
	private static long criticalSectionExit;
	private static ArrayList<String> metricsString = new ArrayList<String>();
	
	public static void incSentMsg() {
		sentMsg++;
	}
	
	public static void incRecMsg() {
		recMsg++;
	}
	
	public static int getTotalSentMsg() {
		return sentMsg;
	}
	
	public static int getTotalRecMsg() {
		return recMsg;
	}
	
	public static void takeCompletionSnapshot() {
		String msg1 = "Total no. of message SENT from BEGINNING until COMPLETION: " + sentMsg;
		metricsString.add(msg1);
		String msg2 = "Total no. of message RECEIVED from BEGINNING until COMPLETION: " + recMsg;
		metricsString.add(msg2);
	}

	public static void criticalSectionStartMsgSnapshot() {
		criticalSectionSent = sentMsg;
		criticalSectionRec = recMsg;		
	}

	public static void criticalSectionEndMsgSnapshot(int requestNo) {
		String msg = "Critical Section entry: " + (++requestNo) + ", total no "
				+ "of messages exchanged: " + ((sentMsg - criticalSectionSent) + (recMsg - criticalSectionRec));
		metricsString.add(msg);
	}

	public static void criticalSectionStartTimeSnapshot() {
		startTime = System.currentTimeMillis();		
	}

	public static void criticalSectionEndTimeSnapshot(int cnt) {
		endTime = System.currentTimeMillis();
		String msg = "Critical section entry: " + cnt + ". Total time elapsed between making "
				+ "a request and entering critical section: " + (endTime - startTime);
		metricsString.add(msg);
	}

	public static void noteCriticalSectionExit() {
		criticalSectionExit = System.currentTimeMillis();
	}

	public static void display() {
		for(String output: metricsString) {
			Logger.info(output);
		}	
	}
}

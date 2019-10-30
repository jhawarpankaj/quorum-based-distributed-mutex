package edu.utd.aos.mutex.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.tinylog.Logger;

import edu.utd.aos.mutex.dto.ApplicationConfig;
import edu.utd.aos.mutex.dto.MasterDetails;
import edu.utd.aos.mutex.dto.NodeDetails;
import edu.utd.aos.mutex.exception.MutexException;
import edu.utd.aos.mutex.references.MutexConfigHolder;
import edu.utd.aos.mutex.references.MutexReferences;
import edu.utd.com.aos.nodes.Host;

public class Client {
	
	public static int requestsCount = 0;
	public static HashMap<Integer, Boolean> clientRepliesCount = new HashMap<Integer, Boolean>();
	public static ArrayList<Integer> currentQuorum;
	public static boolean enteredCriticalSection;

	public static void start() throws MutexException {
		Quorum.initialize();
		startSocket();
		shutdown();
	}

	private static void startSocket() throws MutexException {
		Thread t1 = new ClientServerSockets();
		t1.start();
		boolean deadlock = false;
		while(requestsCount != 20) {
			ArrayList<Integer> randomQuorum = Quorum.getRandomQuorum();
//			randomQuorum = new ArrayList<Integer>(Arrays.asList(4, 5, 6, 7));
			Logger.info("Selected a quorum: " + randomQuorum);
			randomWait();
			currentQuorum = randomQuorum;
			enteredCriticalSection = false;
			Metrics.criticalSectionStartMsgSnapshot();
			Metrics.criticalSectionStartTimeSnapshot();
			if(Client.requestsCount != 0) {
				Metrics.exitAndReEntry(Client.requestsCount);
			}
			for(Integer id: randomQuorum) {
//				Client.randomWait();
				Map<String, String> serverById = Host.getServerById(id);
				Entry<String, String> entry = serverById.entrySet().iterator().next();
				String serverName = entry.getKey();
				int serverPort = Integer.valueOf(entry.getValue());
				long currentTimeStamp = System.currentTimeMillis();
				String clientId = String.valueOf(Host.getId());
				String formattedRequestMessage = Client.prepareRequestMessage(currentTimeStamp, clientId);
				Logger.info("Sending REQUEST to quorum member: " + serverById + ", i.e., server name:" + serverName);
				Mutex.sendMessage(serverName, serverPort, formattedRequestMessage);
			}
			int waitingTimeinSeconds = 0;
			while(!enteredCriticalSection) {
				Logger.info("Waiting for the old REQUEST to get COMPLETED before generating a new quorum.");
				Mutex.fixedWait(3);
				waitingTimeinSeconds+=3;
				if(waitingTimeinSeconds >= 90) {
					Logger.info("Looks like a DEADLOCK situation. Preparing to ABORT.");
					deadlock = true;
					break;
				}
			}
			if(deadlock) {
				Mutex.sendMessage(Host.getname(), Integer.valueOf(Host.getPort()), MutexReferences.ABORT);
			}
			Metrics.criticalSectionEndMsgSnapshot(requestsCount);
			requestsCount++;
			if(requestsCount < 20) {
				Logger.info("Last request successfully completed. Generating a new request.");
			}
			else {
				Logger.info("Completed 20 requests. Preparing to send COMPLETE to master.");
			}
		}
			
	}
	
	private static void shutdown() {
		ApplicationConfig applicationConfig = MutexConfigHolder.getApplicationConfig();
		NodeDetails nodeDetails = applicationConfig.getNodeDetails();
		MasterDetails masterDetails = nodeDetails.getMaster();
		String name = masterDetails.getName();
		int port = Integer.valueOf(masterDetails.getPort());
		Metrics.takeCompletionSnapshot();
		Mutex.sendMessage(name, port, MutexReferences.COMPLETE);
	}

	public static void randomWait() {
		Random rand = new Random();
		int low = 2;
		int high = 5;
		long result = rand.nextInt(high-low) + low;
		Logger.info("Sleeping for: " + result + " seconds.");
		try {
			TimeUnit.SECONDS.sleep(result);
		} catch (InterruptedException e) {
			Logger.error("Error while sleeping. " + e);
		}
	}
	
	// REQUEST||timestamp||id
	public static String prepareRequestMessage(long currentTimeStamp, String clientId) {
		String msg = MutexReferences.REQUEST + MutexReferences.SEPARATOR_TEXT + currentTimeStamp + MutexReferences.SEPARATOR_TEXT + clientId;
		return msg;
		
	}

	public static boolean gotRequiredReplies() {
		
		for(Integer serverId: currentQuorum) {
			Boolean gotReply = clientRepliesCount.getOrDefault(serverId, false);
			if(!gotReply) return false;
		}
		return true;
	}

}

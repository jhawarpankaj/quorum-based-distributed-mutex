package edu.utd.aos.mutex.core;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.tinylog.Logger;

import edu.utd.aos.mutex.exception.MutexException;
import edu.utd.aos.mutex.references.MutexReferences;
import edu.utd.com.aos.nodes.Host;

public class Client {
	
	public static int requestsCount = 0;
	public static HashMap<Integer, Boolean> clientRepliesCount = new HashMap<Integer, Boolean>();
	private static ArrayList<Integer> currentQuorum;
	private static boolean enteredCriticalSection;

	public static void start() throws MutexException {
		Quorum.initialize();
		startSocket();
	}

	private static void startSocket() throws MutexException {
		Thread t1 = new ClientServerSockets();
		t1.start();
		while(requestsCount != 20) {
			ArrayList<Integer> randomQuorum = Quorum.getRandomQuorum();
			randomWait();
			currentQuorum = randomQuorum;
			enteredCriticalSection = false;
			for(Integer id: randomQuorum) {
				Map<String, String> serverById = Host.getServerById(id);
				Entry<String, String> entry = serverById.entrySet().iterator().next();
				String serverName = entry.getKey();
				int serverPort = Integer.valueOf(entry.getValue());
				long currentTimeStamp = System.currentTimeMillis();
				String clientId = String.valueOf(Host.getId());
				String formattedRequestMessage = Client.prepareRequestMessage(currentTimeStamp, clientId);
				Socket socket = null;
				DataOutputStream out = null;
				try {
					socket = new Socket(serverName, serverPort);
					out = new DataOutputStream(socket.getOutputStream());
					out.writeUTF(formattedRequestMessage);
					socket.close();
				}catch(Exception e) {
					throw new MutexException("Error while sending request to server: " + serverName);
				}
			}
			requestsCount++;
			while(!enteredCriticalSection) {
				randomWait();
			}
		}
	}

	private static void randomWait() {
		Random rand = new Random();
		int low = 2;
		int high = 5;
		long result = rand.nextInt(high-low) + low;
		Logger.info("Sleeping for: " + result + " seconds before generating request.");
		try {
			TimeUnit.SECONDS.sleep(result);
		} catch (InterruptedException e) {
			Logger.error("Error while sleeping. " + e);
		}
		
	}

	private static String prepareRequestMessage(long currentTimeStamp, String clientId) {
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

package edu.utd.aos.mutex.core;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import org.tinylog.Logger;

import edu.utd.aos.mutex.dto.ApplicationConfig;
import edu.utd.aos.mutex.dto.MasterDetails;
import edu.utd.aos.mutex.dto.NodeDetails;
import edu.utd.aos.mutex.exception.MutexException;
import edu.utd.aos.mutex.references.MutexConfigHolder;
import edu.utd.aos.mutex.references.MutexReferences;
import edu.utd.com.aos.nodes.Host;

public class ClientServerSockets extends Thread {
	public static final ReentrantLock lock = new ReentrantLock();
	@Override
	public void run() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(Integer.parseInt(Host.getPort()));
			Logger.info("Setting up server socket for receiving requests.");
			while(true) {
				Socket clientSocket = null;
				clientSocket = serverSocket.accept();
				String serverHostName = clientSocket.getInetAddress().getHostName();
				Integer serverId = Host.getServerIdFromName(serverHostName);
				DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
	            String serverResponse = dis.readUTF();
	            Logger.info("Received message: " + serverResponse + ", from server: " + serverHostName);
	            lock.lock();
	            Metrics.incRecMsg();
	            if(serverResponse.equalsIgnoreCase(MutexReferences.GRANT)) {
	            	Logger.info("Received a GRANT message from server: " + serverHostName);
	            	if(Client.clientRepliesCount.containsKey(serverId)) {
	            		throw new MutexException("Client already had a request from server: " + serverHostName + ", but got another.");
	            	}
	            	else {
	            		Client.clientRepliesCount.put(serverId, true);
	            		Logger.info("Saved the grant: " + Client.clientRepliesCount);
	            		if(Client.gotRequiredReplies()) {
	            			Metrics.criticalSectionEndTimeSnapshot(Client.requestsCount);
	            			Logger.info("Got required replies to enter critical section.");
	            			ApplicationConfig applicationConfig = MutexConfigHolder.getApplicationConfig();
	            			NodeDetails nodeDetails = applicationConfig.getNodeDetails();
	            			MasterDetails masterDetails = nodeDetails.getMaster();
	            			String name = masterDetails.getName();
	            			int port = Integer.valueOf(masterDetails.getPort());
	            			long time = System.currentTimeMillis();
	            			String msg = Client.prepareRequestMessage(time, String.valueOf(Host.getId()));
	            			Mutex.sendMessage(name, port, msg);
	            			// then create a socket for fileServer and enter critical section.
	            			// make enteredCriticalSection true.
	            			// send RELEASE to all servers in the quorum.
	            		}
	            	}
	            }
	            else if(serverResponse.equalsIgnoreCase(MutexReferences.SUCCESS)) {
	            	Metrics.noteCriticalSectionExit(Client.requestsCount);
	            	Logger.info("Received a SUCCESS from Master server: " + serverHostName);
	            	Client.randomWait();
//	            	Mutex.fixedWait(10);
	            	ArrayList<Integer> currentQuorum = Client.currentQuorum;
	            	Logger.info("Sending a RELEASE to all current quorom servers.");
	            	Logger.info("Quorum servers to send RELEASE messages: " + currentQuorum);
	            	for(Integer id: currentQuorum) {
	            		Map<String, String> serverById = Host.getServerById(id);
	            		Entry<String, String> hm = serverById.entrySet().iterator().next();
    					String serverAddress = hm.getKey();
    					int serverPort = Integer.valueOf(hm.getValue());
    					Client.clientRepliesCount.remove(id);
    					Mutex.sendMessage(serverAddress, serverPort, MutexReferences.RELEASE);
	            	}
	            	Logger.info("After sending RELEASE to all servers, replies map: " + Client.clientRepliesCount);
	            	Client.enteredCriticalSection = true;
	            }
	            else if(serverResponse.equalsIgnoreCase(MutexReferences.ABORT)) {	            	
	            	Logger.info("Got an ABORT. Killing myself. Bye! Below are my metrics:");
//	            	Client.randomWait();
	            	Metrics.display();
	            	clientSocket.close();
	            	System.exit(1);
	            }
	            else {
	    			throw new MutexException("Unknown operation type. Message received: " + serverResponse);
	    		}
	            lock.unlock();
			}
		}catch(Exception e) {
			Logger.error("Error on client socket(acting as server): " + e);
		}
	}

}

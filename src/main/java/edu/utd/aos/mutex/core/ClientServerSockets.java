package edu.utd.aos.mutex.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

import org.tinylog.Logger;

import edu.utd.aos.mutex.exception.MutexException;
import edu.utd.aos.mutex.references.MutexReferences;
import edu.utd.com.aos.nodes.Host;

public class ClientServerSockets extends Thread {
	public static final ReentrantLock lock = new ReentrantLock();
	@Override
	public void run() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(Integer.parseInt(Host.getPort()));
			while(true) {
				Socket clientSocket = null;
				clientSocket = serverSocket.accept();
				String serverHostName = clientSocket.getInetAddress().getHostName();
				Integer serverId = Host.getServerIdFromName(serverHostName);
				DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
	            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
	            String serverResponse = dis.readUTF();
	            lock.lock();
	            if(serverResponse.equalsIgnoreCase(MutexReferences.GRANT)) {
	            	if(Client.clientRepliesCount.containsKey(serverId)) {
	            		throw new MutexException("Client already had a request from server: " + serverHostName + ", but got another.");
	            	}
	            	else {
	            		Client.clientRepliesCount.put(serverId, true);
	            		if(Client.gotRequiredReplies()) {
	            			// then create a socket for fileServer and enter critical section.
	            			// make enteredCriticalSection true.
	            			// send RELEASE to all servers.
	            		}
	            		
	            	}
	            }
	          
			}
		}catch(Exception e) {
			Logger.error("Error on client socket(acting as server): " + e);
		}
	}

}

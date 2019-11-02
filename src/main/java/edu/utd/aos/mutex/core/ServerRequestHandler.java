package edu.utd.aos.mutex.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import org.tinylog.Logger;

import edu.utd.aos.mutex.exception.MutexException;
import edu.utd.aos.mutex.references.MutexReferences;
import edu.utd.com.aos.nodes.Host;

/**
 * Requests handler for quorum servers.
 * 
 * @author pankaj
 *
 */
public class ServerRequestHandler extends Thread {
	final Socket worker;
	final DataInputStream dis;
    final DataOutputStream dos;
    public static final ReentrantLock lock = new ReentrantLock();
    
    public ServerRequestHandler(Socket worker, DataInputStream dis, DataOutputStream dos) {
    	this.worker = worker;
    	this.dis = dis;
    	this.dos = dos;
    }
    
    @Override
    public void run(){
    	try {
    		String received = dis.readUTF();
    		String clientName = this.worker.getInetAddress().getHostName();    		
    		ArrayList<String> parsedMsg = Server.parseRequest(received);
    		lock.lock();
    		Metrics.incRecMsg();
    		String reqType = parsedMsg.get(0);
    		if(reqType.equalsIgnoreCase(MutexReferences.REQUEST)) {
    			Logger.info("Received REQUEST from client: " + clientName);
    			if(Server.isLocked()) {
    				Logger.info("Server already in LOCKED state. Putting this request in queue.");
    				Long time = Long.valueOf(parsedMsg.get(1));
    				int clientId = Integer.valueOf(parsedMsg.get(2));
    				ClientRequestQueue parseClientRequest = new ClientRequestQueue(time, clientId);
    				Server.priorityQueue.add(parseClientRequest);
    				Server.printQueue();
    			}
    			else {
    				Logger.info("Server currently UNLOCKED. Going to LOCKED state and sending GRANT to client: " + clientName);
    				Server.toggleState(); // server goes to LOCKED state.
    				int port = Host.getClientPortByName(clientName);
    				Logger.info("Port: " + port);
    				Mutex.sendMessage(clientName, port, MutexReferences.GRANT);
    			}
    		}
    		else if(reqType.equalsIgnoreCase(MutexReferences.RELEASE)){
    			Logger.info("Received a RELEASE from client: " + clientName);
    			if(!Server.isLocked()) {
    				throw new MutexException("Received a RELEASE from client: " + clientName + " , but STATE was UNLOCKED!!");
    			}
    			else {
    				if(Server.priorityQueue.isEmpty()) {
    					Logger.info("Server locked status was: " + Server.state + ", and "
    							+ "currently Queue is empty, so going to: " + !Server.state);
    					Server.toggleState();
    				}
    				else {
    					Logger.info("Server locked status was: " + Server.state + ", but "
    							+ "queue was not empty. Hence continuing in the same state." );
    					Logger.info("Current queue: " + Server.priorityQueue);
    					ClientRequestQueue poll = Server.priorityQueue.poll();
    					int id = poll.getId();
    					Map<String, String> client = Host.getClientById(id);
    					Entry<String, String> hm = client.entrySet().iterator().next();
    					String clientAddress = hm.getKey();
    					int clientPort = Integer.valueOf(hm.getValue());
    					Logger.info("Sending GRANT to the first entry in queue.");
    					Mutex.sendMessage(clientAddress, clientPort, MutexReferences.GRANT);
    				}
    			}
    		}
    		else if(reqType.equalsIgnoreCase(MutexReferences.ABORT)){
    			Logger.info("Master server sent an ABORT message. Bye! Below are my metrics:");
//    			Client.randomWait();
    			Metrics.takeCompletionSnapshot();
            	Metrics.display();
    			this.worker.close();
    			System.exit(1);
    		}
    		else {
    			throw new MutexException("Unknown operation type. Message received: " + received);
    		}
    		lock.unlock();
    	}catch(IOException | MutexException e) {
    		Logger.error("Error while performing client request: " + e);
    	}	
    }
}

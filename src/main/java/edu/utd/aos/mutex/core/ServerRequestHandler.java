package edu.utd.aos.mutex.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import org.tinylog.Logger;

import edu.utd.aos.mutex.exception.MutexException;
import edu.utd.aos.mutex.references.MutexReferences;
import edu.utd.com.aos.nodes.Host;

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
    		boolean isRequest = Server.isRequest(received);
    		lock.lock();
    		if(isRequest) {
    			if(Server.isLocked()) {
    				ClientRequestQueue parseClientRequest = Server.parseClientRequest(received);
    				Server.priorityQueue.add(parseClientRequest);
    			}
    			else {
    				Server.toggleState();
    				dos.writeUTF(MutexReferences.GRANT);
    			}
    		}
    		else {
    			if(!Server.isLocked()) {
    				throw new MutexException("Received a RELEASE from client: " + clientName + " , but STATE was UNLOCKED!!");
    			}
    			else {
    				if(Server.priorityQueue.isEmpty()) {
    					Server.toggleState();
    				}
    				else {
    					ClientRequestQueue poll = Server.priorityQueue.poll();
    					int id = poll.getId();
    					Map<String, String> client = Host.getClientById(id);
    					Entry<String, String> hm = client.entrySet().iterator().next();
    					String clientAddress = hm.getKey();
    					int clientPort = Integer.valueOf(hm.getValue());
    					Socket socket = null;
    					DataOutputStream out = null;
    					socket = new Socket(clientAddress, clientPort);
    					out = new DataOutputStream(socket.getOutputStream());
    					out.writeUTF(MutexReferences.GRANT);
    					socket.close();
    				}
    			}
    		}
    		lock.unlock();
    	}catch(IOException | MutexException e) {
    		Logger.error("Error while performing client request: " + e);
    	}	
    }
}

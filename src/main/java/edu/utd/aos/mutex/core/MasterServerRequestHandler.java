package edu.utd.aos.mutex.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.tinylog.Logger;

import edu.utd.aos.mutex.exception.MutexException;
import edu.utd.aos.mutex.references.MutexReferences;
import edu.utd.com.aos.nodes.Host;

/**
 * Handling all requests received by the master server.
 * @author pankaj
 *
 */
public class MasterServerRequestHandler extends Thread {
	final Socket worker;
	final DataInputStream dis;
    final DataOutputStream dos;
    public static final ReentrantLock lock = new ReentrantLock();
    
    public MasterServerRequestHandler(Socket worker, DataInputStream dis, DataOutputStream dos) {
    	this.worker = worker;
    	this.dis = dis;
    	this.dos = dos;
    }
    
    @Override
    public void run(){
    	try {
    		String received = dis.readUTF();
    		String clientName = this.worker.getInetAddress().getHostName();
    		int clientId = Host.getClientIdFromName(clientName);
    		Map<String, String> client = Host.getClientById(clientId);
			Entry<String, String> hm = client.entrySet().iterator().next();
			int clientPort = Integer.valueOf(hm.getValue());    		
    		ArrayList<String> parseInputMessage = Master.parseInputMessage(received);
    		Metrics.incRecMsg();
    		if(parseInputMessage.get(0).equalsIgnoreCase(MutexReferences.REQUEST)) {
    			Logger.info("Received a REQUEST from server: " + clientName);
    			lock.lock();
    			File fileWrite = new File(Host.getFilePath() + Host.getFileName());
    			String textToAppend = "request from " + parseInputMessage.get(2) + " " + parseInputMessage.get(1);
    			try {
    				FileUtils.writeStringToFile(fileWrite, textToAppend + "\n", MutexReferences.ENCODING, true);
    			} catch (IOException e) {
    				Logger.error("Error while writing operations to file: " + e);
    			}
    			Logger.info("WRITE was successful for client: " + clientName);
    			Mutex.sendMessage(clientName, clientPort, MutexReferences.SUCCESS);
    			lock.unlock();
    		}
    		else if(parseInputMessage.get(0).equalsIgnoreCase(MutexReferences.COMPLETE)) {
    			Logger.info("Received COMPLETE from client: " + clientName);
    			Master.updateCompletionCount(clientName);
    			if(Master.allRequestsCompleted()) {
    				Master.sendAbort();
    			}
    		}
    		else {
    			throw new MutexException("Unknown operation type. Message received: " + received);
    		}
    	}catch(Exception e) {
    		Logger.error("Error while performing client request: " + e);
    	}
    }
}

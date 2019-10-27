package edu.utd.aos.mutex.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.tinylog.Logger;

import edu.utd.aos.mutex.dto.ApplicationConfig;
import edu.utd.aos.mutex.dto.ClientDetails;
import edu.utd.aos.mutex.dto.NodeDetails;
import edu.utd.aos.mutex.dto.ServerDetails;
import edu.utd.aos.mutex.exception.MutexException;
import edu.utd.aos.mutex.references.MutexConfigHolder;
import edu.utd.aos.mutex.references.MutexReferences;
import edu.utd.com.aos.nodes.Host;

public class Master {
	
	private static Map<String, Boolean> completionCount = new HashMap<String, Boolean>();

	public static void start() throws MutexException{
		try {
			initialize();
			openSocket();
		}catch(Exception e) {
			throw new MutexException("Error in master server intialization or socket:" + e);
		}
	}
	
	public static void updateCompletionCount(String client) {
		Logger.info("Noting the completion for client: " + client);
		completionCount.put(client, true);
	}
	
	public static boolean allRequestsCompleted() {
		ApplicationConfig applicationConfig = MutexConfigHolder.getApplicationConfig();
		NodeDetails nodeDetails = applicationConfig.getNodeDetails();
		List<ClientDetails> clientDetails = nodeDetails.getClients();
		for(ClientDetails client: clientDetails) {
			String name = client.getName();
			Boolean status = completionCount.getOrDefault(name, false);
			if(status == false) {
				return false;
			}
		}
		return true;
	}

	private static void openSocket() throws IOException, MutexException {
		Logger.info("Setting up master server socket for all external requests.");
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(Integer.parseInt(Host.getPort()));
			while(true) {
				Socket clientSocket = null;
				clientSocket = serverSocket.accept();
				DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
	            Thread t = new MasterServerRequestHandler(clientSocket, dis, dos);
	            t.start();
			}
		}catch(Exception e) {
			serverSocket.close();
			throw new MutexException("Error in server socket." + e);
		}
	}

	private static void initialize() throws MutexException {
		String filePath = Host.getFilePath();
		String fileName = Host.getFileName();
		Logger.info("Creating file on the server at path: " + filePath);
		File target = new File(filePath + fileName);
		try {
			FileUtils.deleteDirectory(new File(filePath));
			FileUtils.forceMkdir(new File(filePath));
			Logger.info("Creating new file: " + target);
			FileUtils.touch(target);
		}
		catch(IOException e) {			
			throw new MutexException("Error while creating file on the server:" + e);	
		}
	}
	// REQUEST||<TIMESTAMP>||CLIENT_ID
	// COMPLETE
	public static ArrayList<String> parseInputMessage(String received) {
		ArrayList<String> result = new ArrayList<String>();
		String[] split = received.split(MutexReferences.SEPARATOR);
		result.add(split[0]);
		if(split.length == 3) {
			result.add(split[1]);
			result.add(split[2]);
		}
		return result;
	}

	public static void sendAbort() {
		
		ApplicationConfig applicationConfig = MutexConfigHolder.getApplicationConfig();
		NodeDetails nodeDetails = applicationConfig.getNodeDetails();
		List<ServerDetails> serverDetails = nodeDetails.getServers();
		List<ClientDetails> clientDetails = nodeDetails.getClients();
		
		for(ServerDetails server: serverDetails) {
			String name = server.getName();
			int port = Integer.valueOf(server.getPort());
			Logger.info("Sending ABORT to server: " + name);
			Mutex.sendMessage(name, port, MutexReferences.ABORT);
		}
		
		for(ClientDetails client: clientDetails) {
			String name = client.getName();
			int port = Integer.valueOf(client.getPort());
			Logger.info("Sending ABORT to client: " + name);
			Mutex.sendMessage(name, port, MutexReferences.ABORT);
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			Logger.error("No peace even before death !!!");
		}
		Logger.info("Killing myself !! Below are my metrics:");
		Metrics.display();
		System.exit(1);
	}
}

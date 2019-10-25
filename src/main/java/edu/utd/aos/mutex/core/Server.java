package edu.utd.aos.mutex.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.PriorityQueue;

import org.tinylog.Logger;

import edu.utd.aos.mutex.exception.MutexException;
import edu.utd.aos.mutex.references.MutexReferences;
import edu.utd.com.aos.nodes.Host;

public class Server {
	
	private static boolean state = false;
	
	public static PriorityQueue<ClientRequestQueue> priorityQueue = new PriorityQueue<>();

	public static void start() throws MutexException {
		try {
			openSocket();
		}catch(IOException e) {
			throw new MutexException("Error while opening server socket.");
		}
	}

	private static void openSocket() throws MutexException, IOException {
		Logger.info("Setting up for server sockets.");
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(Integer.parseInt(Host.getPort()));
			while(true) {
				Socket clientSocket = null;
				clientSocket = serverSocket.accept();
				DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
	            Thread t = new ServerRequestHandler(clientSocket, dis, dos);
	            t.start();
			}
		}catch(Exception e) {
			serverSocket.close();
			throw new MutexException("Error in server socket." + e);
		}
	}
	
	public static boolean isLocked() {
		return state;
	}
	
	public static void toggleState() {
		state = !state;
	}

	public static boolean isRequest(String input) {
		String[] parsedString = input.split(MutexReferences.SEPARATOR);
		if(parsedString[0].equalsIgnoreCase(MutexReferences.REQUEST)) return true;
		else return false;
	}

	// format of Req: REQUEST||timestamp||client_id
	public static ClientRequestQueue parseClientRequest(String received) {
		String[] parsedString = received.split(MutexReferences.SEPARATOR);
		return new ClientRequestQueue(Long.valueOf(parsedString[1]), Integer.valueOf(parsedString[2])); 
	}

}

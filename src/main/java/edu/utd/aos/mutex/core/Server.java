package edu.utd.aos.mutex.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.PriorityQueue;

import org.tinylog.Logger;

import edu.utd.aos.mutex.exception.MutexException;
import edu.utd.aos.mutex.references.MutexReferences;
import edu.utd.com.aos.nodes.Host;

public class Server {
	
	public static boolean state = false;
	
	public static PriorityQueue<ClientRequestQueue> priorityQueue = new PriorityQueue<>();

	public static void start() throws MutexException {
		try {
			openSocket();
		}catch(IOException e) {
			throw new MutexException("Error while opening server socket.");
		}
	}

	private static void openSocket() throws MutexException, IOException {
		Logger.info("Setting up for server sockets for external requests.");
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

	// format of Req: REQUEST||timestamp||client_id
	public static ArrayList<String> parseRequest(String received) {
		ArrayList<String> result = new ArrayList<String>();
		String[] parsedString = received.split(MutexReferences.SEPARATOR);
		result.add(parsedString[0]);
		if(parsedString.length == 3) {
			result.add(parsedString[1]);
			result.add(parsedString[2]);
		}		
		return result; 
	}
	
	public static void printQueue() {
		String temp = "";
		for(ClientRequestQueue obj: Server.priorityQueue) {
			temp = String.valueOf(obj.getId()) + ", ";
		}
		temp = temp.substring(0, temp.length() - 1);
		Logger.info("Current Queue elements: " + temp);
	}

}

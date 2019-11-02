package edu.utd.aos.mutex.core;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.tinylog.Logger;

import edu.utd.aos.mutex.exception.MutexException;
import edu.utd.com.aos.nodes.Host;
import edu.utd.com.aos.nodes.Host.Node;

/**
 * To start execution for client, server or master server.
 * @author pankaj
 *
 */
public class Mutex {
	
	public static void start() throws MutexException {
		Node type = Host.getType();
		switch(type) {
			case MASTER:
				Master.start();
				break;
			case SERVER:
				Server.start();
				break;
			case CLIENT:
				Logger.info("Sleeping for 1/2 minutes until all clients are up.");
				fixedWait(30);
				Client.start();
				break;
			default:
				throw new MutexException("Unidentified node type.");
		}
	}
	
	public static void sendMessage(String name, int port, String message) {
		try {
			Socket socket = null;
			DataOutputStream out = null;
			socket = new Socket(name, port);
			out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF(message);
			Metrics.incSentMsg();
			socket.close();
			Logger.info("Sent " + message + " message to node: " + name);
		}catch(Exception e) {
			Logger.error("Error while sending " + message + " message to node: " + name);
		}
	}
	
	public static void fixedWait(int time) {
		Logger.info("Waiting for " + time + " seconds.");
		try {
			TimeUnit.SECONDS.sleep(time);
		} catch (InterruptedException e) {
			Logger.error("Error while sleeping. " + e);
		}
	}
}

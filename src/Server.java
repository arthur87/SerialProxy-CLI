import java.io.*;
import java.net.*;
import java.util.*;
public class Server extends Thread{
	private ServerSocket serverSocket;
	private Socket socket;
	private int port;
	private Main proxy;
	
	private Vector<Client> clist;
	public Server(Main proxy, int port) {
		this.proxy = proxy;
		this.port = port;
		clist = new Vector<Client>();
	}
	
	public void run() {
		proxy.printLogMessage("\nStarting server.");
		try {
			serverSocket = new ServerSocket(port);
			proxy.printLogMessage("Server started on port " + port + ".");
			while(true) {
				socket = serverSocket.accept();
				Client c = new Client(this, socket);
				clist.add(c);
				c.start();
				proxy.printLogMessage("\n" + c.getIp() + " connected to the server.");
				getClients();
			}
		}catch(IOException e) {
			proxy.printLogMessage("\nConnection error inside server.");
			//e.printStackTrace();
			stopServer();
		}
	}
	public void stopServer() {
		try {
			serverSocket.close();
			proxy.printLogMessage("\nServer closed.");
		}catch(IOException e) {
			//e.printStackTrace();
		}
	}
	public void deleteClient(Client c) {
		proxy.printLogMessage("\n" + c.getIp() + " disconnected.");
		clist.remove(c);
		if(clist.size() == 0) {
			
		}
		this.getClients();
	}
	public void dispose() {
		if(clist != null) {
			Enumeration<Client> e = clist.elements();
			while(e.hasMoreElements()) {
				Client c = (Client)(e.nextElement());
				c.dispose();
			}
		}
	}
	
	private void getClients() {
		if(clist != null) {
			proxy.printLogMessage("\nGet the client(s).");
			Enumeration<Client> e = clist.elements();
			while(e.hasMoreElements()) {
				Client c = (Client)(e.nextElement());
				proxy.printLogMessage(" " + c.getIp());
			}
			proxy.printLogMessage(" Total " + clist.size() + " client(s).");
		}		
	}
	
	public void sendToAllClient(String s) {
		if(clist != null) {
			Enumeration<Client> e = clist.elements();
			while(e.hasMoreElements()) {
				Client c = (Client)(e.nextElement());
				c.sendToClient(s + "\0");
			}
		}
	}
	
	public void reserveMessage(String s) {
		proxy.sendToSerial(s);
	}
}

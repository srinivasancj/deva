package deva.agent;

/*
 * Code from: http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html
 * Shows how we can create a java chat room..
 * run ChatServer.java.. it will start and wait for clients to connect
 * Start two or more ChatClient.java instances
 * Type in one of them, press return..
 * Message is sent to all other chat clients through the server
 */


import java.net.*;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import deva.im.DialogueManager;

public class DEVAServer implements Runnable
{  private ServerThread clients[] = new ServerThread[50];
   private ServerSocket server = null;
   private Thread       thread = null;
   private int clientCount = 0;
   private String[] clientNames = new String[50];
   private Timer timer;
   
   Logger logger = Logger.getLogger(DEVAServer.class.getName());
   
   class RemindTask extends TimerTask {
       public void run() {
           //System.out.format("Time's up!%n");
           System.out.println("TIME UP!");
           handle(-1, "timeOut");
           //timer.cancel(); 
           timer.schedule(new RemindTask(), 5*1000);             
          
       }
   }
   
   public DEVAServer(int port)
   {  
	  PropertyConfigurator.configure("log4jserver.properties"); 
	  
	  try
      {  logger.info("Binding to port " + port + ", please wait  ...");
         server = new ServerSocket(port);  
         logger.info("Server started: " + server);
         timer = new Timer();
         timer.schedule(new RemindTask(), 5*1000);
         
         start(); 
         
         
      }
      catch(IOException ioe)
      {  logger.info("Can not bind to port " + port + ": " + ioe.getMessage()); }
   }
   public void run()
   {  while (thread != null)
      {  try
         {  logger.info("Waiting for a client ..."); 
            addThread(server.accept()); }
         catch(IOException ioe)
         {  logger.info("Server accept error: " + ioe); stop(); }
      }
   }
   public void start()  { 
	   if (thread == null)
	      {  thread = new Thread(this); 
	         thread.start();
	      }
   }
   public void stop()   { 
	   if (thread != null)
	      {  thread.stop(); 
	         thread = null;
	      }
   
   }
   private int findClient(int ID)
   {  for (int i = 0; i < clientCount; i++)
         if (clients[i].getID() == ID)
            return i;
      return -1;
   }
   
   private int findClient(String name)
   {  for (int i = 0; i < clientCount; i++)
         if (clients[i].getClientName().equals(name))
            return i;
      return -1;
   }
   
   public synchronized void handle(int ID, String input)
   {  if (input.equals(".bye"))
	  {  
	     for (int i = 0; i < clientCount; i++){
	        clients[i].send(".bye");
	     }
	     //clients[findClient(ID)].send(".bye");
         remove(ID); 
      }
      else {
    	 if (input.equals("timeOut")){
    		 //System.out.println("TIMEOUT");
    		 for (int i = 0; i < clientCount; i++){
    			JSONArray dmOut = new JSONArray();
    			JSONObject jo = new JSONObject();
    			try {
					jo.put("timeOut", "true");
				} catch (JSONException e) {
					e.printStackTrace();
				}
    			dmOut.put(jo);
    			String outMessage= dmOut.toString();
 		        clients[i].send(outMessage);
 		        //logger.info("Sending timeout to " + clients[i].getClientName());
 		     }
    	 }
    	 else if (input.startsWith("name=")){
    		 String[] t = input.split("=");
    		 clients[findClient(ID)].setClientName(t[1]);
    		 logger.info("Setting name:" + t[1] + " to client " + ID);
    		 //sending the message to this client, that other regd clients are ready
	         for (int i = 0; i < clientCount; i++){
	        	clients[findClient(ID)].send("ready="+clients[i].getClientName());
	         }
	         //sending message to all regd clients that this client is ready
	         for (int i = 0; i < clientCount; i++){
		        clients[i].send("ready="+t[1]);
		     }
    	 } else {
    		 if (input.contains(";")){
    			 logger.info(input);
    			 timer.cancel();
    			 timer = new Timer();
    	         timer.schedule(new RemindTask(), 5*1000);
    	         String[] t = input.split(";");
    			 String sender = t[0];
    			 String message = t[1];
    			 int i = findClient(sender);
    			 //sending the message to all clients but sender
		         for (int j = 0; j < clientCount; j++){
		        	if (i != j){
		        		clients[j].send(message);
		        	}
		         }
    		 } else {
		    	 //sending the message to all clients
		         for (int i = 0; i < clientCount; i++){
		        	clients[i].send(input);
		         }
    		 }
    	 }
      }
   }
   public synchronized void remove(int ID)
   {  int pos = findClient(ID);
      if (pos >= 0)
      {  ServerThread toTerminate = clients[pos];
         logger.info("Removing client thread " + ID + " at " + pos);
         if (pos < clientCount-1)
            for (int i = pos+1; i < clientCount; i++)
               clients[i-1] = clients[i];
         clientCount--;
         try
         {  toTerminate.close(); }
         catch(IOException ioe)
         {  logger.info("Error closing thread: " + ioe); }
         toTerminate.stop(); }
   }
   private void addThread(Socket socket)
   {  if (clientCount < clients.length)
      {  logger.info("Client accepted: " + socket);
         clients[clientCount] = new ServerThread(this, socket);
         try
         {  clients[clientCount].open(); 
            clients[clientCount].start();  
            clientCount++; 
         }
         catch(IOException ioe)
         {  logger.info("Error opening thread: " + ioe); } }
      else
         logger.info("Client refused: maximum " + clients.length + " reached.");
   }
   public static void main(String args[]) { 
	   DEVAServer server = null;
	   server = new DEVAServer(5051); 
	   System.out.println("Server started");
   }
}
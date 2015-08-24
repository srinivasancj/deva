package deva.agent;

import java.net.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.io.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import deva.im.DialogueManager;

public class DEVAClient implements Runnable
{  private Socket socket              = null;
   protected Thread thread              = null;
   private DataInputStream  console   = null;
   protected DataOutputStream streamOut = null;
   private String serverName;
   private int serverPort;
   private ClientThread client    = null;
   private String clientName;
   private String[] requiredAgents;
   protected Boolean allAgentsReady;
   protected Hashtable<String,Boolean> agentsReady;
   protected Boolean messageReady;
   protected String outMessage;
   
   Logger logger = Logger.getLogger(DEVAClient.class.getName());
   
   
   public DEVAClient(String clientName, String serverName, int serverPort, String[] requiredAgents)
   {  
	  PropertyConfigurator.configure("log4j.properties");
	  this.clientName = clientName;
	  this.requiredAgents = requiredAgents;
	  
	  
	  logger.info("Establishing connection. Please wait ...");
	  this.serverName = serverName;
	  this.serverPort = serverPort;
	  
      try
      {  socket = new Socket(serverName, serverPort);
         logger.info("Connected: " + socket);
      }
      catch(UnknownHostException uhe)
      {  logger.info("Host unknown: " + uhe.getMessage()); }
      catch(IOException ioe)
      {  logger.info("Unexpected exception: " + ioe.getMessage()); }
   }
   
   
   protected void reset() {
	   messageReady = false;
		  
	   allAgentsReady = false;
	   agentsReady = new Hashtable<String,Boolean>();
	   for (int i=0; i< requiredAgents.length; i++){
		  agentsReady.put(requiredAgents[i], false);
	   }
	
   }
   
   
   @SuppressWarnings("deprecation")
   public void run()
   {  
	  synchronized (messageReady){
		  while (thread != null)
		  {  
		     if (messageReady){
			   	 try
		         {  
		    	  	streamOut.writeUTF(clientName + ";" + outMessage);
		            streamOut.flush();
		            messageReady = false;
		         }
		         catch(IOException ioe)
		         {  logger.info("Sending error: " + ioe.getMessage());
		            stop();
		         }
		     } else {
		    	 //logger.info(dm.getName() +":Message not ready!");
		    	 try {
					Thread.sleep(1000);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		     }
		     //System.out.println("Waiting for message to publish..");
	      }
	  }
   }
   
   //clientThread that listens to messages from the server 
   //invokes handle(msg) for the client to handle the input message
   //this needs to be overridden by any subclass of DEVAClient
   public void handle(String msg){
	   handleBasic(msg);
   }
   
   //any input to this module is handled here..
   public Boolean handleBasic(String msg)
   {   
	   //logger.info("Handler input: " + msg);
	   
	   if (msg.equals(".bye")){  
		   logger.info("Good bye. Press RETURN to exit ...");
	       stop();
	       return true;
	   } 
	   else if (msg.startsWith("ready=")){
			String[] t = msg.split("=");
			if (agentsReady.containsKey(t[1])){
				  agentsReady.put(t[1], true);
				  logger.info("Setting " + t[1] + " to ready");
			}
			Boolean allAgentsReadyTemp = true;
			Enumeration<String> agents = agentsReady.keys();
			while (agents.hasMoreElements()){
				  String nextAgent = agents.nextElement();
				  if (!agentsReady.get(nextAgent)){
					  allAgentsReadyTemp = false;
					  break;
				  }
			}
			allAgentsReady = allAgentsReadyTemp;
			return true;
	   }
	   return false;
   }
   
   public void start() throws IOException
   {  
	  
		  console   = new DataInputStream(System.in);
	      streamOut = new DataOutputStream(socket.getOutputStream());
	      if (thread == null)
	      {  client = new ClientThread(this, socket);
	         thread = new Thread(this);   
	         try
	         {  streamOut.writeUTF("name=" + clientName);
	            streamOut.flush();
	         }
	         catch(IOException ioe)
	         {  logger.info("Sending error: " + ioe.getMessage());
	            stop();
	         }
	         logger.info(clientName + " INITIATED");
	         
	         while (!allAgentsReady){
		         try {
		        	logger.info("Waiting for required agents!");
		 			Thread.sleep(1000);
		 		 } catch (InterruptedException e) {
		 			e.printStackTrace();
		 		 }
	         }
	         
	         
	   	  
	         //thread.start();
	      }
	  
   }
   
   
   

   @SuppressWarnings("deprecation")
   public void stop()
   {  
	  if (thread != null)
	      {  thread.stop();  
	         thread = null;
	      }
      try
      {  if (console   != null)  console.close();
         if (streamOut != null)  streamOut.close();
         if (socket    != null)  socket.close();
      }
      catch(IOException ioe)
      {  logger.info("Error closing ..."); }
      client.close();  
      client.stop();
   }
   
   public static void main(String args[])
   {  
   }

   
}

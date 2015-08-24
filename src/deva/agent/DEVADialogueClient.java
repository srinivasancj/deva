package deva.agent;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import deva.im.DialogueManager;
import deva.learner.RewardsCalculator;

public class DEVADialogueClient extends DEVAClient {

	private DialogueManager dm;
    private int maxRunPerEpisode;
    private JSONArray dmOut, dmIn;
   
    private Integer episodeCounter, totalEpisodes, halveningEpisode;
    private Boolean episodeEnded;
    private Double turnReward, finalReward;
    private RewardsCalculator rc;
	
    Logger logger = Logger.getLogger(DEVAClient.class.getName());
    
	public DEVADialogueClient(String clientName, String serverName, int serverPort, String[] requiredAgents) {
		super(clientName, serverName, serverPort, requiredAgents);
		logger = Logger.getLogger(clientName);
		
		episodeCounter = 0;
		totalEpisodes = -1;
		maxRunPerEpisode = -1;
		episodeEnded = false;
		rc = null;
		 
		reset();
		
	}

	//any input to this module is handled here..
	public synchronized void handle(String msg){ 
		
	   logger.info("Handler input: " + msg);
	   if (handleBasic(msg)){
		   logger.info("Input handled by basic handler");
		   return;
	   }
	   
	   synchronized (this){ 
		   
		   if (msg.equals("episodeEnded") && !episodeEnded){
			  episodeEnded = true;
			  logger.info(dm.getName() + " Episode ended!" + episodeCounter);
			  try {
				  	if (rc != null){
				  		Double r = rc.calculateReward(dm);
				  		logger.info("REWARD CALCULATED: " + r);
				  		dm.setFinalReward(r);
				  	} else {
				  		dm.setFinalReward(0.0);
				  	}
					dm.displayLAReport(episodeCounter);
					
			  } catch (JSONException e) {
					e.printStackTrace();
			  }
			  
			  
			  episodeCounter++;
			  if (totalEpisodes > 0){
				  if (episodeCounter == halveningEpisode){
					dm.reduceLAEpsilonByHalf();
					halveningEpisode += ((totalEpisodes - episodeCounter) / 2);
				  }
				  
				  if (episodeCounter >= totalEpisodes){
					  
					  dm.saveLAPolicy();
					  
					  try {
							streamOut.writeUTF(".bye");
							streamOut.flush();
							return;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				  }
			  }
			  
			  dm.reset();
			  dm.setDStateVariable("interactionStatus", "started");
			  JSONObject k = new JSONObject();
			  dmOut = dm.run(k, this.turnReward);
			  outMessage = dmOut.toString();
			  messageReady = true;
			  logger.info(dm.getName() + " >> " + dmOut.toString());
			  
		  }
	      else {
	         try {
	        	JSONArray ja = new JSONArray(msg);
	        	//logger.info("Input > " + ja.toString());
	        	JSONObject k;
	        	if (ja.length() == 0){
	        		return;
	        		
	        	} else {
	        		k = ja.getJSONObject(0);
	        	}
	        	
	        	
	        	if (k.has("timeOut") && k.getString("timeOut").equals("true") && !allAgentsReady){
	        	   	logger.info("Waiting for required agents!");
	        	   	return;
	        	}
	        	
	        	if (k.has("toModule") && k.getString("toModule").equals("hub")){
					if (k.has("interactionStatus") && k.getString("interactionStatus").equals("stopped")){
						//message sent to hub by the other agent to stop interaction
						//logger.info(dm.getName() + " INTERACTION STOPPED BY OTHER AGENT!");
						return;
					}
	        	}
	        	
	        	episodeEnded = false;
	        	
	        	dmOut = dm.run(k, this.turnReward);
	        	outMessage = dmOut.toString();
				messageReady = true;
				if (dmOut.length() > 0){
					JSONObject k2 = dmOut.getJSONObject(0);
					if (k2.has("toModule") && k2.getString("toModule").equals("hub")){
						if (k2.has("interactionStatus") && k2.getString("interactionStatus").equals("stopped")){
							logger.info(dm.getName() + " STOPPING INTERACTION!");
							messageReady = false;
							try {
								streamOut.writeUTF("episodeEnded");
								streamOut.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				            
							
						}
					}
				}
				
				//stop interaction when it goes beyond maxRunPerEpisode
				if (this.maxRunPerEpisode > 0 && dm.getRunCount() >= this.maxRunPerEpisode){
					messageReady = false;
					try {
						streamOut.writeUTF("episodeEnded");
						streamOut.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (messageReady){
					logger.info(dm.getName() + " >> " + dmOut.toString());
				}
				  
			} catch (JSONException e) {
				e.printStackTrace();
			}
	      }
	   }
   }
	
   public void startEpisode() {
	   try {
			start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	   synchronized (messageReady) {
		   dmOut = dm.run(new JSONObject(), 0.0);
		   outMessage= dmOut.toString();
		   logger.info(dm.getName() + " >> " + outMessage);
	       messageReady = true;
	   }
	   thread.start();
   }
   
   private void resetDM() {
		 dm.reset();
		 dm.setDStateVariable("interactionStatus", "started");
   }

	
	public void setEpisodeParameters(Integer totalEpisodes){
		  this.totalEpisodes = totalEpisodes;
		  this.halveningEpisode = totalEpisodes / 2;
	}
	   
    public void setTurnReward(Double d){
	   this.turnReward = d;
    }
   
    public void setFinalReward(Double d){
	   this.finalReward = d;
    }
    
    public void setRewardsCalculator(RewardsCalculator rc){
 	   this.rc = rc;
     }
    
    
    public void setMaxRunPerEpisode(int i) {
		this.maxRunPerEpisode = i;
		
    }
	
	public void setDialogueManager(DialogueManager dm) {
		this.dm = dm;
		resetDM();
	}

}

package deva.learner;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import deva.im.Domains;
import deva.im.strategy.Strategies;
import deva.tools.json.JSONTools;


public class LearningAgent {

	DialoguePolicy policy;
	String policyFileName;
	Double alpha, discount, epsilon;
	String currentDS, lastDS;
	String currentActionId, lastActionId;
	Double totalReward;
	
	Logger logger = Logger.getLogger(LearningAgent.class.getName());
	
	public LearningAgent(Domains d, Strategies actionStrategies) {
		policy = null;
		
		policyFileName = "policy.txt";
		
		policy = new DialoguePolicy(d.getLearnerState(), actionStrategies);
		policy.storePolicy(policyFileName);
	
		alpha = 0.3;
		discount = 0.9;
		epsilon = 0.3;
		
		lastDS = null;
		lastActionId = null;
		currentDS = null;
		currentActionId = null;
		
		totalReward = 0.0;
	}
	
	
	
	public void setLastDS(String ls){
		lastDS = ls;
	}
	
	public void setCurrentDS(String ls){
		currentDS = ls;
	}
	
	public void setLastActionId(String id){
		lastActionId = id;
	}
	
	public void setCurrentActionid(String id){
		currentActionId = id;
	}
	
	

	public String selectActionToExecute(String ds) throws JSONException{
		//ds.display();
		
		JSONObject entry = policy.getPolicyEntry(ds);
		if (entry != null){
			//we found the entry in the policy that matches the current dialogue state.. lets find an action to execute
			//logger.info("Policy state:" + entry.getJSONArray("state"));
			//logger.info("Possible actions:" + entry.getJSONArray("possibleActions"));
			JSONArray possibleActions = entry.getJSONArray("possibleActions");
			JSONObject action = null;
			String actionId = null;
			if (possibleActions.length() == 1){
				action = possibleActions.getJSONObject(0);
			} 
			else if (possibleActions.length() > 1){
				int index;
				//if randomSelection is false, do epsilon selection
				if (Math.random() < epsilon){
					//select randomly to allow for exploration
					index = (int) Math.floor(Math.random() * possibleActions.length());
					//logger.info("LA: selecting a random action");
				} else {
					//getting action with highest q-value
					index = 0;
					Double maxQ = 0.0;
					for (int i2 = 0; i2 < possibleActions.length(); i2++){
						JSONObject action1 = possibleActions.getJSONObject(i2);
						double q = action1.getDouble("qValue");
						if (q > maxQ){
							maxQ = q;
							index = i2;
						}
					}
					//logger.info("LA: selecting the max Q action");
				}
				//logger.info("Action index:" + index);
				action = possibleActions.getJSONObject(index);
			}
			if (action != null){
				actionId = action.getString("actionId");
			}
			return actionId;
		}
		return null;
	}

	public void updateQValues(Double r) throws JSONException {
		if (lastDS != null && currentDS != null){
			Double alpha = 0.3;
			Double oldQ = policy.getQValue(lastDS,lastActionId);
			Double newQ = oldQ + alpha * (r + policy.getQValue(currentDS,currentActionId) - oldQ);
			//logger.info("Q update:\nLastState:" + lastDS + "\nCurrentState:" + currentDS
				//	+ "\nLastActionId:" + lastActionId + " CurrentActionId:" + currentActionId
				//	+ "\nOldQ:" + oldQ + " NewQ:" + newQ);
			policy.setQValue(lastDS, lastActionId, newQ);
		}
		totalReward += r; 
	}
	
	
	
	
	public void setEpsilon(Double e){
		epsilon = e;
	}
	
	public void reduceEpsilonByHalf(){
		epsilon = epsilon / 2;
		//logger.info("Reducing epsilon by half:" + epsilon);
	}



	public void savePolicy() {
		if (policy != null){
			policy.storePolicy(policyFileName);
		}
	}

	public void reset() {
		lastDS = null;
		lastActionId = null;
		currentDS = null;
		currentActionId = null;
		
		totalReward = 0.0;
		
	}
	
	public String laReport(){
		return "Total reward: " + totalReward;
	}
	
	public Double getTotalReward(){
		return totalReward;
	}
}

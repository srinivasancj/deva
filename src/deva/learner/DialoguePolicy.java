package deva.learner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import deva.im.Domains;
import deva.im.rule.Rule;
import deva.im.rule.Rules;
import deva.im.strategy.Strategies;
import deva.im.strategy.Strategy;
import deva.learner.LearnerState;





public class DialoguePolicy{
	
	JSONArray policy;
	

	Logger logger = Logger.getLogger(DialoguePolicy.class.getName());
	
	
	
	public DialoguePolicy(LearnerState d, Strategies s){
		PropertyConfigurator.configure("log4j.properties");
		policy = makeRandomPolicy(d,s);
		
		
	}
	
	/*
	 * Get policy entry that will match the given dialogue state
	 */
	public JSONObject getPolicyEntry(String ds) throws JSONException{
		//logger.info("Searching for entry in policy:" + ds);
		for (int i=0; i< policy.length();i++){
			JSONObject entry = policy.getJSONObject(i);
			String stateString = entry.getString("state");
			//logger.info("Does this match:" + stateString);
			
			JSONArray state2 = new JSONArray(ds);
			
			JSONArray state = new JSONArray(stateString);
			Boolean match = true;
			//match the states in the policy to see which one matches to current dialogue state
			for (int j=0; j<state.length();j++){
				JSONObject temp = state.getJSONObject(j);
				String name = temp.getString("name");
				Object value = temp.get("value");
				if (value.equals("*")){
					continue;
				}
				for (int k=0; k<state2.length();k++){
					JSONObject temp2 = state2.getJSONObject(k);
					String name2 = temp2.getString("name");
					Object value2 = temp2.get("value");
					if (name.equals(name2) && !value.equals(value2)){
						match = false;
						//logger.info("match=false");
						break;
					}
				}
				if (match == false){
					break;
				}
				
			}
			if (match){
				//logger.info("We found a match");
				return entry;
			}
			
		}
		return null;
	}

	
	
	public void storePolicy(){
		try{
			FileWriter fstream = new FileWriter("policy.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i=0; i<policy.length();i++){
				out.write(policy.get(i).toString() + "\n");
			}  
			out.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public double getQValue(String ds, String actionId) throws JSONException{
		JSONObject entry = getPolicyEntry(ds);
		if (entry != null){
			//we found the entry in the policy that matches the current dialogue state.. lets find an action to execute
			//logger.info("Policy state:" + entry.getJSONArray("state"));
			//logger.info("Possible actions:" + entry.getJSONArray("possibleActions"));
			JSONArray possibleActions = entry.getJSONArray("possibleActions");
			
			
			for (int i2 = 0; i2 < possibleActions.length(); i2++){
				JSONObject action1 = possibleActions.getJSONObject(i2);
				String aId = action1.getString("actionId");
				if (aId.equals(actionId)){
					return action1.getDouble("qValue");
				}
			}
		} else {
			//logger.info("Entry NOT found in policy!!");
		}
		return 0.0;
	}
	
	public void setQValue(String ds, String actionId, Double q) throws JSONException{
		JSONObject entry = getPolicyEntry(ds);
		if (entry != null){
			//we found the entry in the policy that matches the current dialogue state.. lets find an action to execute
			//logger.info("Policy state:" + entry.getJSONArray("state"));
			//logger.info("Possible actions:" + entry.getJSONArray("possibleActions"));
			JSONArray possibleActions = entry.getJSONArray("possibleActions");
			
			
			for (int i2 = 0; i2 < possibleActions.length(); i2++){
				JSONObject action1 = possibleActions.getJSONObject(i2);
				String aId = action1.getString("actionId");
				if (aId.equals(actionId)){
					action1.put("qValue", q);
				}
			}
			//logger.info("Updated q:" + entry.toString());
		}
	}
	
	
	
	

	public JSONArray makeRandomPolicy(LearnerState d, Strategies st){
		
		logger.info("Length of learner state:" + d.length());
		
		JSONArray p = new JSONArray();
		try {
			
			//enumerate dialogue states
			for (int i = 0; i < d.length(); i++){
				
				JSONObject j = d.getVariable(i);
				//logger.info(j.toString());
				//get the range for each variable
				ArrayList<String> range = null;
				if (j.has("range")){
					String ra = j.getString("range");
					range = new ArrayList<String>(Arrays.asList(ra.split((","))));
				} 
				else if (j.has("type") && j.getString("type").equals("boolean")){
					range = new ArrayList<String>();
					range.add("true");
					range.add("false");
				}
				else {
					range = new ArrayList<String>();
					range.add("*");
					range.add("null");
				}
				
				
				JSONArray temp = new JSONArray();
				//temp array will hold new states where we extend the variables by one more..
				if (p.length() == 0){ //if p is empty.. which originally it is..
					JSONObject m = null;
					for (int k = 0; k < range.size(); k++){
						m = new JSONObject();
						m.put(j.getString("name"), range.get(k));
						temp.put(m);
					}
				} else {
					for (int k2 = 0; k2 < p.length(); k2++){
						for (int k = 0; k < range.size(); k++){
							//need to copy the JSONObjects explicitly as below
							//because just assigning n to m does not work.. :(
							JSONObject n = p.getJSONObject(k2);
							JSONObject m = new JSONObject();
							Iterator<String> keys = n.keys();
							while(keys.hasNext()){
								String key1 = keys.next();
								m.put(key1, n.get(key1));
							}
							m.put(j.getString("name"), range.get(k));
							temp.put(m);
						}
					}
				}
				p = temp;
			}
			logger.info("States enumerated!" + p.length());
			
			policy = new JSONArray();
			
			for (int i = 0; i < p.length(); i++){
				JSONObject policyEntry = new JSONObject();
				
				//converting jsonobject into jsonarray in the same format as dialogue state
				JSONArray ds = new JSONArray();
				JSONObject dstemp = p.getJSONObject(i);
				//logger.info(i + ";" + dstemp.toString());
				Iterator<String> keys = dstemp.keys();
				while (keys.hasNext()){
					String ke = keys.next();
					JSONObject temp = new JSONObject();
					temp.put("name", ke);
					temp.put("value", dstemp.get(ke));
					ds.put(temp);
				}
				policyEntry.put("state", ds.toString());
				policyEntry.put("stateId", i);
				
				JSONArray possibleActions = new JSONArray();
				Strategy str = st.getStrategy("main");
				Rules aRules = str.getRules();
				
				if (aRules.length() > 0){
					for (int ri=0; ri < aRules.length(); ri++){
						Rule rule = aRules.getRule(ri);
						//logger.info("Checking... "  + rule.getComment() + ";" + rule.getId());
						LearnerState dstate = new LearnerState(ds);
						//logger.info("Against ... "  + dstate.displayString());
						Domains dom = new Domains();
						dom.setLearnerState(dstate);
						Boolean ruleSatisfied = rule.checkPrecondition(dom);
						if (ruleSatisfied){
							JSONObject action = new JSONObject();
							action.put("actionId", rule.getActionId());
							action.put("qValue", Math.random());
							possibleActions.put(action);
						}
					}
				}
				
				if (possibleActions.length() != 0){
					policyEntry.put("possibleActions", possibleActions);
					policy.put(policyEntry);
					logger.info(policyEntry.toString());
				}
			}
			
			//logger.info("Policy created for dialogue states: " + p.length());
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return policy;
	}

}
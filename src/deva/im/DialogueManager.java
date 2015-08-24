
package deva.im;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import deva.im.action.Action;
import deva.im.action.ActionAnd;
import deva.im.action.ActionSet;
import deva.im.rule.Rule;
import deva.im.rule.Rules;
import deva.im.strategy.Strategies;
import deva.learner.DialoguePolicy;
import deva.learner.LearnerState;
import deva.learner.LearningAgent;
import deva.script.ScriptReader;




/**
 * @author srinijanarthanam
 *
 */
public class DialogueManager {
	
	String name;
	
	Domains d;
	Rules uRules;
	Strategies actionStrategies;
	ActionSet actionSet;
	LearningAgent la;
	DialoguePolicy policy;
	
	JSONObject properties;
	ScriptReader sr;
	String scriptFile;
	File scriptsDir;
	int run;
	Boolean running;
	Boolean learningAgentActive;
	
	Logger logger = Logger.getLogger(DialogueManager.class.getName());
	
	public DialogueManager(String name, File dir, String filename){
		
		logger = Logger.getLogger(name);
		
		this.name = name;
		
		PropertyConfigurator.configure("log4j.properties");
		Date now = new Date();
		
		logger.info("NEW DM CREATED: " + name + ";" + (now).toString());
		
		sr = new ScriptReader(dir, filename);
		scriptFile = filename;
		scriptsDir = dir;
		
		d = new Domains();
		uRules = new Rules();
		actionStrategies = new Strategies();
		actionSet = new ActionSet();
		
		uRules.setRules(sr.getUpdateRules());
		actionStrategies.setActionStrategies(sr.getActionStrategies());
		actionSet.setActionSet(sr.getActionSet());
		d.d.setStateVariables(sr.getDialogueState());
		d.l.setStateVariables(sr.getLearnerState());
		d.iv.setInputVariables(sr.getInputVariables());
		d.iv.setDefaultInput(sr.getInputVariables());
		d.ov.setOutputVariables(sr.getOutputVariables());
		d.mt.setStateVariables(sr.getMetrics());
		
		properties = sr.getProperties();
		
		logger.info("all loaded");
		
		learningAgentActive = false;
		try {
			if (properties.getBoolean("dialoguePolicy")){
				learningAgentActive = true;
				la = new LearningAgent(d, actionStrategies);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		run = 0;
		running = false;
	}
	
	public String getName(){
		return name;
	}
	
	public Object getStateVariable(String name){
		try {
			Object r = d.d.getValue(name);
			if (r == null){
				r = d.l.getValue(name);
			}
			return r;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void close(){
		running = false;
		Date now = new Date();
		logger.info("DM " + name + " STOPPED: " + (now).toString());
		logger.info("");
		logger.info("");
		logger.info("");
	}
	
	public void setDStateVariable(String var, Object val){
		JSONObject t2 = new JSONObject();
		try {
			t2.put("name", var);
			t2.put("value", val);
			d.d.setValue(t2);
			//d.d.display();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void reset(){
		//sr = null;
		sr = new ScriptReader(scriptsDir, scriptFile);
		
		d.d.setStateVariables(sr.getDialogueState());
		d.l.setStateVariables(sr.getLearnerState());
		d.iv.setInputVariables(sr.getInputVariables());
		d.iv.setDefaultInput(sr.getInputVariables());
		d.ov.setOutputVariables(sr.getOutputVariables());
		d.mt.setStateVariables(sr.getMetrics());
		
		if (this.learningAgentActive){
			la.reset();
			la.setLastDS(d.getLearnerState().slotValueString());
			la.setLastActionId("null");
			
		}
		run = 0;
		//logger.info("Reset");
	}
	
	
	public JSONArray run(JSONObject in, Double lastReward){
		running = true;
		//logger.info(name + " run:" + run + ":" + System.currentTimeMillis());
		
		logger.info(name + " input :" + in.toString());
		JSONArray out = null;
		
		try {
			updateInputVariables(in);
			//d.getDState().display();
			//d.getLearnerState().display();
			executeUpdateRules();
			//d.iv.display();
			d.getDState().display();
			d.getLearnerState().display();
			//d.getMetrics().display();
			//state is updated now..
			d.ov.clear();
			
			
			out = selectAndExecuteAction(lastReward);
			//displayDialogueState();
			//action selected and executed now.
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		//ov.display();
		//logger.info(name + " output:" + out.toString());
		run++;
		running = false;
		return out;
	}

	public Boolean isRunning(){
		return running;
	}
	
	public int getRunCount(){
		return run;
	}
	
	public void displayDialogueState(){
		try {
			d.d.display();
			d.getLearnerState().display();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private JSONArray selectAndExecuteAction(Double lastReward) throws JSONException {
		JSONArray out = null;
		if (actionStrategies.isEmpty()){ return out; }
		
		//find the ActionAnd to execute
		ActionAnd currentActionToExecute = null;
		
		if (this.learningAgentActive){
			String actionId = la.selectActionToExecute(d.getLearnerState().slotValueString());
			Action a = actionSet.getAction(actionId);
			if (a != null){
				currentActionToExecute = new ActionAnd();
				currentActionToExecute.addAction(a);
			} else {
				//logger.info("NO ACTION FOUND!!!!");
				
			}
		}
		
		//as a backup..
		//we search using collect & select strategy on all rules, if the above process did not yield any actions
		if (currentActionToExecute == null) {
			//first find the main strategy to start with
			//actionStrategies.executeStrategy("first-fire", "main", d);
			
			//collect and select
			JSONArray actions = actionStrategies.selectActions("main", d);
			if (actions.length() > 0){
				currentActionToExecute = (ActionAnd) actions.get((int)(Math.random() * actions.length())); 
			}
			else if (actions.length() == 0){
				//logger.info("NO ACTIONS TO EXECUTE!!!");
			}
		}
			
		//execute the action
		if (currentActionToExecute != null){
			if (this.learningAgentActive){
				la.setCurrentDS(d.getLearnerState().slotValueString());
				la.setCurrentActionid(currentActionToExecute.getId());
				la.updateQValues(lastReward);
				la.setLastActionId(currentActionToExecute.getId());
				la.setLastDS(d.getLearnerState().slotValueString());
			}
			actionStrategies.executeAction(currentActionToExecute, d, actionSet);
		}
		
		
		d.ov.deleteUnusedOutputDA();
			
		out = d.ov.getAllDA();
		return out;
	}

	
	public void setFinalReward(double lastReward) throws JSONException {
		if (this.learningAgentActive){
			la.setCurrentDS(d.getLearnerState().slotValueString());
			la.setCurrentActionid("null");
			la.updateQValues(lastReward);
		}
		
	} 
	
	public void updateInputVariables(JSONObject in) throws JSONException{
		d.iv.refresh();
		//d.iv.display();
		if (in != null && in.length() > 0){
			
			@SuppressWarnings("unchecked")
			Iterator<String> it = in.keys();
			while (it.hasNext()){
				String tempKey = it.next();
				JSONObject temp = new JSONObject();
				temp.put("name", tempKey);
				temp.put("value", in.get(tempKey));
				d.iv.setValue(temp);
			}
		} else {
			//logger.info("NO input!");
			JSONObject temp = new JSONObject();
			temp.put("name", "fromModule");
			temp.put("value", "null");
			d.iv.setValue(temp);
		}
		//d.iv.display();
	}
	
	public void executeUpdateRules() throws JSONException {
		
		if (uRules.isEmpty()){ return; }
		
		for (int i = 0; i < uRules.length(); i++){
			Rule rule = uRules.getRule(i);
			//logger.info("Checking update rule: " + rule.getComment());
			Boolean preconditionTrue = rule.checkPrecondition(d);
			
			if (preconditionTrue){
				//logger.info("Executing update rule: " + rule.getComment());
				ActionAnd aa = rule.selectAction();
				
				//enumerate all the actions in aa
				for (int j = 0; j < aa.length(); j++){
					Action action = aa.getAction(j);
					
					//enumerate all actionunits (e.g. assignment) in
					for (int k = 0; k < action.length(); k++){
						JSONObject assignee, assigner;
						assignee = null; assigner = null;
						JSONObject temp = action.getJSONObject(k);
						if (temp.has("type") && temp.getString("type").equals("assignment")){
							if (temp.has("assigner")){
								assigner = temp.getJSONObject("assigner");
								assignee = temp.getJSONObject("assignee");
								//make the assignment
								JSONObject t2 = new JSONObject();
								t2.put("name", assignee.getString("var"));
								t2.put("value", this.getAssignmentValue(assigner, d));
								
								//which domain should be updated..??
								if (assignee.has("domain") && assignee.getString("domain").equals("dstate") && t2.has("value")){
									Boolean success = d.getDState().setValue(t2);
									if (!success){
										d.getLearnerState().setValue(t2);
									}
								}
								if (assignee.has("domain") && assignee.getString("domain").equals("output") && t2.has("value")){
									d.ov.setValue(t2);
								}
								if (assignee.has("domain") && assignee.getString("domain").equals("metric") && t2.has("value")){
									d.mt.setValue(t2);
								}
							}
						} 
					} //end of for k
				}//end of for j
				//logger.info("Executed update rule:" + rule.getComment());
			}
		}
		//d.d.display();
		//d.mt.display();
	}
	
	public Metric getMetrics(){
		return d.mt;
	}


	
	
	
	private Object getAssignmentValue(JSONObject assigner, Domains d) throws JSONException{
		//is the assigner a variable?
		if (assigner.has("var")){
			//is the assigner variable in the input domain??
			if (assigner.has("domain") && assigner.getString("domain").equals("input")){
				return d.iv.getValue(assigner.getString("var"));
			}
			
			//is the assigner variable in the dstate domain??
			if (assigner.has("domain") && assigner.getString("domain").equals("dstate")){
				Object r = d.d.getValue(assigner.getString("var"));
				if (r == null){
					r = d.getLearnerState().getValue(assigner.getString("var"));
				}
				return r;
			}
			
			//is the assigner variable in the metric domain??
			if (assigner.has("domain") && assigner.getString("domain").equals("metric")){
				return d.mt.getValue(assigner.getString("var"));
			}
		}
		//is the assigner a value
		else if (assigner.has("value")){
			return assigner.get("value");
		}
		//is the assigner a function
		else if (assigner.has("function")){
			if (assigner.getString("function").equals("randomBoolean")){
				return ((Math.random() > 0.5) ? "true":"false"); 
			}
			if (assigner.getString("function").equals("randomChoice")){
				String[] range = assigner.getString("range").split(",");
				return (range[(int) (Math.random() * range.length)]); 
			}
		}
		//math
		else if (assigner.has("add") || assigner.has("subtract")){
			if (assigner.has("add")){
				JSONArray arg = assigner.getJSONArray("add");
				Double sum = 0.0;
				for (int i = 0; i< arg.length(); i++){
					JSONObject jb = arg.getJSONObject(i);
					Object ob = getAssignmentValue(jb,d);
					Double o = (Double) ob;
					sum += o;
				}
				return sum;
			}
			if (assigner.has("subtract")){
				JSONArray arg = assigner.getJSONArray("subtract");
				JSONObject jb = arg.getJSONObject(0);
				Double sum = Double.valueOf((getAssignmentValue(jb,d)).toString());
				for (int i = 1; i< arg.length(); i++){
					jb = arg.getJSONObject(i);
					Double o = Double.valueOf((getAssignmentValue(jb,d)).toString());
					sum -= o;
				}
				return sum;
			}
		}
		else if (assigner.has("array")){
			JSONObject array = assigner.getJSONObject("array");
			//logger.info("Got the array def:" + array.toString());
			Integer in = null;
			if (array.has("index")){
				in = (int) Math.round((Double) getAssignmentValue(array.getJSONObject("index"),d));
				//logger.info("Got the index:" + in);
			}
			JSONArray a = null;
			//is the assigner variable in the dstate domain??
			if (array.has("domain") && array.getString("domain").equals("dstate")){
				a = (JSONArray) d.d.getValue(array.getString("var"));
				//logger.info("Got the array:" + a.toString());
			}
			return a.get(in);
		}
		return null;
	}


	public void displayLAReport(int episode) {
		if (this.learningAgentActive){
			//logger.info("LA report:" + la.laReport());
			logger.info(episode + ": " + la.getTotalReward());
		}
		
	}


	public void reduceLAEpsilonByHalf() {
		if (this.learningAgentActive){
			la.reduceEpsilonByHalf();
		}
	}


	public void saveLAPolicy() {
		if (this.learningAgentActive){
			la.savePolicy();
		}
		
	}


	
}
	
	
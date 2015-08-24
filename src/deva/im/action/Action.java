package deva.im.action;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



/**
 * @author srinijanarthanam
 *
 */
public class Action {
	String id;
	Double prob, qprob;
	JSONArray actionUnits;
	/* actionUnits can be assignment statements and goto statements */
	
	Logger logger = Logger.getLogger(Action.class.getName());
	
	public Action(){
		PropertyConfigurator.configure("log4j.properties");
		id = "null";
		prob = 1.0;
		qprob = 1.0;
		actionUnits = new JSONArray(); 
	}
	
	
	public Action(JSONArray a){
		id = "null";
		actionUnits = a;
	}
	
	public Action(String id, JSONArray a){
		this.id = id;
		actionUnits = a;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setProbability(double d){
		this.prob = d;
	}
	
	public void setQProbability(double d){
		this.qprob = d;
	}

	public void addActionUnit(JSONObject au){
		actionUnits.put(au);
	}
	
	public JSONArray getActionUnits() {
		return actionUnits;
	}

	public int length() {
		return actionUnits.length();
	}

	public JSONObject getJSONObject(int k) throws JSONException {
		return actionUnits.getJSONObject(k);
	}
	
	public String toString(){
		return "p=" + prob + "," + "q=" + qprob + "," + actionUnits.toString();
	}

	public double getProbability() {
		return this.prob;
	}
	
	public double getQProbability() {
		return this.qprob;
	}
	
}
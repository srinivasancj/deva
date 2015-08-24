package deva.im;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import deva.State;



/**
 * DialogueState.java
 * 
 * @author srinijanarthanam
 * 
 */

public class DialogueState extends State {
	
	Logger logger = Logger.getLogger(DialogueState.class.getName());
	
	
	DialogueState(){
		PropertyConfigurator.configure("log4j.properties");
		svar = new JSONArray();
	}
	
	public DialogueState(JSONArray ds) {
		svar = ds;
	}

	public void display() throws JSONException{
		logger.info("DState:" + displayString());
	}
	
	public Integer length(){
		return svar.length();
	}
	
	public JSONObject getVariable(int index){
		if (index < svar.length()){
			try {
				return svar.getJSONObject(index);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
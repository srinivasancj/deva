package deva.learner;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import deva.State;
import deva.im.DialogueState;


public class LearnerState extends State {

	Logger logger = Logger.getLogger(LearnerState.class.getName());
	
	public LearnerState() {
		PropertyConfigurator.configure("log4j.properties");
		svar = new JSONArray();
	}

	
	public LearnerState(JSONArray ds) {
		PropertyConfigurator.configure("log4j.properties");
		svar = ds;
	}

	public void display() throws JSONException{
		logger.info("LearnerState:" + displayString());
	}

	public String toString(){
		return svar.toString();
	}


	public String slotValueString() {
		JSONArray sv = new JSONArray();
		for (int i=0; i<svar.length();i++){
			JSONObject k;
			try {
				k = svar.getJSONObject(i);
				
				String name = k.getString("name");
				Object value = k.get("value");
				JSONObject n = new JSONObject();
				n.put("name", name);
				n.put("value", value);
				sv.put(n);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return sv.toString();
	}
}

package deva.im;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Metric.java
 * 
 * @author srinijanarthanam
 * 
 */

public class Metric{
	
	JSONArray svar;

	Logger logger = Logger.getLogger(Metric.class.getName());
	
	
	Metric(){
		PropertyConfigurator.configure("log4j.properties");
		svar = new JSONArray();
	}
	
	public Metric(JSONArray ds) {
		svar = ds;
	}

	public void setStateVariables(JSONArray v){
		svar = v;
	}
	
	public Integer length(){
		return svar.length();
	}
	
	public void setValue(JSONObject k) throws JSONException{
		String name = k.getString("name");
		Object value = k.get("value");
		
		for (int i=0; i < svar.length(); i++){
			JSONObject temp = svar.getJSONObject(i);
			if (temp.getString("name").equals(name)){
				temp.put("value", value);
			}
		}
	}
	
	public Object getValue(String name) throws JSONException{
		Object out = null;
		for (int i=0; i < svar.length(); i++){
			JSONObject temp = svar.getJSONObject(i);
			if (temp.getString("name").equals(name)){
				out = temp.get("value");
			}
		}
		return out;
	}
	
	public void display(){
		logger.info("Metric:" + svar.toString());
	}
	
	public String getVarType(String name) throws JSONException{
		String type = null;
		for (int i=0; i < svar.length(); i++){
			JSONObject temp = svar.getJSONObject(i);
			if (temp.getString("name").equals(name)){
				type = temp.getString("type");
			}
		}
		return type;
	}

	public JSONObject getVariable(int i) throws JSONException {
		if (i < length()){
			return svar.getJSONObject(i);
		}
		return null;
	}
}
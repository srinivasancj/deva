package deva.im;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import deva.tools.json.JSONTools;




public class Input {
	JSONArray invar;
	JSONArray defaultInput;

	Logger logger = Logger.getLogger(Input.class.getName());
	
	
	public Input(){
		PropertyConfigurator.configure("log4j.properties");
		invar = new JSONArray();
		defaultInput = new JSONArray();
	}
	
	public void refresh(){
		try {
			invar = JSONTools.copyJSONArray(defaultInput);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setInputVariables(JSONArray v){
		invar = v;
	}
	
	public void setDefaultInput(JSONArray v) {
		
			try {
				defaultInput = JSONTools.copyJSONArray(v);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void setValue(JSONObject k) throws JSONException{
		String name = k.getString("name");
		Object value = k.get("value");
		
		for (int i=0; i < invar.length(); i++){
			JSONObject temp = invar.getJSONObject(i);
			if (temp.getString("name").equals(name)){
				temp.put("value", value);
			}
		}
	}
	
	public void display(){
		logger.info("Input:" + invar.toString());
	}
	
	public Object getValue(String name) throws JSONException{
		Object out = null;
		for (int i=0; i < invar.length(); i++){
			JSONObject temp = invar.getJSONObject(i);
			if (temp.getString("name").equals(name)){
				out = temp.get("value");
			}
		}
		return out;
	}
}
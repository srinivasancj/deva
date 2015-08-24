package deva;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import deva.tools.json.JSONTools;


public class State {

	protected JSONArray svar;
	
	public State() {
		// TODO Auto-generated constructor stub
	}

	public void setStateVariables(JSONArray v){
		svar = v;
	}
	
	public Integer length(){
		return svar.length();
	}
	
	public Boolean setValue(JSONObject k) throws JSONException{
		Boolean success = false;
		String name = k.getString("name");
		Object value = k.get("value");
		for (int i=0; i < svar.length(); i++){
			JSONObject temp = svar.getJSONObject(i);
			if (temp.getString("name").equals(name)){
				temp.put("value", value);
				success = true;
				//logger.info("Setting:" + name + "=" + value.toString());
			}
		}
		return success;
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
	

	public String displayString() throws JSONException{
		String s = "{";
		if (svar.length() > 0){
			for (int i=0; i < svar.length() - 1; i++){
				JSONObject temp = svar.getJSONObject(i);
				s += temp.getString("name") + "=" + String.valueOf(temp.get("value")) + ",";
			}
			JSONObject temp = svar.getJSONObject(svar.length()-1);
			s = s + temp.getString("name") + "=" + String.valueOf(temp.get("value")) + "}";
		} else {
			s +="}";
		}
		return s;
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
	
	public JSONArray getCopy(){
		try {
			return JSONTools.copyJSONArray(svar);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}

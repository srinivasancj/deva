package deva.im;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Output {
	JSONArray outvar;
	Integer index;

	Logger logger = Logger.getLogger(Output.class.getName());
	
	
	public Output(){
		PropertyConfigurator.configure("log4j.properties");
		outvar = new JSONArray();
		index = -1;
	}
	
	public void setOutputVariables(JSONArray v){
		outvar = v;
	}
	
	public void display(){
		logger.info("Output:" + outvar.toString());
	}
	
	public void createNewOutputDA() throws JSONException{
		JSONArray array = new JSONArray();
		index++;
		outvar.put(index,array);
	}
	
	
	public void clear() throws JSONException {
		outvar = new JSONArray();
		index = -1;
	}

	public int length() throws JSONException{
		JSONArray array = outvar.getJSONArray(index);
		return array.length();
	}
	
	public void setValue(JSONObject k) throws JSONException {
		String name = k.getString("name");
		Object value = k.get("value");
		JSONArray array = outvar.getJSONArray(index);
		for (int i=0; i < array.length(); i++){
			JSONObject temp =array.getJSONObject(i);
			if (temp.getString("name").equals(name)){
				temp.put("value", value);
				return;
			}
		}
		
		//if you cant find it in the array.. make one..
		
		array.put(k);
	}
	
	public Object getValue(String name) throws JSONException{
		Object out = null;
		for (int i=0; i < outvar.length(); i++){
			JSONObject temp = outvar.getJSONObject(i);
			if (temp.getString("name").equals(name)){
				out = temp.get("value");
			}
		}
		return out;
	}
	
	public JSONObject getAllValues(JSONArray array) throws JSONException{
		JSONObject outj = new JSONObject();
		for (int i=0; i < array.length(); i++){
			JSONObject temp = array.getJSONObject(i);
			outj.put(temp.getString("name"),temp.get("value"));
		}
		return outj;
	}
	
	public JSONArray getAllDA() throws JSONException {
		JSONArray outArray = new JSONArray();
		for (int i=0; i < outvar.length(); i++){
			JSONObject obj = getAllValues(outvar.getJSONArray(i));
			if (obj.length() > 0){
				outArray.put(obj);
			}
		}
		return outArray;
	}

	public void deleteUnusedOutputDA() throws JSONException {
		if (outvar.length() > 0){
			for (int i=0; i < outvar.length(); i++){
				try {
					if (outvar.get(i) != null && outvar.getJSONArray(i).length() == 0){
						//logger.info("Deleting empty array:" + outvar.getJSONArray(i).toString());
						outvar.remove(i);
					}
				} catch (JSONException j){
					outvar.remove(i);
				}
			}
		}
	}
}


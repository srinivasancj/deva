package deva.im.rule;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;




public class Rules {
	JSONArray rules;

	Logger logger = Logger.getLogger(Rules.class.getName());
	
	
	public Rules(){
		PropertyConfigurator.configure("log4j.properties");
		rules = null;
	}
	
	public Rules(JSONArray actionRules) {
		this.rules = actionRules;
	}

	public void setRules(JSONArray v){
		rules = v;
	}
	
	public boolean isEmpty(){
		if (rules == null || rules.length() == 0){
			return true;
		}
		return false;
	}

	public int length() {
		return rules.length();
	}

	public Rule getRule(int i) throws JSONException {
		if (rules.length() > i){
			return (Rule) rules.get(i);
		}
		return null;
	}
	
	public void display() throws JSONException{
		for (int i=0; i<rules.length();i++){
			Rule r = (Rule) rules.get(i);
			r.display();
		}
	}

	public Rule getRule(String id) throws JSONException {
		for (int i=0; i<rules.length();i++){
			Rule r = (Rule) rules.get(i);
			if (r.id.equals(id)){
				return r;
			}
		}
		return null;
	}
	
}
package deva.im.strategy;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;

import deva.im.rule.Rules;


public class Strategy {
	String id;
	Rules rules;

	Logger logger = Logger.getLogger(Strategy.class.getName());
	
	
	public Strategy(){
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getId(){
		return this.id;
	}

	public void put(String name, String value) {
		if (name.equals("id")){
			this.id = value;
		}
		
	}

	public void put(String name, Rules actionRules) {
		if (name.equals("rules")){
			this.rules = actionRules;
		}
		
	}
	
	public Rules getRules(){
		return this.rules;
	}

	public void display() {
		logger.info("STRATEGY: " + id);
		
		try {
			rules.display();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	
	
}

package deva.im.action;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;

public class ActionAnd {
	String id;
	JSONArray actions;
	Double prob;
	
	Logger logger = Logger.getLogger(ActionAnd.class.getName());
	
	
	public ActionAnd(){
		PropertyConfigurator.configure("log4j.properties");
		id = "null";
		actions = new JSONArray();
		prob = 1.0;
		
	}
	
	public void setProbability(double d){
		this.prob = d;
	}
	
	public double getProbability(){
		return this.prob;
	}	
	
	
	
	public void addAction(Action action) {
		if (action != null){
			actions.put(action);
			id = action.getId();
		}
	}
	
	public Action getAction(int i) throws JSONException{
		return (Action) actions.get(i);
	}
	
	public String toString(){
		return "p=" + prob + "," + actions.toString();
	}
	
	public int length(){
		return actions.length();
	}

	public String getId() {
		return id;
	}
}

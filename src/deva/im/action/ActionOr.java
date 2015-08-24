package deva.im.action;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;

public class ActionOr {
	JSONArray actionAnds;
	String id;
	
	Logger logger = Logger.getLogger(ActionOr.class.getName());
	
	
	public ActionOr(){
		PropertyConfigurator.configure("log4j.properties");
		actionAnds = new JSONArray();
		id = "null";
	}
	
	public void addActionAnd(ActionAnd aa) {
		actionAnds.put(aa);
		id = aa.getId();
	}
	
	public String toString(){
		return actionAnds.toString();
	}
	
	public Integer length(){
		return actionAnds.length();
	}
	
	public JSONArray getActionAnds(){
		return actionAnds;
	}

	public String getId() {
		return id;
	}
}

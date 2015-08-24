package deva.im.action;

import org.json.JSONArray;
import org.json.JSONException;

public class ActionSet {

	JSONArray actionSet;
	
	public ActionSet() {
		actionSet = new JSONArray();
	}
	
	public void setActionSet(JSONArray as){
		this.actionSet = as;
	}

	public Action getAction(String id) {
		for (int i=0; i < actionSet.length(); i++){
			try {
				Action a = (Action) actionSet.get(i);
				if (a.getId().equals(id)){
					return a;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
			
		}
		return null;
	}

	

}

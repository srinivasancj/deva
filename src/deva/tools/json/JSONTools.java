
package deva.tools.json;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * JSONTools
 */

public class JSONTools {
	
	public static void main(String[] arg){
		JSONObject a  = new JSONObject();
		JSONObject b  = new JSONObject();
		try {
			a.put("cf", "search");
			b.put("cf", "cal2");
			System.out.println(mergeJSONObjects(a,b).toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static JSONObject mergeJSONObjects(JSONObject a, JSONObject b) throws JSONException{
		@SuppressWarnings("unchecked")
		Iterator<String> i = b.keys();
		while (i.hasNext()){
			String key = i.next();
			a.put(key, b.get(key));
		}
		
		//Logger.log(20, "JSONTools", "Warning..conflicting keys");
		return a;
	}
	
	public static JSONArray appendJSONArrays(JSONArray a, JSONArray b) throws JSONException{
		if (a.length() > 0 && b.length() > 0){
			int s = a.length();
			for (int i = 0; i< b.length(); i++){
				a.put(s+i, b.get(i));
			}
			return a;
		} 
		else if (a.length() == 0){
			return b;
		}
		else {
			return a;
		}
	}
	
	public static JSONArray copyJSONArray(JSONArray j) throws JSONException{
		JSONArray k = new JSONArray();
		for(int i=0;i<j.length();i++){
			if (j.get(i) instanceof JSONObject){
				JSONObject nj = copyJSONObject(j.getJSONObject(i));
				k.put(nj);
			} else {
				k.put(j.get(i));
			}
		}
		return k;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject copyJSONObject(JSONObject j) throws JSONException{
		JSONObject k = new JSONObject();
		Iterator<String> i = j.keys();
		while (i.hasNext()){
			String tk = i.next();
			k.put(tk, j.get(tk));
		}
		return k;
	}
}
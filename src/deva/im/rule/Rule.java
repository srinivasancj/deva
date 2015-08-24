package deva.im.rule;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import deva.im.Domains;
import deva.im.action.Action;
import deva.im.action.ActionAnd;
import deva.im.action.ActionOr;
import deva.tools.json.JSONTools;



public class Rule {
	
	JSONObject precondition;
	ActionOr actionOr;
	String id;
	String comment;
	String ruleType;
	
	Logger logger = Logger.getLogger(Rule.class.getName());
	
	public Rule(){
		
		PropertyConfigurator.configure("log4j.properties");
		
		precondition = null;
		actionOr = null;
	}
	
	public void setPrecondition(JSONObject p){
		precondition = p;
	}
	
	/* when the script has an actionOr */
	public void setActionOr(ActionOr ao){
		actionOr = ao;
	}
	
	/* when the script has only an actionAnd element */
	public void setActionAnd(ActionAnd aa){
		actionOr = new ActionOr();
		actionOr.addActionAnd(aa);
	}
	
	/* when the script has only an action element */
	public void setAction(Action a){
		actionOr = new ActionOr();
		ActionAnd actionAnd = new ActionAnd();
		actionAnd.addAction(a);
		actionOr.addActionAnd(actionAnd);
	}
	
	public void setRuleType(String type){
		ruleType = type;
	}
	
	public void setAttribute(String k, String v){
		if (k.equals("id")){
			id = v;
		}
		if (k.equals("comment")){
			comment = v;
		}
	}

	public JSONObject getPrecondition() {
		return precondition;
	}
	
	public String getActionId(){
		return actionOr.getId();
	}
	
	public void display(){
		logger.info("Rule:" + id + "," + comment + ",Pre:" + precondition.toString() + ",Eff:" + actionOr.toString());
	}

	public Boolean checkPrecondition(Domains d) throws JSONException {
		if (precondition.length() == 0){
			return true;
		}
		
		if (precondition.has("element")){
			return checkPreconditionElement(precondition.getJSONObject("element"), d);
		}
		else if (precondition.has("and")){
			return checkPreconditionAnd(precondition.getJSONArray("and"), d);
		}
		else if (precondition.has("or")){
			return checkPreconditionOr(precondition.getJSONArray("or"), d);
		}
		
		return false;
	}

	private Boolean checkPreconditionAnd(JSONArray andEl, Domains d) throws JSONException {
		if (andEl.length() == 0){
			return true;
		}
		Boolean op1True = false;
		Boolean op2True = false;
		
		if (andEl.length() > 0){
			JSONObject op1 = andEl.getJSONObject(0);
			if (op1.has("element")){
				op1True = checkPreconditionElement(op1.getJSONObject("element"), d);
			} else if (op1.has("and")){
				op1True = checkPreconditionAnd(op1.getJSONArray("and"), d);
			} else if (op1.has("or")){
				op1True = checkPreconditionOr(op1.getJSONArray("or"), d);
			}
		}
		
		if (andEl.length() == 1){
			op2True = true;
		}
		else if (andEl.length() > 2){
			JSONArray newAndEl = new JSONArray();
			newAndEl = JSONTools.copyJSONArray(andEl);
			newAndEl.remove(0);
			op2True = checkPreconditionAnd(newAndEl, d);
		}
		else if (andEl.length() == 2){
			JSONObject op2 = andEl.getJSONObject(1);
			if (op2.has("element")){
				op2True = checkPreconditionElement(op2.getJSONObject("element"), d);
			} else if (op2.has("and")){
				op2True = checkPreconditionAnd(op2.getJSONArray("and"), d);
			} else if (op2.has("or")){
				op2True = checkPreconditionOr(op2.getJSONArray("or"), d);
			}
		} 
		
		
		return (op1True && op2True);
	}

	private Boolean checkPreconditionOr(JSONArray orEl, Domains d) throws JSONException {
		
		if (orEl.length() == 0){
			return true;
		}
		Boolean op1True = false;
		Boolean op2True = false;
		
		if (orEl.length() > 0){
			JSONObject op1 = orEl.getJSONObject(0);
			if (op1.has("element")){
				op1True = checkPreconditionElement(op1.getJSONObject("element"), d);
			} else if (op1.has("and")){
				op1True = checkPreconditionAnd(op1.getJSONArray("and"), d);
			} else if (op1.has("or")){
				op1True = checkPreconditionOr(op1.getJSONArray("or"), d);
			}
		}
		
		if (orEl.length() == 1){
			op2True = true;
		}
		else if (orEl.length() > 2){
			JSONArray newOrEl = new JSONArray();
			newOrEl = JSONTools.copyJSONArray(orEl);
			newOrEl.remove(0);
			op2True = checkPreconditionOr(newOrEl, d);
		}
		else if (orEl.length() == 2){
			JSONObject op2 = orEl.getJSONObject(1);
			
			if (op2.has("element")){
				op2True = checkPreconditionElement(op2.getJSONObject("element"), d);
			} else if (op2.has("and")){
				op2True = checkPreconditionAnd(op2.getJSONArray("and"), d);
			} else if (op2.has("or")){
				op2True = checkPreconditionOr(op2.getJSONArray("or"), d);
			}
		}
		return (op1True || op2True);
	}

	private Boolean checkPreconditionElement(JSONObject element, Domains d) throws JSONException {
		Object op1 = getOperand(element.getJSONObject("op1"), d);
		Object op2 = getOperand(element.getJSONObject("op2"), d);
		if (op1 == null || op2 == null){
			return false;
		}
		
		String lo = element.getString("lo");
		if (lo.equals("equals") && op1.equals(op2)){
			//logger.info(op1.toString() + "==" + op2.toString());
			return true;
		}
		if (lo.equals("gt") && ((Double) op1 > (Double) op2)){
			return true;
		}
		if (lo.equals("lt") && ((Double) op1 < (Double) op2)){
			return true;
		}
		if (lo.equals("ge") && ((Double) op1 >= (Double) op2)){
			return true;
		}
		if (lo.equals("le") && ((Double) op1 <= (Double) op2)){
			return true;
		}
		if (lo.equals("not-equals") && !op1.equals(op2)){
			return true;
		}
		
		//logger.info("Failed:" + op1.toString() + " " + lo + " " + op2.toString());
		return false;
	}

	private Object getOperand(JSONObject op, Domains d) throws JSONException {
		Object o = null;
		if (op.has("var")){
			if (op.has("domain") && op.getString("domain").equals("dstate")){
				o = d.getDState().getValue(op.getString("var"));
				if (o == null){
					o = d.getLearnerState().getValue(op.getString("var"));
				}
			}
			if (op.has("domain") && op.getString("domain").equals("input")){
				o = d.getInput().getValue(op.getString("var"));
			}
		}
		else if (op.has("value")){
			if (op.has("type") && op.getString("type").equals("number")){
				o = Double.valueOf(op.getString("value"));
			} else {
				o = op.get("value");
			}
		}
		
		return o;
	}
	
	
	/* Select an actionAnd unit from the list randomly */
	public ActionAnd selectAction() throws JSONException{
		JSONArray actionAnds = actionOr.getActionAnds();
		if (actionAnds.length() == 1){
			//logger.info("Choosing 0th actionAnd");
			return (ActionAnd) actionAnds.get(0);
		} 
		else if (actionAnds.length() > 1){
			//int k = (int)(Math.random() * actionAnds.length()); 
			double r = Math.random();
			double p1 = 0.0;
			for (int i=0; i < actionAnds.length(); i++){
				ActionAnd aa = (ActionAnd) actionAnds.get(i);
				p1 = p1 + aa.getProbability();
				logger.info("r=" + r + " p1=" + p1);
				if (r < p1){
					logger.info("Choosing " + (i+1) + "th actionAnd. Possible choices:" + actionAnds.length());
					return aa;
				}
			}
			return (ActionAnd) actionAnds.get(actionAnds.length() - 1);
		}
		logger.info("NO ACTION FOUND!!!");
		return null;
	}

	public String getId() {
		return id;
	}

	public String getComment() {
		return comment;
	}
	
	
	
	
	
}

package deva.script;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import deva.im.DialogueManager;
import deva.im.action.Action;
import deva.im.action.ActionAnd;
import deva.im.action.ActionOr;
import deva.im.rule.Rule;
import deva.im.rule.Rules;
import deva.im.strategy.Strategy;





/**
 * ScriptReader.java
 * 
 * Reads the dialogue script in XML
 * @author srinijanarthanam
 *
 */
public class ScriptReader {
	JSONArray dstate, lstate, metric;
	JSONArray inputVariables, outputVariables, updateRules, actionStrategies;
	JSONArray actionSet;
	JSONObject properties;
	
	File scriptsDir;
	
	Logger logger = Logger.getLogger(ScriptReader.class.getName());
	
	public static void main(String[] arg) throws JSONException{
		
	}
	
	public ScriptReader(File dir, String fileName){
		PropertyConfigurator.configure("log4j.properties");
		
		scriptsDir = dir;
		inputVariables  = null;
		outputVariables = null;
		dstate = null;
		lstate = null;
		updateRules = null;
		actionStrategies = null;
		actionSet = null;
		metric = null;
		
		File file = new File (dir, fileName);
		try {
			URL scriptURL = file.toURI().toURL();
			Document scriptDoc = parse(scriptURL);
			getDialogueSpecification(scriptDoc);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		//logger.info("Script loaded!");
	}
	
	private Document parse(URL url) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(url);
        return document;
	}
	
	private Element getStrategyFromDocument(Document scriptDoc) throws DocumentException, JSONException {
		Element root = scriptDoc.getRootElement();
		return root;
	}
	
	private void getDialogueSpecification(Document scriptDoc) throws DocumentException, JSONException {
		Element root = scriptDoc.getRootElement();
		Boolean dialogueStateFound, inputFound, outputFound, stateUpdateRulesFound, actionSelectionRulesFound, actionSetFound, metricFound;
		dialogueStateFound = inputFound = outputFound = stateUpdateRulesFound = actionSelectionRulesFound = metricFound = actionSetFound = false;
		inputVariables  = new JSONArray();
		outputVariables = new JSONArray(); 
		dstate = new JSONArray();
		lstate = new JSONArray();
		
		updateRules = new JSONArray();
		actionStrategies = new JSONArray();
		actionSet = new JSONArray();
		properties = new JSONObject();
		metric = new JSONArray();
		
		properties.put("dialoguePolicy", false);
		
		for (Iterator<Element> i = root.elementIterator(); i.hasNext(); ) {
            Element element = (Element) i.next();
            if (element.getName().equals("dialoguePolicy")){
            	properties.put("dialoguePolicy", true);
            } 
            if (element.getName().equals("dialogueState")){
            	dialogueStateFound = true;
            	for (Iterator<Element> j = element.elementIterator(); j.hasNext();){
            		Element e = (Element) j.next();
            		JSONObject v = getVariable(e);
            		if (v.has("ls") && v.getString("ls").equals("true")){
            			lstate.put(v);
            		} else {
            			dstate.put(v);
            		}
            	}
            	//logger.info(dstate.toString());
            	//logger.info(lstate.toString());
            }
            if (element.getName().equals("input")){
            	inputFound = true;
            	if (element.attributeCount() > 0){
            		
            	}
            	for (Iterator<Element> j = element.elementIterator(); j.hasNext();){
            		Element e = (Element) j.next();
            		inputVariables.put(getVariable(e));
            	}
            	//logger.info(inputVariables.toString());
            }
            if (element.getName().equals("metric")){
            	metricFound = true;
            	if (element.attributeCount() > 0){
            		
            	}
            	for (Iterator<Element> j = element.elementIterator(); j.hasNext();){
            		Element e = (Element) j.next();
            		metric.put(getVariable(e));
            	}
            	//logger.info(metric.toString());
            }
            if (element.getName().equals("stateUpdateRules")){
            	stateUpdateRulesFound = true;
            	for (Iterator<Element> j = element.elementIterator(); j.hasNext();){
            		Element e = (Element) j.next();
            		Rule temp = getRule(e);
            		temp.setRuleType("update");
            		updateRules.put(temp);
            	}
            }
            if (element.getName().equals("actionSelectionRules")){
            	actionSelectionRulesFound = true;
            	for (Iterator<Element> j = element.elementIterator(); j.hasNext();){
            		Element e = (Element) j.next();
            		Strategy st = getStrategy(e);
            		//st.display();
            		actionStrategies.put(st);
            	}
            }
            if (element.getName().equals("actionSet")){
            	actionSetFound = true;
            	for (Iterator<Element> j = element.elementIterator(); j.hasNext();){
            		Element e = (Element) j.next();
            		if (e.getName().equals("action")){
    					actionSet.put(getRuleAction(e));
    				}
            		//at the moment..actionAnd and actionOr elements are ignored..
            		//need to handle them later.
            	}
            }
        }
	}
	
	
	private Strategy getStrategy(Element v){
		Strategy strategy = new Strategy();
		if (v.getName().equals("strategy")){
			String sourceFile = "null";
			for (Iterator<Attribute> j = v.attributeIterator(); j.hasNext();){
				Attribute e = (Attribute) j.next();
				if (e.getName().equals("source")){
					sourceFile = e.getValue();
				}
				//logger.info(e.getName() + "," + e.getValue());
				strategy.put(e.getName(), e.getValue());
         	}
			if (!sourceFile.equals("null")){
				//logger.info("Try to load the script from:" + sourceFile);
				try {
					File file = new File (scriptsDir, sourceFile);
					URL scriptURL = file.toURI().toURL();
					Document scriptDoc = parse(scriptURL);
					Element st = getStrategyFromDocument(scriptDoc);
					strategy.put("rules", new Rules(getActionRulesFromStrategyElement(st)));
				} catch (MalformedURLException ex) {
					ex.printStackTrace();
				} catch (DocumentException ex) {
					ex.printStackTrace();
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
			} else {
				strategy.put("rules", new Rules(getActionRulesFromStrategyElement(v)));
			}
		}
		return strategy;
	}
	
	private JSONArray getActionRulesFromStrategyElement(Element v){
		JSONArray actionRules = new JSONArray();
		for (Iterator<Element> j = v.elementIterator(); j.hasNext();){
			Element e = (Element) j.next();
			if (e.getName().equals("rule")){
				Rule temp = getRule(e);
				temp.setRuleType("action");
				actionRules.put(temp);
			}
		}
		return actionRules;
	}
	
	private Rule getRule(Element v){
		Rule rule = new Rule();
		/*
		 * <rule number="1" comment="">
			<precondition>
				<and>
					<equals>
						<var name="affect"/>
						<val value="sad"/>
					</equals>
					<equals>
						<var name="responseCorrectness"/>
						<val value="incorrect"/>
					</equals>
				</and>
			</precondition>
			<action>
				<assign>
					<var name="communicativeFunction"/>
					<val value="inform"/>
				</assign>
			</action>
		</rule>
		 */
		if (v.getName().equals("rule")){
			for (Iterator<Attribute> j = v.attributeIterator(); j.hasNext();){
				Attribute e = (Attribute) j.next();
				rule.setAttribute(e.getName(),e.getValue());
         	}
			for (Iterator<Element> j = v.elementIterator(); j.hasNext();){
				Element e = (Element) j.next();
				if (e.getName().equals("precondition")){
					rule.setPrecondition(getRulePrecondition(e));
				}
				if (e.getName().equals("actionAnd")){
					rule.setActionAnd(getRuleActionAnd(e));
				}
				if (e.getName().equals("actionOr")){
					rule.setActionOr(getRuleActionOr(e));
				}
				if (e.getName().equals("action")){
					rule.setAction(getRuleAction(e));
				}
			}
		}
		//rule.display();
		return rule;
	}
	
	private ActionOr getRuleActionOr(Element ef) {
		ActionOr actionOr = new ActionOr();
		for (Iterator<Element> j = ef.elementIterator(); j.hasNext();){
			//each element will be an action element here.
			Element e = (Element) j.next();
			if (e.getName().equals("actionAnd")){
				ActionAnd actionAnd = getRuleActionAnd(e);
				actionOr.addActionAnd(actionAnd);
			}
			/* if there is no actionAnd but there is just an action element */
			else if (e.getName().equals("action")){
				ActionAnd actionAnd = new ActionAnd();
				Action action = getRuleAction(e);
				actionAnd.addAction(action);
				actionAnd.setProbability(action.getProbability());
				actionOr.addActionAnd(actionAnd);
			}
		}
		return actionOr;
	}

	private ActionAnd getRuleActionAnd(Element ef) {
		ActionAnd actionAnd = new ActionAnd();
		for (Iterator<Attribute> j = ef.attributeIterator(); j.hasNext();){
			Attribute a = (Attribute) j.next();
			if (a.getName().equals("p")){
				actionAnd.setProbability(Double.valueOf(a.getValue()));
			}
		}
		for (Iterator<Element> j = ef.elementIterator(); j.hasNext();){
			//each element will be an action element here.
			Element e = (Element) j.next();
			if (e.getName().equals("action")){
				Action action = getRuleAction(e);
				actionAnd.addAction(action);
			}
		}
		return actionAnd;
	}


	
	private Action getRuleAction(Element ef) {
		Action action = new Action();
		for (Iterator<Attribute> j = ef.attributeIterator(); j.hasNext();){
			Attribute a = (Attribute) j.next();
			if (a.getName().equals("p")){
				action.setProbability(Double.valueOf(a.getValue()));
			}
			if (a.getName().equals("id")){
				action.setId(a.getValue());
			}
		}
		for (Iterator<Element> j = ef.elementIterator(); j.hasNext();){
			Element e = (Element) j.next();
			if (e.getName().equals("assign")){
				action.addActionUnit(getAssignment(e));
			}
			else if (e.getName().equals("gotoStrategy")){
				action.addActionUnit(getGotoStrategy(e));
			}
		}
		return action;
	}
	
	private JSONObject getGotoStrategy(Element e) {
		JSONObject ef1 = new JSONObject();
		try {
			ef1.put("type", "gotoStrategy");
			for (Iterator<Attribute> j = e.attributeIterator(); j.hasNext();){
				Attribute a = (Attribute) j.next();
				if (a.getName().equals("id")){
					ef1.put("id", a.getValue());
				}
			
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return ef1;
	}
	
	private JSONObject getAssignment(Element e) {
		JSONObject ef1 = new JSONObject();
		try {
			ef1.put("type", "assignment");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String domain = "null";
		String varName = "null";
		
		for (Iterator<Element> k = e.elementIterator(); k.hasNext();){
			Element e2 = (Element) k.next();
			JSONObject k1 = new JSONObject();
			if (e2.hasContent()){
				k1 = getMath(e2);
			} else {
				//<assignee var="correctResponses" domain="dstate" />
				//Putting the attributes (var, domain) of the element into JSONObject.
				for (Iterator<Attribute> j = e2.attributeIterator(); j.hasNext();){
					Attribute a = (Attribute) j.next();
					try {
						if (!domain.equals("null") && a.getName().equals("value") && !varName.equals("null")){
							String varType = getVariableType(varName,domain);
							if (varType.equals("number")){
								k1.put(a.getName(), Double.valueOf(a.getValue()));
							}
							else {
								k1.put(a.getName(), a.getValue());
							}
						} else {
							k1.put(a.getName(), a.getValue());
						}
						if (a.getName().equals("domain")){
							domain = a.getValue();
						}
						if (a.getName().equals("var")){
							varName = a.getValue();
						}
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}
			}
			try {
				ef1.put(e2.getName(), k1);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		
		}
		return ef1;
	}
	
	/*
	 * <assigner>
			<add>
				<op1 var="correctResponses" domain="dstate" />
				<op2 value="1" />
			</add>
		</assigner>
	 */

	private JSONObject getMath(Element e2) {
		JSONObject math = new JSONObject();
		for (Iterator<Element> j = e2.elementIterator(); j.hasNext();){
			Element e = (Element) j.next();
			try {
				if (e.getName().equals("add")){
					math.put("add", getMath2(e));
				}
				else if (e.getName().equals("subtract")){
					math.put("subtract", getMath2(e));
				}
				else if (e.getName().equals("array")){
					/*
					 * <assigner>
					    <array var="tasks" domain="dstate">
						    <index var="currentTaskIndex" domain="dstate" />
						</array>
					   </assigner>
					 */
					math.put("array", getArrayReference(e));
				}
			} catch (JSONException e1) {
					e1.printStackTrace();
			}
		}
		return math;
	}

	private JSONObject getArrayReference(Element e) throws JSONException {
		JSONObject a = new JSONObject();
		//adding the array variable info
		for (Iterator<Attribute> k = e.attributeIterator(); k.hasNext();){
			Attribute s = (Attribute) k.next();
			a.put(s.getName(), s.getValue());
		}
		//adding the index variable info
		List<Element> el = e.elements("index");
		Element in = el.get(0);
		JSONObject ino = new JSONObject();
		for (Iterator<Attribute> k = in.attributeIterator(); k.hasNext();){
			Attribute s = (Attribute) k.next();
			ino.put(s.getName(), s.getValue());
		}
		a.put("index", ino);
		return a;
	}

	private JSONArray getMath2(Element e) {
		JSONArray a = new JSONArray();
		for (Iterator<Element> j = e.elementIterator(); j.hasNext();){
			Element e2 = (Element) j.next();
			JSONObject sj = new JSONObject();
			try {
				for (Iterator<Attribute> k = e2.attributeIterator(); k.hasNext();){
					Attribute s = (Attribute) k.next();
					if (s.getName().equals("value")){
						sj.put(s.getName(), Double.valueOf(s.getValue()));
					} else {
						sj.put(s.getName(), s.getValue());
					}
				}
				a.put(sj);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		return a;
	}

	private JSONObject getRulePrecondition(Element ef) {
		JSONObject pre = new JSONObject();
		for (Iterator<Element> j = ef.elementIterator(); j.hasNext();){
			Element e = (Element) j.next();
			try {
				if (e.getName().equals("and")){
					pre.put("and", getPreConditionElementList(e));
				}
				else if (e.getName().equals("or")){
					pre.put("or", getPreConditionElementList(e));
				}
				else {
					pre.put("element", getPreConditionElement(e));
				}
			} catch (JSONException e1) {
					e1.printStackTrace();
			}
		}
		return pre;
	}

	private JSONObject getPreConditionElement(Element e) throws JSONException {
		JSONObject el = new JSONObject();
		el.put("lo", e.getName());
		for (Iterator<Element> j = e.elementIterator(); j.hasNext();){
			Element e2 = (Element) j.next();
			JSONObject sj = new JSONObject();
			try {
				for (Iterator<Attribute> k = e2.attributeIterator(); k.hasNext();){
					Attribute s = (Attribute) k.next();
					sj.put(s.getName(), s.getValue());
				}
				el.put(e2.getName(), sj);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		return el;
	}

	private JSONArray getPreConditionElementList(Element e) {
		JSONArray a = new JSONArray();
		for (Iterator<Element> j = e.elementIterator(); j.hasNext();){
			Element e2 = (Element) j.next();
			try {
				if (e2.getName().equals("and")){
					JSONObject sj = new JSONObject();
					sj.put("and", getPreConditionElementList(e2));
					a.put(sj);
				}
				else if (e2.getName().equals("or")){
					JSONObject sj = new JSONObject();
					sj.put("or", getPreConditionElementList(e2));
					a.put(sj);
				}
				else {
					JSONObject sj = new JSONObject();
					sj.put("element", getPreConditionElement(e2));
					a.put(sj);
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		return a;
	}
	
	private String getVariableType(String name, String domain) throws JSONException{
		JSONArray searchArray;
		if (domain.equals("dstate")){
			searchArray = dstate;
		} 
		else if (domain.equals("input")){
			searchArray = inputVariables;
		}
		else {
			searchArray = metric;
		}
		
		for (int i=0; i < searchArray.length(); i++){
			JSONObject j = searchArray.getJSONObject(i);
			if (j.has("name")){
				if (j.getString("name").equals(name)){
					return j.getString("type");
				}
			}
		}
		return "null";
	}

	private JSONObject getVariable(Element v){
		JSONObject var = new JSONObject();
		String type = null;
		if (v.getName().equals("var")){
			 //logger.info(v.toString());
			 for (Iterator<Attribute> j = v.attributeIterator(); j.hasNext();){
				Attribute e = (Attribute) j.next();
				try {
					var.put(e.getName(),e.getValue());
					if (e.getName().equals("type")){
						type = e.getValue();
					}
					//logger.info(e.getName() + "," + e.getValue());
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
			//add a default value key.. 
			try {
				if (!var.has("value")){
					if (type != null && type.equals("symbolic")){
						var.put("value", "null");
					} 
					else if (type != null && type.equals("string")){
						var.put("value", "null");
					} 
					else if (type != null && type.equals("boolean")){
						var.put("value", "false");
					} 
					else if (type != null && type.equals("number")){
						var.put("value", 0.0);
					} else if (type != null && type.startsWith("array")){
						var.put("value", new JSONArray());
					}
				} 
				//if we already have a value.. it may need to be reassigned 
				//using the right type info
				else {
					if (type != null && type.equals("number")){
						try {
							var.put("value", Double.valueOf(var.getString("value")));
						}
						catch(NumberFormatException nfe) {  
							logger.info("ERROR! Wrong argument for type number");  
							var.put("value", 0.0);
						}  
					} else if (type != null && type.startsWith("array")){
						String elType = (type.replaceAll("^array\\(", "")).replaceAll("\\)$","");
						String val = var.getString("value");
						if (val.startsWith("[") && val.endsWith("]")){
							val = val.replaceAll("^\\[", "");
							val = val.replaceAll("\\]$", "");
							String[] ar = val.split(",");
							JSONArray j = new JSONArray();
							for (int i=0; i<ar.length;i++){
								if (elType.equals("number")){
									if (isNumber(ar[i])){
										j.put(Double.valueOf(ar[i]));
									} else {
										logger.info("ERROR! Wrong argument for type number within array");  
										j.put(0);
									}
								} else {
									j.put(ar[i]);
								}
							}
							var.put("value", j);
						} else {
							logger.info("ERROR! Wrong argument for type array");  
						}
					}
				}
				
				
			} catch (JSONException e1) {
			e1.printStackTrace();
			}
		}
		 
		return var;
	}

	
	public JSONObject getProperties(){
		return properties;
	}
	
	public JSONArray getDialogueState() {
		return dstate;
	}
	
	public JSONArray getInputVariables() {
		return this.inputVariables;
	}
	
	public JSONArray getOutputVariables() {
		return this.outputVariables;
	}
	
	public JSONArray getUpdateRules() {
		return this.updateRules;
	}
	
	public JSONArray getActionStrategies() {
		return this.actionStrategies;
	}
	
	public JSONArray getMetrics() {
		return this.metric;
	}
	
	private Boolean isNumber(String in){
		try {
			Double.valueOf(in);
		}
		catch(NumberFormatException nfe) {  
			return false;
		}  
		return true;
	}

	public JSONArray getLearnerState() {
		return this.lstate;
	}

	public JSONArray getActionSet() {
		return this.actionSet;
	}
}
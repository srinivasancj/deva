package deva.im;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import deva.State;
import deva.learner.LearnerState;




public class Domains{
	DialogueState d;
	LearnerState l;
	Input iv;
	Output ov;
	Metric mt;

	Logger logger = Logger.getLogger(Domains.class.getName());
	
	
	public Domains(){
		PropertyConfigurator.configure("log4j.properties");
		d = new DialogueState();
		l = new LearnerState();
		iv = new Input();
		ov = new Output();
		mt = new Metric();
	}
	
	/*
	public void setDialogueState(DialogueState d){
		this.d = d;
	}*/
	
	public void setInput(Input d){
		this.iv = d;
	}
	
	public void setOutput(Output d){
		this.ov = d;
	}


	public void setLearnerState(LearnerState l) {
		this.l = l;
		
	}


	public DialogueState getDState() {
		return d;
	}


	public Input getInput() {
		return iv;
	}


	public Output getOutput() {
		return ov;
	}


	public Metric getMetrics() {
		return mt;
	}


	public LearnerState getLearnerState() {
		return l;
	}
	
}
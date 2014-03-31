package edu.mit.csail.sdg.alloy4compiler.generator;

import java.util.ArrayList;

public class DefAndInvariants {
	public String def = "";
	public ArrayList<String> invariants = new ArrayList<String>();
	public String extra = "";
	public DefAndInvariants() {}
	
	public DefAndInvariants(String def){
		this.def = def;
	}
	
	public void join(DefAndInvariants d){
		this.def = d.def;
		this.invariants.addAll(d.invariants);
	}
	
	@Override
	public String toString() {
		return "'" + def + "' ( " + invariants + ")";
	}
}

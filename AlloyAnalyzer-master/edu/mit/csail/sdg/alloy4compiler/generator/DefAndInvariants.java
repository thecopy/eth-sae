package edu.mit.csail.sdg.alloy4compiler.generator;

import java.util.ArrayList;

public class DefAndInvariants {
	public String def = "";
	public ArrayList<String> invariants = new ArrayList<String>();
	
	public DefAndInvariants() {}
	
	public DefAndInvariants(String def){
		this.def = def;
	}
}

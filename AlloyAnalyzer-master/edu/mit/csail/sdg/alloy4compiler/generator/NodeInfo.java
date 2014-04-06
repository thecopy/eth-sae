package edu.mit.csail.sdg.alloy4compiler.generator;

import java.util.ArrayList;
import java.util.HashSet;

import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;

public class NodeInfo {
	public enum FieldModifier {
		Set,
		One
	}
	
	public String typeName = "";
	public HashSet<String> types = new HashSet<String>();
	public HashSet<String> getTypes() {
		if(types.size() == 0)
			types.add(typeName);
		
		return types;
	}
	
	public ArrayList<String> invariants = new ArrayList<String>();
	public String fieldName = "";
	public String csharpCode = "";
	public PrimSig sig = null;
	public FieldModifier fieldModifier = FieldModifier.One;
	
	public NodeInfo() {}
	
	public NodeInfo(String typeName){
		this.typeName = typeName;
	}

	public NodeInfo(String typeName, String fieldName){
		this.typeName = typeName;
		this.fieldName = fieldName;
	}
	
	public void join(NodeInfo d){
		this.typeName = d.typeName;
		this.invariants.addAll(d.invariants);
		this.fieldName = d.fieldName;
	}
	
	@Override
	public String toString() {
		return "TypeName = " + typeName + " FieldName = " + fieldName + " C#-Code: " + csharpCode + "(" + invariants + ")";
	}
}

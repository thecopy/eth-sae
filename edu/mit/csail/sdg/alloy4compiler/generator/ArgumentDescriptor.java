package edu.mit.csail.sdg.alloy4compiler.generator;

public class ArgumentDescriptor {
	public String name;
	public String typeName;
	public int quantity;
	
	public ArgumentDescriptor(String name){
		this.name = name;
	}

	public ArgumentDescriptor(String name, String typeName){
		this.name = name;
		this.typeName = typeName;
	}

	public ArgumentDescriptor(String name, String typeName, int quantity){
		this.name = name;
		this.typeName = typeName;
		this.quantity = quantity;
	}
}

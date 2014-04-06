package edu.mit.csail.sdg.alloy4compiler.generator;

public class InvariantDescriptor {
	public enum InvariantConstraint{
		NONE,
		SET_ONLY,
		NONSET_ONLY
	}
	
	public InvariantDescriptor(String inv) {
		this.invariant = inv;
	}	
	public InvariantDescriptor(String inv, InvariantConstraint invariantConstraint) {
		this.invariant = inv;
		this.invariantConstraint = invariantConstraint;
	}	
	
	public String invariant;
	public InvariantConstraint invariantConstraint = InvariantConstraint.NONE;
}

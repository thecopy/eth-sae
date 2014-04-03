package edu.mit.csail.sdg.alloy4compiler.generator;

import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;

public class ASPHelper {
	public static NodeInfo findFirstCommonClass(PrimSig a, PrimSig b){
		return findCommonParentClass(a, b, true);
	}
	public static NodeInfo findDeepestCommonClass(PrimSig a, PrimSig b){
		return findCommonParentClass(a, b, false);
	}
	
	public static NodeInfo findCommonParentClass(PrimSig a, PrimSig b, boolean first){
		if(a == null || b == null){
			System.out.println("The supplied classes are not non-null");
			return new NodeInfo("Object");
		}
		
		PrimSig currentBaseA = a;
		PrimSig currentBaseB = b;
		PrimSig deepestCommon = null;
		
		while(true){
			currentBaseA = currentBaseA.parent;
			if(currentBaseA == null || currentBaseA.label.equals("univ"))
				break;

			System.out.println("Checking if " + currentBaseA.label + " is equal to some of B's parents");
			currentBaseB = (PrimSig)b;
			while(true){
				if(currentBaseB == null || currentBaseB.label.equals("univ"))
					break;
				
				if(currentBaseA.label.equals(currentBaseB.label)){
					deepestCommon = currentBaseB;
					
					if(first)
						return new NodeInfo(deepestCommon.label.substring(5));
				}
				
				currentBaseB = currentBaseB.parent;
			}
		}
		if(deepestCommon == null)
			return new NodeInfo("Object");
		
		return new NodeInfo(deepestCommon.label.substring(5));
	}
}

package edu.mit.csail.sdg.alloy4compiler.generator;

import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;

public class ASTHelper {
	public static String extractGeneratedInstanceName(String s){
		  String name = s.substring(0, s.indexOf("$"));
		  name += s.substring(s.indexOf("$") + 1);
		  
		  return name;
	}
	
	public static NodeInfo findFirstCommonClass(PrimSig a, PrimSig b){
		return findCommonParentClass(a, b, true);
	}
	public static NodeInfo findDeepestCommonClass(PrimSig a, PrimSig b){
		return findCommonParentClass(a, b, false);
	}
	
	public static NodeInfo findCommonParentClass(PrimSig a, PrimSig b, boolean first){
		if(a == null || b == null){
			return new NodeInfo("Object");
		}
		
		PrimSig currentBaseA = a;
		PrimSig currentBaseB = b;
		PrimSig deepestCommon = null;
		
		while(true){
			currentBaseA = currentBaseA.parent;
			if(currentBaseA == null || currentBaseA.label.equals("univ"))
				break;

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

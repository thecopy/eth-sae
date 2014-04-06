package edu.mit.csail.sdg.alloy4compiler.generator;

import java.util.HashSet;

import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary.Op;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;

public class ASTHelper {
	public static NodeInfo handleSimpleBinaryOperator(ExprBinary x, NodeInfo ret, NodeInfo left, NodeInfo right){
		String operator = "???";
		ret.typeName = "bool";
		
		switch (x.op) {
		case NOT_LTE:
		case GT:
			operator = ">";
			break;
		case NOT_GTE:
		case LT:
			operator = "<";
			break;
		case NOT_LT:
		case GTE:
			operator = ">=";
			break;
		case NOT_GT:
		case LTE:
			operator = "<=";
			break;
		case MUL:
			operator = "*";
			ret.typeName = "int";
			break;
		case AND:
			operator = "&&";
			break;
		case DIV:
			operator = "/";
			ret.typeName = "int";
			break;
		case NOT_EQUALS:
			operator = "!=";
			break;
		case OR:
			operator = "||";
			break;
		case REM:
			operator = "%";
			ret.typeName = "int";
			break;
		}
		

		StringBuilder s = new StringBuilder();
		s.append("(");
		s.append(left.fieldName);
		s.append(") ");
		s.append(operator);
		s.append(" (");
		s.append(right.fieldName);
		s.append(")");
		
		ret.csharpCode = s.toString();
		ret.invariants.addAll(left.invariants);
		ret.invariants.addAll(right.invariants);
		
		return ret;
	}
	public static NodeInfo generateInvariantsForSetOperation(ExprBinary x, NodeInfo ret, NodeInfo left, NodeInfo right){
		ret.types = left.getTypes();
		for(String type : right.getTypes()){
			if(!ret.types.contains(type)){
				ret.types.add(type);
			}
		}
		StringBuilder typeCheckBuilder = new StringBuilder();
		
		for(String type : ret.getTypes())
			typeCheckBuilder.append(" || {def} is " + type);

		if(x.left.deNOP() instanceof Sig){ // If this is the case, assume that multiplicity is one
			ret.invariants.add("(" + typeCheckBuilder.toString().substring(4) + ")");
		}else{	
			ret.invariants.add("Contract.ForAll({def}, e => " + typeCheckBuilder.toString().substring(4).replace("{def}", "e") + ")");
			
			if(!left.fieldName.isEmpty() && !right.fieldName.isEmpty()){
				switch(x.op){
				case PLUS:
					ret.invariants.add("Contract.ForAll({def}, e => " + left.fieldName + ".Contains(e) || " + right.fieldName + ".Contains(e))");					
					ret.invariants.add("Contract.ForAll(" + left.fieldName + ", e => {def}.Contains(e))");
					ret.invariants.add("Contract.ForAll(" + right.fieldName + ", e => {def}.Contains(e))");
					ret.fieldName = left.fieldName + ".Union<" + ret.typeName + ">(" + right.fieldName + ")";
					break;
				case MINUS:
					ret.invariants.add("Contract.ForAll({def}, e => " + left.fieldName + ".Contains(e) && " + right.fieldName + ".Contains(e) == false)");
					ret.fieldName = left.fieldName + ".Except<" + ret.typeName + ">(" + right.fieldName + ")";
					break;
				case INTERSECT:
					ret.fieldName = left.fieldName + ".Intersect<" + ret.typeName + ">(" + right.fieldName + ")";					
					ret.invariants.add("Contract.ForAll({def}, e => " + left.fieldName + ".Contains(e) && "	+ right.fieldName + ".Contains(e))");
					ret.invariants.add("Contract.ForAll({def}, e => e is " + left.typeName + " && e is " + right.typeName + ") ");
					break;
				default:
					System.out.println("Errornous OP type in generateInvariantsForSetOperation: " + x.op);
					break;
				}
			}
		}
		
		return ret;
	}
	
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

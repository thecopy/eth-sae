package edu.mit.csail.sdg.alloy4compiler.generator;

import java.util.HashSet;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary.Op;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;
import edu.mit.csail.sdg.alloy4compiler.generator.InvariantDescriptor.InvariantConstraint;

public class ASTHelper {
	public static Expr deJOIN(Expr expr){
		if(expr instanceof ExprBinary && ((ExprBinary)expr).op.equals(ExprBinary.Op.JOIN))
			return deJOIN(((ExprBinary)expr).right);
		
		return expr;
		
	}
	public static boolean isSet(Expr expr){
		
		if(false == (expr instanceof ExprUnary)){
			System.err.print("Expression root is not ExprUnary, but rather " + expr.getClass());

			if(expr.deNOP().mult != 2){
				System.err.println(", and mult is not 2 => Not a set");
				return false;
			}
			System.err.println(", but mult=2 => It is a set");
			return true;
		}
		
		ExprUnary unaryExpr =(ExprUnary)expr.deNOP();
		
		// If the unary op is not declaring a set or some, then this is not a set, go to the next invariant
		if(false == (unaryExpr.op.equals(ExprUnary.Op.SOME) || unaryExpr.op.equals(ExprUnary.Op.SETOF))){
			System.err.println("Expression root is ExprUnary but it does not declare SOMEOF or SETOF => Not a set");
			return false;
		}
		
		System.err.println("Expression root is ExprUnary which delcare SOMEOF or SETOF _or_ is or mult=2 => Is a set");

		return true; // if not assume this expression is an ISet<T>
	}
	
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
		case IPLUS:
			operator = "+";
			ret.typeName = "int";
			break;
		case IMINUS:
			operator = "-";
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
		StringBuilder typeCheckBuilder = new StringBuilder();
		
		for(String type : ret.getTypes())
			typeCheckBuilder.append(" || {def} is " + type);

		ret.addInvariant("(" + typeCheckBuilder.toString().substring(4) + ")", InvariantConstraint.NONSET_ONLY);
		ret.addInvariant("Contract.ForAll({def}, e => " + typeCheckBuilder.toString().substring(4).replace("{def}", "e") + ")", InvariantConstraint.SET_ONLY);

		if(!left.fieldName.isEmpty() && !right.fieldName.isEmpty()){
			switch(x.op){
			case PLUS:
				ret.addInvariant("Contract.ForAll({def}, e => " + left.fieldName + ".Contains(e) || " + right.fieldName + ".Contains(e))", InvariantConstraint.SET_ONLY);					
				ret.addInvariant("Contract.ForAll(" + left.fieldName + ", e => {def}.Contains(e))", InvariantConstraint.SET_ONLY);
				ret.addInvariant("Contract.ForAll(" + right.fieldName + ", e => {def}.Contains(e))", InvariantConstraint.SET_ONLY);
				ret.fieldName = left.fieldName + ".Union<" + ret.typeName + ">(" + right.fieldName + ")";
				ret.fieldName = "new HashSet<" + ret.typeName + ">(" + ret.fieldName + ")";
				break;
			case MINUS:
				ret.addInvariant("Contract.ForAll({def}, e => " + left.fieldName + ".Contains(e) && " + right.fieldName + ".Contains(e) == false)", InvariantConstraint.SET_ONLY);
				ret.fieldName = left.fieldName + ".Except<" + ret.typeName + ">(" + right.fieldName + ")";
				ret.fieldName = "new HashSet<" + ret.typeName + ">(" + ret.fieldName + ")";
				break;
			case INTERSECT:
				ret.fieldName = left.fieldName + ".Intersect<" + ret.typeName + ">(" + right.fieldName + ")";
				ret.fieldName = "new HashSet<" + ret.typeName + ">(" + ret.fieldName + ")";
				ret.addInvariant("Contract.ForAll({def}, e => " + left.fieldName + ".Contains(e) && "	+ right.fieldName + ".Contains(e))", InvariantConstraint.SET_ONLY);
				ret.addInvariant("Contract.ForAll(" + left.fieldName + ", e => " + right.fieldName + ".Contains(e))", InvariantConstraint.SET_ONLY);
				ret.addInvariant("Contract.ForAll(" + right.fieldName + ", e => " + left.fieldName + ".Contains(e))", InvariantConstraint.SET_ONLY);
				break;
			default:
				System.out.println("Errornous OP type in generateInvariantsForSetOperation: " + x.op);
				break;
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

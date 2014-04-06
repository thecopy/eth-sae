package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4compiler.ast.Attr;
import edu.mit.csail.sdg.alloy4compiler.ast.Browsable;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBad;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprCall;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprHasName;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprITE;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprLet;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprList;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprQt;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary.Op;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.SubsetSig;
import edu.mit.csail.sdg.alloy4compiler.ast.Type;
import edu.mit.csail.sdg.alloy4compiler.ast.Type.ProductType;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.generator.InvariantDescriptor.InvariantConstraint;
import edu.mit.csail.sdg.alloy4compiler.generator.NodeInfo.FieldModifier;

public class CodeGeneratorVisitor extends VisitQuery<NodeInfo> {

	private PrintWriter out;
	int ident = 0;
	public void setIdent(int i){
		ident = i;
	}

	public CodeGeneratorVisitor(PrintWriter out) throws Exception{
		if(out == null)
			throw new Exception("ArgumentNullException: out");

		this.out = out;
		System.out.println("Init Visistor with PrintWriter");
	}


	@Override
	public NodeInfo visit(Sig x) throws Err{	
		ident++;	

		sprintln("Visit Sig: " + x.label);
		String finalLabel = x.label;
		if(x.label.startsWith("this/")){
			finalLabel = x.label.substring(5);
		} else if (x.label.equals("Int")){
			finalLabel = "int";
		}else{
			finalLabel = x.label;
		}
		NodeInfo ret = new NodeInfo(finalLabel);
		ret.sig = (PrimSig)x;

		ident--;
		return ret;
	}

	@Override
	public NodeInfo visit(ExprBad x) throws Err {
		sprintln("Visit bad code.");
		sprintln("  // Illegal code: " + x.toString());
		return new NodeInfo("??? /* ExprBad */");
	}

	// We do not have to support: IDEN, NEXT, EMPTYNESS 
	@Override
	public NodeInfo visit(ExprConstant x) throws Err {

		ident++;
		sprintln("Visit constant expression with OP " + x.op + " and type " + x.type());
		NodeInfo ret = new NodeInfo();
		ret.typeName = "?";

		switch(x.op){
		case NUMBER:
			ret.typeName = "int";
			ret.fieldName = Integer.toString(x.num);	
			ret.csharpCode = ret.fieldName;
			break;
		case FALSE:
			ret.typeName = "bool";
			ret.fieldName = "false";
			ret.csharpCode = ret.fieldName;
			break;
		case TRUE:
			ret.typeName = "bool";
			ret.fieldName = "true";
			ret.csharpCode = ret.fieldName;
			break;
		case MAX:
			ret.typeName = "int";
			ret.fieldName = "Int32.MaxValue";
			ret.csharpCode = ret.fieldName;
			break;
		case MIN:
			ret.typeName = "int";
			ret.fieldName = "Int32.MinValue";
			ret.csharpCode = ret.fieldName;
			break;
		case STRING:
			ret.typeName = "string";
			ret.fieldName = x.string;
			ret.csharpCode = ret.fieldName;
			break;
		default:
			sprintln("! ECONSTTYPE: " + x.type());
			break;
		}
		ident--;
		return ret;
	}

	@Override
	public NodeInfo visit(ExprCall x) throws Err {
		sprintln("Visit call expression: " + x.toString());
		sprintln("[ CALL EXPRESSION ]");
		return new NodeInfo("??? /* ExprCall */");
	}

	@Override
	public NodeInfo visit(Field x) throws Err {
		ident++;
		sprintln("Visit field expression: " + x + ", type: " + x.type());

		NodeInfo s = new NodeInfo();

		Expr e = x.type().toExpr();
		if(e instanceof ExprBinary){
			ExprBinary bin = (ExprBinary)e;
			if(x.type().arity() == 2){
				s.typeName = ((Sig)bin.right).label.substring(5);
				s.sig = (PrimSig)bin.right;
			}
			else{ 
				// Arity = 3 since we aren't supposed to support higher
				// Assume left is another binary with -> operator
				ExprBinary binLeft = (ExprBinary)bin.left;
				NodeInfo left = null;
				NodeInfo right = null;

				if(binLeft.right instanceof Sig){
					left = new NodeInfo(((Sig)binLeft.right).label.substring(5));
				}else{
					left = binLeft.right.accept(this); 		// x.left.right
				}
				if(bin.right instanceof Sig){
					right = new NodeInfo(((Sig)bin.right).label.substring(5));
				}else{
					right = bin.right.accept(this); 		// x.left.right
				}

				s.typeName = "ISet<Tuple<";
				s.typeName += left.typeName;
				s.typeName += ",";
				s.typeName += right.typeName;
				s.typeName += ">>";
			}
		}else{
			s = new NodeInfo("Unknown Field Expression");
		}

		s.addInvariant(x.label + " != null");
		s.fieldName = x.label;
		sprintln("Field Expression returning: " + s);
		ident--;
		return s;
	}

	@Override
	public NodeInfo visit(ExprUnary x) throws Err {
		ident++;
		sprintln("Visit unary expression ('" + x.toString() + "') with OP: '" + x.op + "' (" + x.op.name() + ") and sub: " + x.sub.toString() + ", type: " + x.type());
		NodeInfo ret = new NodeInfo();
		NodeInfo t;

		switch(x.op){ 
		case ONEOF:
			ret.addInvariant("{def} != null");
		case LONEOF:
			t = x.sub.accept(this);
			ret.typeName = t.typeName;
			ret.fieldModifier = t.fieldModifier;
			ret.invariants.addAll(t.invariants);
			ret.fieldName = t.fieldName;
			break;
		case SOMEOF:
			ret.addInvariant("{def}.Count() > 0");
		case SETOF:
			t = (NodeInfo) x.sub.accept(this);
			ret.typeName += "ISet<" + t.typeName + ">";
			ret.addInvariant("{def} != null");
			ret.addInvariant("Contract.ForAll({def}, e => e != null)");
			//ret = ASTHelper.generateInvariantsForSetOperation(x, ret, left, right);
			ret.invariants.addAll(t.invariants);
			ret.fieldName = t.fieldName;
			break;
		case NOOP:
			ret = x.sub.accept(this);
			break;
		case CLOSURE:
			t = x.sub.accept(this);
			ret.typeName = "Object";
			ret.fieldModifier = t.fieldModifier;
			ret.csharpCode = "Helper.Closure(" + t.fieldName + ")";
			break;
		case RCLOSURE:
			t = x.sub.accept(this);
			ret.typeName = "Object";
			ret.fieldModifier = t.fieldModifier;
			ret.csharpCode = "Helper.RClosure(" + t.fieldName + ")";
			break;
		case CAST2INT:
			ret = x.sub.accept(this);
			ret.typeName = "int";
			ret.csharpCode = "(int)" + ret.csharpCode;
			ret.fieldName = "(int)" + ret.fieldName;
			break;
		case CARDINALITY:
			t = x.sub.accept(this);
			ret.typeName = "int";
			ret.csharpCode = t.fieldName + ".Count()";
			ret.fieldName = t.fieldName + ".Count()";
			break;
		case CAST2SIGINT: // no idea what this is
			ret = x.sub.accept(this);
			break;
		case NOT:
			t = x.sub.accept(this);
			ret.csharpCode = "!(" + t.csharpCode + ")";
			ret.fieldName = "!(" + t.csharpCode + ")";
			ret.typeName = "bool";
			break;
		case TRANSPOSE: // hm?
			ret = x.sub.accept(this); // i dont know
			break;
			// We do not have to support these
		case EXACTLYOF:
		case NO:
		case SOME:
		case LONE:
		case ONE:
		default:
			ret.typeName = "??? /*ExprUnary. Unkown Operator Type: \"" + x.op + "\" (" + x.op.name() + ")*/";
			break;

		}

		sprintln("Unary Expression returning " + ret);

		ident--;
		return ret;
	}	

	@Override
	public NodeInfo visit(ExprBinary x) throws Err {
		ident++;
		sprintln("Visit binary expression (mult=" + x.mult + ", type=" + x.type() + ", type size=" + x.type().size() + ") (OP=" + x.op.name() + ", '" + x.op + "' ) [" + x + "].");

		StringBuilder s = new StringBuilder();
		NodeInfo ret = new NodeInfo();

		switch (x.op) {
		case ARROW: // "set -> set " (Set of Tuples)
			NodeInfo left = x.left.accept(this);
			NodeInfo right = x.right.accept(this);
			
			s.append("ISet<Tuple<");
			s.append(left.typeName);
			s.append(", ");
			s.append(right.typeName);
			s.append(">>");

			if(right.fieldName != null && !right.fieldName.isEmpty()){
				ret.addInvariant("Contract.ForAll({def}, e => e.Item2.Equals(" + right.fieldName + "))");
			}
			if(left.fieldName != null && !left.fieldName.isEmpty()){
				ret.addInvariant("Contract.ForAll({def}, e => e.Item1.Equals(" + left.fieldName + "))");
			}

			ret.typeName = s.toString();
			ret.addAllInvariants(right.invariants);
			ret.addInvariant("{def} != null");
			ret.addInvariant("Contract.ForAll({def}, e => e != null)");
			break;
		case ANY_ARROW_LONE: // "A -> lone B" (Tuple) (Lone can be null (0 or 1))
			left = x.left.accept(this);
			right = x.right.accept(this);
			s.append("ISet<Tuple<");
			s.append(left.typeName);
			s.append(", ");
			s.append(right.typeName);
			s.append(">>");

			ret.typeName = s.toString();
			ret.addInvariant("{def} != null");

			ret.addInvariant(
					"Contract.ForAll({def}, e1 => e1 != null)"
							+ " && {def}.Count(x => x.Item1.Equals(e1.Item1)) <= 1");

			if(left.fieldName != null && !left.fieldName.isEmpty()){
				ret.addInvariant("Contract.ForAll({def}, e => e.Item1.Equals(" + left.fieldName + "))");
			}
			if(right.fieldName != null && !right.fieldName.isEmpty()){
				ret.addInvariant("Contract.ForAll({def}, e => e.Item2.Equals(" + right.fieldName + "))");
			}

			ret.invariants.addAll(left.invariants);
			ret.invariants.addAll(right.invariants);
			break;
		case ONE_ARROW_ONE: // Set of Tuples with one-to-one mapping
			left = x.left.accept(this);
			right = x.right.accept(this);
			s.append("ISet<Tuple<");
			s.append(left.typeName);
			s.append(",");
			s.append(right.typeName);
			s.append(">>");

			ret.typeName = s.toString();
			ret.addInvariant("{def} != null");
			ret.addInvariant(
					"Contract.ForAll({def}, e1 => e1 != null" 
							+ " && {def}.Count(x => x.Item1.Equals(e1.Item1)) == 1"
							+ " && {def}.Count(x => x.Item2.Equals(e1.Item2)) == 1)");

			if(left.fieldName != null && !left.fieldName.isEmpty()){
				ret.addInvariant("Contract.ForAll({def}, e => e.Item1.Equals(" + left.fieldName + "))");
			}
			if(right.fieldName != null && !right.fieldName.isEmpty()){
				ret.addInvariant("Contract.ForAll({def}, e => e.Item2.Equals(" + right.fieldName + "))");
			}

			ret.invariants.addAll(left.invariants);
			ret.invariants.addAll(right.invariants);
			break;

		case INTERSECT:
			left = x.left.accept(this);
			right = x.right.accept(this);

			String typeName = ASTHelper.findFirstCommonClass(left.sig, right.sig).typeName;
			if(left.typeName.equals(right.typeName))
				typeName = left.typeName;

			ret.typeName = typeName;
			ret.addInvariant("{def} != null");

			ret.types = left.getTypes();
			for(String type : right.getTypes()){
				if(!ret.types.contains(type)){
					ret.types.add(type);
				}
			}

			ret = ASTHelper.generateInvariantsForSetOperation(x, ret, left, right);						
			break;

		case MINUS: // Set Difference, -
			left = x.left.accept(this);
			right = x.right.accept(this);

			typeName = ASTHelper.findFirstCommonClass(left.sig, right.sig).typeName;

			if(left.typeName.equals(right.typeName))
				typeName = left.typeName;

			s.append(typeName);

			ret.typeName = typeName;
			ret.addInvariant("{def} != null");

			ret.types = left.getTypes();

			ret = ASTHelper.generateInvariantsForSetOperation(x, ret, left, right);
			break;

		case PLUS: // Union, +, or logical disjunction
			left = x.left.accept(this);
			right = x.right.accept(this);

			typeName = ASTHelper.findFirstCommonClass(left.sig, right.sig).typeName;

			if(left.typeName.equals(right.typeName))
				typeName = left.typeName;

			ret.typeName = typeName;
			ret.addInvariant("{def} != null");

			ret.types = left.getTypes();
			for(String type : right.getTypes()){
				if(!ret.types.contains(type)){
					ret.types.add(type);
				}
			}

			ret = ASTHelper.generateInvariantsForSetOperation(x, ret, left, right);
			break;

		case JOIN:
			sprintln("Visiting left, type " + x.left.type() + "(" + x.left.getClass() + ")");
			left = x.left.accept(this);
			sprintln("Visiting right, type " + x.right.type() + "(" + x.right.getClass() + ")");
			right = x.right.accept(this);

			if(false == left.fieldName.isEmpty()){
				ret.fieldName = left.fieldName + ".";
			}
			ret.fieldName += right.fieldName;
			ret.typeName = right.typeName;
			break;

		case EQUALS:
			left = x.left.accept(this);
			right = x.right.accept(this);
			s.append("(");
			s.append(left.fieldName);
			s.append(".Equals(");
			s.append(right.fieldName);
			s.append("))");

			ret.typeName = "bool";
			ret.csharpCode = s.toString();
			ret.invariants.addAll(left.invariants);
			ret.invariants.addAll(right.invariants);
			break;

		case NOT_LTE:
		case GT:
		case NOT_GTE:
		case LT:
		case NOT_LT:
		case GTE:
		case NOT_GT:
		case LTE:
		case MUL:
		case AND:
		case DIV:
		case NOT_EQUALS:
		case OR:
		case REM:
			left = x.left.accept(this);
			right = x.right.accept(this);
			ret = ASTHelper.handleSimpleBinaryOperator(x, ret, left, right);
			break;
		case IMPLIES:
			break;
			// These we do not have to support
		case ISSEQ_ARROW_LONE:
		case DOMAIN:
		case RANGE:
		case PLUSPLUS:
		case SHL:
		case SHA:
		case SHR:
		default:
			ret.typeName = "Object /*ExprBinary Unkown Operator Type: \"" + x.op + "\" (" + x.op.name() + ")*/";
			break;
		}
		sprintln("Binary " + x.op.name() + " Expression returning " + ret );
		ident--;
		return ret;
	}


	@Override
	public NodeInfo visit(ExprITE x) throws Err {
		sprintln("Visit IF-ELSE expression");

		NodeInfo ret = new NodeInfo();
		NodeInfo cond = x.cond.accept(this);
		NodeInfo left = x.left.accept(this);
		NodeInfo right = x.right.accept(this);

		StringBuilder s = new StringBuilder();


		s.append("(");		
		s.append(cond.csharpCode);		
		s.append(") \r\n  ? (");		
		s.append(left.csharpCode);		
		s.append(")\r\n  : (");		
		s.append(right.csharpCode);		
		s.append(");\r\n");

		ret.typeName = ASTHelper.findFirstCommonClass(left.sig, right.sig).typeName;

		ret.csharpCode = s.toString();
		ret.fieldName = s.toString();
		return ret;
	}

	// let x = e | A  =	A with every occurrence of x replaced by expression e
	@Override
	public NodeInfo visit(ExprLet x) throws Err {
		ident++;

		NodeInfo ret = new NodeInfo();

		NodeInfo sub = x.sub.accept(this);
		NodeInfo var = x.var.accept(this);
		NodeInfo newVal = x.expr.accept(this);

		ident--;
		return new NodeInfo("??? /* ExprLet */");
	}	

	@Override
	public NodeInfo visit(ExprList x) throws Err {
		// stolen from TestGeneratorVisitor
		ident++;
		sprintln("Visit ExprList expression: " + x.toString());
		NodeInfo ret = new NodeInfo();

		NodeInfo[] argNodes = new NodeInfo[x.args.size()];
		for(int i = 0; i < x.args.size(); i++){
			sprintln("Going into arg in ExprList...");
			Expr arg = x.args.get(i);
			argNodes[i] = arg.accept(this);
		}
		String combiner = "/* ??? */";
		switch(x.op){
		case AND:
			combiner = "&&";
			break;
		case OR:
			combiner = "||";
			break;
		default:
			sprintln("Unsupported ExprList.OP: " + x.op);
		}

		for(NodeInfo argNode : argNodes){
			ret.csharpCode += " " + combiner + " " + argNode.csharpCode;
		}
		ret.csharpCode = ret.csharpCode.substring(4);
		sprintln("ExprList returning " + ret);
		ident--;
		return ret;
	}

	@Override
	public NodeInfo visit(ExprVar x) throws Err {
		ident++;
		sprintln("Visit Variable expression: " + x.toString());

		Expr e = x.type().toExpr();

		String typeName = null;
		String fieldName = x.label;

		if(e instanceof PrimSig){
			typeName = ((PrimSig)e).label.substring(5);
		}else{
			typeName = e.accept(this).typeName;
		}

		sprintln("Variable expression returning typeName = " + typeName + ", fieldName = " + fieldName);

		ident--;		
		return new NodeInfo(typeName, fieldName);
	}

	@Override
	public NodeInfo visit(ExprQt x) throws Err {
		sprintln("Visit quantified expression: " + x.toString());
		sprintln("!ENOTIMPL:QT:" + x.toString() + "!");
		return new NodeInfo("??? /* ExprQt */");
	}


	private void sprintln(Object s){
		String idents = "";
		for(int i = 0; i < ident; i++)
			idents += "  ";

		System.out.print(idents + s + "\r\n");
	}
}

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
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.SubsetSig;
import edu.mit.csail.sdg.alloy4compiler.ast.Type;
import edu.mit.csail.sdg.alloy4compiler.ast.Type.ProductType;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;

///
///
///     A lot of this has been directly copied from CodeGeneratorVisitor
///
///

public class TestGeneratorVisitor extends VisitQuery<NodeInfoTest> {
	
	private PrintWriter out;
	int ident = 0;
	public void setIdent(int i){
		ident = i;
	}
	int expect = 0;
	public void setExpect(int i){
		this.expect = i;
		
	}
	
	public TestGeneratorVisitor(PrintWriter out) throws Exception{
		if(out == null)
			throw new Exception("ArgumentNullException: out");
		
		this.out = out;
		System.out.println("Init Test Visistor with PrintWriter");
	}
	
	
	@Override
	public NodeInfoTest visit(Sig x) throws Err{
		sprintln("Visit Sig expression: " + x.toString());
		return new NodeInfoTest(x.label.substring(5));
	}
	
	@Override
	public NodeInfoTest visit(ExprBad x) throws Err {
		sprintln("Visit ExprBad expression: " + x.toString());
		return new NodeInfoTest("??? /* ExprBad */");
	}
	
	@Override
	public NodeInfoTest visit(ExprConstant x) throws Err {
		ident++;
		sprintln("Visit constant expression with OP " + x.op + " and type " + x.type());
		NodeInfoTest ret = new NodeInfoTest();
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
	public NodeInfoTest visit(Field x) throws Err {
		ident++;
		sprintln("Visit field expression: " + x + ", type: " + x.type());

		NodeInfoTest s = new NodeInfoTest();

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
				NodeInfoTest left = null;
				NodeInfoTest right = null;

				if(binLeft.right instanceof Sig){
					left = new NodeInfoTest(((Sig)binLeft.right).label.substring(5));
				}else{
					left = binLeft.right.accept(this); 		// x.left.right
				}
				if(bin.right instanceof Sig){
					right = new NodeInfoTest(((Sig)bin.right).label.substring(5));
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
			s = new NodeInfoTest("Unknown Field Expression");
		}

		s.fieldName = x.label;
		sprintln("Field Expression returning: " + s);
		ident--;
		return s;
	}
	
	@Override
	public NodeInfoTest visit(ExprUnary x) throws Err {
		ident++;
		sprintln("Visit unary expression ('" + x.toString() + "') with OP: '" + x.op + "' (" + x.op.name() + ") and sub: " + x.sub.toString() + ", type: " + x.type());
		NodeInfoTest ret = new NodeInfoTest();
		NodeInfoTest t;

		switch(x.op){ 
		case ONEOF:
		case LONEOF:
			t = x.sub.accept(this);
			ret.typeName = t.typeName;
			ret.invariants.addAll(t.invariants);
			ret.fieldName = t.fieldName;
			break;
		case SOMEOF:
		case SETOF:
			t = (NodeInfoTest) x.sub.accept(this);
			ret.typeName += "ISet<" + t.typeName + ">";
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
			ret.csharpCode = "Helper.Closure(" + t.fieldName + ")";
			break;
		case RCLOSURE:
			t = x.sub.accept(this);
			ret.typeName = "Object";	
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
	public NodeInfoTest visit(ExprList x) throws Err {
		ident++;
		sprintln("Visit ExprList expression: " + x.toString());
		NodeInfoTest ret = new NodeInfoTest();
		
		NodeInfoTest[] argNodes = new NodeInfoTest[x.args.size()];
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
		
		for(NodeInfoTest argNode : argNodes){
			ret.csharpCode += " " + combiner + " " + argNode.csharpCode;
		}
		ret.csharpCode = ret.csharpCode.substring(4);
		sprintln("ExprList returning " + ret);
		ident--;
		return ret;
	}
	
	@Override
	public NodeInfoTest visit(ExprQt x) throws Err {
		ident++;
		NodeInfoTest ret = new NodeInfoTest();
		
		sprintln("Visit ExprQt  with op: " + x.op + " with sub " + x.sub);
		
		StringBuilder subExprBuilder = new StringBuilder();
		String argType = "?";
		for(Decl decl : x.decls){
			NodeInfoTest exprInfo = decl.expr.deNOP().accept(this);
			argType = exprInfo.typeName;
			
			for(ExprHasName expr : decl.names){
				subExprBuilder.append(expr.label);
			}
		}

		NodeInfoTest subRet = x.sub.accept(this);
		
		subExprBuilder.append(" => ");
		subExprBuilder.append(subRet.csharpCode);
		subExprBuilder.append(")");
		
		switch (x.op) {
			case ONE:
				ret.csharpCode = argType + "Set.Where(";
				subExprBuilder.append(".Count() == 1");
				break;
			case SOME:
				ret.csharpCode = argType + "Set.Where(";
				subExprBuilder.append(".Count() > 1");
				break;
			case LONE:
				ret.csharpCode = argType + "Set.Where(";
				subExprBuilder.append(".Count() <= 1");
				break;
			case NO:
				ret.csharpCode = argType + "Set.Where(";
				subExprBuilder.append(".Count() == 0");
				break;
			case ALL:
				ret.csharpCode = "Contract.ForAll(" + argType + "Set, ";
				break;
			default:
				sprintln("Unkown ExprQt.Op: " + x.op);
				break;
		}
		
		ret.csharpCode += subExprBuilder.toString();
		
		sprintln("ExprQt returning " + ret);
		ident--;
		return ret;
	}
	
	@Override
	public NodeInfoTest visit(ExprVar x) throws Err {
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
		return new NodeInfoTest(typeName, fieldName);
	}
	
	@Override
	public NodeInfoTest visit(ExprCall x) throws Err {
		ident++;
		NodeInfoTest ret = new NodeInfoTest();

		sprintln("Visit ExprCall expression: " + x.toString());
		for(Expr arg : x.args){
			NodeInfoTest argInfo = arg.accept(this);
			ret.args.add(new ArgumentDescriptor(argInfo.fieldName, argInfo.typeName));
		}
		
		Func func = x.fun;
		String functionName = func.label.substring(5);
		sprintln("Target Function: " + functionName);
		NodeInfoTest returnType = func.returnDecl.accept(this);

		StringBuilder argBuilder = new StringBuilder();
		
		for(Decl decl : func.decls){
			NodeInfoTest declInfo = decl.expr.accept(this);
			sprintln(declInfo);
			for(ExprHasName declname : decl.names){
				argBuilder.append(",");
				argBuilder.append(" ");
				argBuilder.append(declname.label);
			}
		}
		ret.typeName = returnType.typeName;
		ret.csharpCode = "FuncClass." +functionName + "(" + argBuilder.toString().substring(2) + ")";

		sprintln("ExprCall returning " + ret);
		ident--;
		return ret;
	}

	@Override
	public NodeInfoTest visit(ExprBinary x) throws Err {
		ident++;
		sprintln("Visit binary expression (mult=" + x.mult + ", type=" + x.type() + ", type size=" + x.type().size() + ") (OP=" + x.op.name() + ", '" + x.op + "' ) [" + x + "].");

		StringBuilder s = new StringBuilder();
		NodeInfoTest ret = new NodeInfoTest();

		switch (x.op) {
		case ARROW: // "set -> set " (Set of Tuples)
			NodeInfoTest left = x.left.accept(this);
			NodeInfoTest right = x.right.accept(this);
			
			s.append("ISet<Tuple<");
			s.append(left.typeName);
			s.append(", ");
			s.append(right.typeName);
			s.append(">>");	

			ret.typeName = s.toString();
			break;
		case ANY_ARROW_LONE: // "A -> lone B" (Tuple) (Lone can be null (0 or 1))
			left = x.left.accept(this);
			right = x.right.accept(this);
			s.append("ISet<Tuple<");
			s.append(left.typeName);
			s.append(", ");
			s.append(right.typeName);
			s.append(">>");

			break;
		case ONE_ARROW_ONE: // Set of Tuples with one-to-one mapping
			left = x.left.accept(this);
			right = x.right.accept(this);
			s.append("ISet<Tuple<");
			s.append(left.typeName);
			s.append(",");
			s.append(right.typeName);
			s.append(">>");

			break;

		case INTERSECT:
			left = x.left.accept(this);
			right = x.right.accept(this);

			String typeName = ASTHelper.findFirstCommonClass(left.sig, right.sig).typeName;
			if(left.typeName.equals(right.typeName))
				typeName = left.typeName;

			ret.typeName = typeName;
					
			break;

		case MINUS: // Set Difference, -
			left = x.left.accept(this);
			right = x.right.accept(this);

			typeName = ASTHelper.findFirstCommonClass(left.sig, right.sig).typeName;

			if(left.typeName.equals(right.typeName))
				typeName = left.typeName;

			s.append(typeName);

			ret.typeName = typeName;

			break;

		case PLUS: // Union, +, or logical disjunction
			left = x.left.accept(this);
			right = x.right.accept(this);

			typeName = ASTHelper.findFirstCommonClass(left.sig, right.sig).typeName;

			if(left.typeName.equals(right.typeName))
				typeName = left.typeName;

			ret.typeName = typeName;
			ret.fieldName = left.fieldName + ".Union<" + ret.typeName + ">(" + right.fieldName + ")";
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
	public NodeInfoTest visit(ExprITE x) throws Err {
		sprintln("Visit ExprITE expression: " + x.toString());
		return new NodeInfoTest("??? /* ExprITE */");
	}
	
	@Override
	public NodeInfoTest visit(ExprLet x) throws Err {
		ident++;
		sprintln("Visit ExprLet expression: " + x.toString());

		NodeInfoTest ret = new NodeInfoTest();

		NodeInfoTest sub = x.sub.accept(this);
		NodeInfoTest var = x.var.accept(this);
		NodeInfoTest newVal = x.expr.accept(this);
		
		if(sub == null)
			sprintln("sub == null");
		if(var == null)
			sprintln("var == null");
		if(newVal == null)
			sprintln("newVal == null");
		ret.csharpCode = "{ " + newVal.typeName + " " + var.fieldName + " = " + newVal.fieldName + "; return  " + sub.csharpCode + ";}";
		
		ident--;
		sprintln("Visit ExprLet expression: " + ret);
		return ret;
	}	

	private void sprintln(Object s){
		String idents = "";
		for(int i = 0; i < ident; i++)
			idents += "  ";
		
		System.out.print(idents + s + "\r\n");
	}
}

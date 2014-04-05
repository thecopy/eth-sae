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
		sprintln("Visit constant expression of type " + x.type());
		NodeInfoTest ret = new NodeInfoTest();
		ret.typeName = "?";
		if(x.type().equals(ExprConstant.Op.NUMBER)){
		}else if(x.type().toString().equals("{PrimitiveBoolean}")){
			ret.typeName = "bool";
		}else if(x.type().equals(ExprConstant.Op.FALSE)){
			ret.typeName = "false";
		}else if(x.type().equals(ExprConstant.Op.TRUE)){
			ret.typeName = "true";
		}else if(x.type().equals(ExprConstant.Op.STRING)){
			ret.typeName = "string";
		}else if(x.type().equals(ExprConstant.Op.EMPTYNESS)){

		}else if(x.type().equals(ExprConstant.Op.NEXT)){

		}else if(x.type().equals(ExprConstant.Op.MAX)){

		}else if(x.type().equals(ExprConstant.Op.MIN)){

		}else{
			sprintln("! ECONSTTYPE: " + x.type());
		}

		ident--;
		return ret;
	}

	@Override
	public NodeInfoTest visit(Field x) throws Err {
		sprintln("Visit Field expression: " + x.toString());
		return new NodeInfoTest("??? /* Field */");
	}
	
	@Override
	public NodeInfoTest visit(ExprUnary x) throws Err {
		ident++;
		
		sprintln("Visit ExprUnary expression of type " + x.op + " with sub " + x.sub + ": " + x.toString());
		
		NodeInfoTest n = new NodeInfoTest();
		
		switch(x.op){
			case NOT:
				n = x.sub.accept(this);
			break;
			case ONEOF:
				n = x.sub.accept(this);
				break;
			case NOOP:
				n = x.sub.accept(this);
				break;
			default:
				sprintln("Unkown OP type: " + x.op + "(" + x.op.name() + ")");
				break;
		}

		sprintln("ExprUnary returning " + n);
		ident--;
		return n;
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
			ret.csharpCode = " " + combiner + " " + argNode.csharpCode;
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
		
		sprintln("Visit ExprVar expression: " + x.toString());
		
		sprintln("Label: " + x.label);
		
		ident--;

		return new NodeInfoTest("", x.label);
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
		sprintln("Visit ExprBinary expression: " + x.toString());
		return new NodeInfoTest("??? /* ExprBinary */");
	}
	
	
	@Override
	public NodeInfoTest visit(ExprITE x) throws Err {
		sprintln("Visit ExprITE expression: " + x.toString());
		return new NodeInfoTest("??? /* ExprITE */");
	}
	
	@Override
	public NodeInfoTest visit(ExprLet x) throws Err {
		sprintln("Visit ExprLet expression: " + x.toString());
		return new NodeInfoTest("??? /* ExprLet */");
	}	

	private void sprintln(Object s){
		String idents = "";
		for(int i = 0; i < ident; i++)
			idents += "  ";
		
		System.out.print(idents + s + "\r\n");
	}
}

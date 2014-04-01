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
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.SubsetSig;
import edu.mit.csail.sdg.alloy4compiler.ast.Type;
import edu.mit.csail.sdg.alloy4compiler.ast.Type.ProductType;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;

public class TestGeneratorVisitor extends VisitQuery<NodeInfo> {
	
	private PrintWriter out;
	int ident = 0;
	public void setIdent(int i){
		ident = i;
	}
	
	public TestGeneratorVisitor(PrintWriter out) throws Exception{
		if(out == null)
			throw new Exception("ArgumentNullException: out");
		
		this.out = out;
		System.out.println("Init Test Visistor with PrintWriter");
	}
	
	
	@Override
	public NodeInfo visit(Sig x) throws Err{
		sprintln("Visit Sig expression: " + x.toString());
		return new NodeInfo("??? /* Sig */");
	}
	
	@Override
	public NodeInfo visit(ExprBad x) throws Err {
		sprintln("Visit ExprBad expression: " + x.toString());
		return new NodeInfo("??? /* ExprBad */");
	}
	
	@Override
	public NodeInfo visit(ExprConstant x) throws Err {
		sprintln("Visit ExprConstant expression: " + x.toString());
		return new NodeInfo("??? /* ExprConstant */");
	}
	
	@Override
	public NodeInfo visit(ExprCall x) throws Err {
		sprintln("Visit ExprCall expression: " + x.toString());
		return new NodeInfo("??? /* ExprCall */");
	}

	@Override
	public NodeInfo visit(Field x) throws Err {
		sprintln("Visit Field expression: " + x.toString());
		return new NodeInfo("??? /* Field */");
	}
	
	@Override
	public NodeInfo visit(ExprUnary x) throws Err {
		sprintln("Visit ExprUnary expression: " + x.toString());
		return new NodeInfo("??? /* ExprUnary */");
	}	

	@Override
	public NodeInfo visit(ExprBinary x) throws Err {
		sprintln("Visit ExprBinary expression: " + x.toString());
		return new NodeInfo("??? /* ExprBinary */");
	}
	
	
	@Override
	public NodeInfo visit(ExprITE x) throws Err {
		sprintln("Visit ExprITE expression: " + x.toString());
		return new NodeInfo("??? /* ExprITE */");
	}
	
	@Override
	public NodeInfo visit(ExprLet x) throws Err {
		sprintln("Visit ExprLet expression: " + x.toString());
		return new NodeInfo("??? /* ExprLet */");
	}	
	
	@Override
	public NodeInfo visit(ExprList x) throws Err {
		sprintln("Visit ExprList expression: " + x.toString());
		return new NodeInfo("??? /* ExprList */");
	}
	
	@Override
	public NodeInfo visit(ExprVar x) throws Err {
		sprintln("Visit ExprVar expression: " + x.toString());
		return new NodeInfo("??? /* ExprVar */");
	}
	
	@Override
	public NodeInfo visit(ExprQt x) throws Err {
		sprintln("Visit ExprQt expression: " + x.toString());
		return new NodeInfo("??? /* ExprQt */");
	}

	private void sprintln(Object s){
		String idents = "";
		for(int i = 0; i < ident; i++)
			idents += "  ";
		
		System.out.print(idents + s + "\r\n");
	}
}

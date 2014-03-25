package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.PrintWriter;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4compiler.ast.Browsable;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBad;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprCall;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprITE;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprLet;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprList;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprQt;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Type;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;

public class Visitor extends VisitQuery<Object> {
	
	private PrintWriter out;

	public Visitor(PrintWriter out) throws Exception{
		if(out == null)
			throw new Exception("ArgumentNullException: out");
		
		this.out = out;
		System.out.println("Init Visistor with PrintWriter");
	}
	
	@Override
	public Object visit(Sig x) throws Err {
		String name = x.label.substring(5);
		out.println("public"
				+ ((x.isAbstract != null) ? " abstract" : "")
				+ (" class " + name)
				
				+ "{");
		
		SafeList<Decl> decls = x.getFieldDecls();
		System.out.println("Visit Sig: " + x.label + " (" + decls.size() + " field declarations)");

		for(Decl decl : decls){
			System.out.println(" Field Declaration: " + decl.names + " (" + decl.names.size() + " fields)");
			decl.expr.accept(this);
		}
		
		out.println("}");
		return x;
	}
	
	@Override
	public Object visit(Field x) throws Err {
		System.out.println("  Visit field expression: " + x.label);
		
		out.print(x.label);
		
		return x;
	}
	
	@Override
	public Object visit(ExprBad x) throws Err {
		System.out.println("  Visit bad code.");
		out.println("  // Illegal code: " + x.toString());
		return x;
	}
	
	@Override
	public Object visit(ExprConstant x) throws Err {
		System.out.println("  Visit constant expression of type " + x.type());
		
		if(x.type().equals(ExprConstant.Op.NUMBER)){
			out.print(x.num());
		}else if(x.type().equals(ExprConstant.Op.FALSE)){
			out.print("false");
		}else if(x.type().equals(ExprConstant.Op.TRUE)){
			out.print("true");
		}else if(x.type().equals(ExprConstant.Op.STRING)){
			out.print(x.string);
		}else if(x.type().equals(ExprConstant.Op.EMPTYNESS)){
			out.print("null");
		}else if(x.type().equals(ExprConstant.Op.NEXT)){
			out.print("++");
		}else if(x.type().equals(ExprConstant.Op.MAX)){
			out.print("Int32.MaxValue");
		}else if(x.type().equals(ExprConstant.Op.MIN)){
			out.print("Int32.MinValue");
		}else{
			out.print("! ECONSTTYPE: " + x.type());
		}
		
		return x;
	}
	
	@Override
	public Object visit(ExprCall x) throws Err {
		System.out.println("  Visit call expression: " + x.toString());
		out.print("[ CALL EXPRESSION]");
		return x;
	}
	
	@Override
	public Object visit(ExprUnary x) throws Err {
		System.out.println("  Visit unary expression with OP: '" + x.op + "' and sub: " + x.sub.toString());
		
		switch(x.op){ 
			default:
				System.out.println("  ! EOP: Unkown OP");
				break;
		
		}
		
		out.print(x.op);
		
		x.sub.accept(this);
		
		return x;
	}	
	
	@Override
	public Object visit(ExprITE x) throws Err {
		System.out.println("  Visit IF-ELSE expression");
		
		out.print("  if (");
		
			x.cond.accept(this);
		
		out.println("){");
		
			x.left.accept(this);
		
		out.println("  }else{");
		
			x.right.accept(this);
		
		out.println("  }");
		
		return x;
	}
	
	@Override
	public Object visit(ExprLet x) throws Err {
		System.out.println("  Visit let expression");
		out.println("  TO DO: 'Let' Expressions");
		return x;
	}	
	
	@Override
	public Object visit(ExprList x) throws Err {
		System.out.println("  Visit List expression.");
		
		switch(x.op){
		case TOTALORDER:
			out.print("[]");
			break;
		default:
			out.print("!EOP:ExprList:" + x.op + "!");
			break;
		}
		
		out.print(x.args.toString());
		
		return x;
	}
	
	@Override
	public Object visit(ExprVar x) throws Err {
		System.out.println("  Visit Variable expression: " + x.toString());
		
		out.print(x.type() + " " + x.label + " = ");
		
		return x;
	}
	
	@Override
	public Object visit(ExprQt x) throws Err {
		System.out.println("  Visit quantified expression: " + x.toString());
		out.println("!ENOTIMPL:QT:" + x.toString() + "!");
		return x;
	}
	
	@Override
	public Object visit(ExprBinary x) throws Err {
		System.out.println("  Visit binary expression.");
		
		//out.print("(");
		
		x.left.accept(this);
		
		switch (x.op) {
			case MINUS:	
					out.print("-");
					break;
			case PLUS:
					out.print("+");
					break;
			case AND:
					out.print("&&");
					break;
			case DIV:
					out.print("/");
					break;
			case GT:
					out.print(">");
					break;
			case GTE:
					out.print(">=");
					break;
			case LT:
					out.print("<");
					break;
			case LTE:
					out.print("<=");
					break;
			default:
					out.print("(EOP: \"" + x.op + "\")");
					break;
		}
		
		x.right.accept(this);
		
		//out.println(")");
		
		return x;
	}
}

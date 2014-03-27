package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.SafeList;
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
import edu.mit.csail.sdg.alloy4compiler.ast.Type;
import edu.mit.csail.sdg.alloy4compiler.ast.Type.ProductType;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;

public class Visitor extends VisitQuery<DefAndInvariants> {
	
	private PrintWriter out;
	int ident = 0;
	public Visitor(PrintWriter out) throws Exception{
		if(out == null)
			throw new Exception("ArgumentNullException: out");
		
		this.out = out;
		System.out.println("Init Visistor with PrintWriter");
	}
	
	
	@Override
	public DefAndInvariants visit(Sig x) throws Err {		
		String name = x.label.substring(5);
		
		StringBuilder s = new StringBuilder();
		
		s.append("public"
				+ ((x.isAbstract != null) ? " abstract" : "")
				+ (" class " + name)
				
				+ " {\r\n");
		
		SafeList<Decl> decls = x.getFieldDecls();
		System.out.println("* Visit Sig: " + x.label + " (" + decls.size() + " field declarations)");
		
		ident++;
		
		ArrayList<String> invariants = new ArrayList<String>();
		for(Decl decl : decls){
			System.out.println("\r\n Field Declaration: " + decl.names + " (" + decl.names.size() + " fields)");
			DefAndInvariants defAndInvariants = (DefAndInvariants)decl.expr.accept(this);
			
			for(ExprHasName n : decl.names){
				s.append("  public " + defAndInvariants.def + " " + n.label + ";\r\n"); 
				
				for(String inv : defAndInvariants.invariants){
					String finalInvariant = inv.replace("{def}", n.label);
					System.out.print("  Transforming '" + inv + "' to '" + finalInvariant + "'\r\n");
					invariants.add(finalInvariant);
				}
			}
		}

		s.append("\r\n");

		if(invariants.size() > 0){
			System.out.println("  Printing Invariants");
			s.append("  [ContractInvariantMethod]\r\n");
			s.append("  private void ObjectInvariant() {\r\n");
			for(String inv : invariants){
				s.append("    Contracts.Invariant(" + inv + ");\r\n");
			}
			s.append("  }\r\n");
		}
		invariants.clear();
		
		// Singleton
		if(x.isOne != null || x.isLone != null){
			s.append("  private static " + name + " instance;\r\n");
			s.append("  private " + name + "() {}\r\n");
			s.append("  public static " + name + " Instance {\r\n");
			ident++;
				s.append("    get{\r\n");
				ident++;
					s.append("      if(instance == null) instance = new " + name + "();\r\n");
					s.append("      return instance;\r\n");
				ident--;
				s.append("    }\r\n");
			ident--;
			s.append("  }\r\n");
		}
		
		ident--;
		
		s.append("}\r\n\r\n");
		System.out.println("* Sig visit completed!");
		return new DefAndInvariants(s.toString());
	}
	
	@Override
	public DefAndInvariants visit(ExprBad x) throws Err {
		sprintln("Visit bad code.");
		println("  // Illegal code: " + x.toString());
		return null;
	}
	
	@Override
	public DefAndInvariants visit(ExprConstant x) throws Err {
		sprintln("Visit constant expression of type " + x.type());
		
		if(x.type().equals(ExprConstant.Op.NUMBER)){
			print(x.num());
		}else if(x.type().equals(ExprConstant.Op.FALSE)){
			print("false");
		}else if(x.type().equals(ExprConstant.Op.TRUE)){
			print("true");
		}else if(x.type().equals(ExprConstant.Op.STRING)){
			print(x.string);
		}else if(x.type().equals(ExprConstant.Op.EMPTYNESS)){
			print("null");
		}else if(x.type().equals(ExprConstant.Op.NEXT)){
			print("++");
		}else if(x.type().equals(ExprConstant.Op.MAX)){
			print("Int32.MaxValue");
		}else if(x.type().equals(ExprConstant.Op.MIN)){
			print("Int32.MinValue");
		}else{
			print("! ECONSTTYPE: " + x.type());
		}
		
		return null;
	}
	
	@Override
	public DefAndInvariants visit(ExprCall x) throws Err {
		sprintln("Visit call expression: " + x.toString());
		print("[ CALL EXPRESSION]");
		return null;
	}

	
	@Override
	public DefAndInvariants visit(Field x) throws Err {
		sprintln("Visit field expression: " + x);
		
		DefAndInvariants s;
		
		Expr e = x.type().toExpr();
		if(e instanceof ExprBinary)
			if(((ExprBinary) e).right instanceof Sig){
				s = new DefAndInvariants(((Sig)((ExprBinary)e).right).label.substring(5));
			}else{
				s = ((ExprBinary)e).right.accept(this);
			}
		else
			s = new DefAndInvariants("Unknown Field Expression");
		
		sprintln("Field Expression returning: " + s);
		return s;
	}
	
	@Override
	public DefAndInvariants visit(ExprUnary x) throws Err {
		sprintln("Visit unary expression ('" + x.toString() + "') with OP: '" + x.op + "' (" + x.op.name() + ") and sub: " + x.sub.toString() + ", type: " + x.type());
		
		DefAndInvariants ret = new DefAndInvariants();
		DefAndInvariants t;
		ident++;
		switch(x.op){ 
			case ONEOF:
				ret.invariants.add("{def} != null");
			case LONEOF:
				t = x.sub.accept(this);
				ret.def = t.def;
				t.invariants.addAll(t.invariants);
				break;
			case SETOF:
				t = (DefAndInvariants) x.sub.accept(this);
				ret.def += "ISet<" + t.def + ">";
				ret.invariants.addAll(t.invariants);
				break;
			case NOOP:
				if(x.sub instanceof Sig)
					ret.def = ((Sig)x.sub).label.substring(5); // No invariant can be extracted here
				else{
					t = (DefAndInvariants) x.sub.accept(this);
					ret.def = t.def;
					ret.invariants.addAll(t.invariants);
				}
				break;
			default:
				println(x.op.name() + "[" + x.sub.accept(this) + "]");
				ret.def = "?";
				break;
		
		}
		ident--;
		sprintln("Unary Expression returning '" + ret.def + "' with " + ret.invariants.size() + " invariants: " + ret.invariants);
		
		return ret;
	}	

	@Override
	public DefAndInvariants visit(ExprBinary x) throws Err {
		sprintln("Visit binary expression (OP=" + x.op.name() + ", '" + x.op + "' ) [" + x + "]");
		
		StringBuilder s = new StringBuilder();
		
		ident++;
		switch (x.op) {
			case ARROW: // "->" (Set Tuples)
				s.append("ISet<Tuple<");
				s.append(x.left.accept(this));
				s.append(", ");
				s.append(x.right.accept(this));
				s.append(">>");
				break;
			case ANY_ARROW_LONE: // "-> lone" (Tuple)
				s.append("Map<");
				s.append(x.left.accept(this));
				s.append(", ");
				s.append(x.right.accept(this));
				s.append(">");
				break;
			case JOIN:
				s.append(x.right.accept(this));
				break;
			default:
				s.append(x.toString());
				break;
		}
		ident--;
		sprintln("Binary Expression returning '" + s + "'");
		return new DefAndInvariants(s.toString());
	}
	
	
	@Override
	public DefAndInvariants visit(ExprITE x) throws Err {
		sprintln("Visit IF-ELSE expression");
		
		print("  if (");
		
			x.cond.accept(this);
		
		println("){");
		
			x.left.accept(this);
		
		println("  }else{");
		
			x.right.accept(this);
		
		println("  }");
		
		return null;
	}
	
	@Override
	public DefAndInvariants visit(ExprLet x) throws Err {
		sprintln("Visit let expression");
		println("  TO DO: 'Let' Expressions");
		return null;
	}	
	
	@Override
	public DefAndInvariants visit(ExprList x) throws Err {
		sprintln("Visit List expression.");
		
		switch(x.op){
			default:
				print("!EOP:ExprList:" + x.op + "!");
				break;
		}
		
		print(x.args.toString());
		
		return null;
	}
	
	@Override
	public DefAndInvariants visit(ExprVar x) throws Err {
		sprintln("Visit Variable expression: " + x.toString());
		
		print(x.type() + " " + x.label + " = ");
		
		return null;
	}
	
	@Override
	public DefAndInvariants visit(ExprQt x) throws Err {
		sprintln("Visit quantified expression: " + x.toString());
		println("!ENOTIMPL:QT:" + x.toString() + "!");
		return null;
	}
	

	private void sprintln(Object s){
		String idents = "";
		for(int i = 0; i < ident; i++)
			idents += "  ";
		
		System.out.print(idents + s + "\r\n");
	}
	
	private void print(Object s){
		String idents = "";
		for(int i = 0; i < ident; i++)
			idents += "  ";
		
		out.print(idents + s);
		System.out.print(">" + idents + s);
	}
	
	private void println(String s){
		print(s + "\r\n");
	}
}

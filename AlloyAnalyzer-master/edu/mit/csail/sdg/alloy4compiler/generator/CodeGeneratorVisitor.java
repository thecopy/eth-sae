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

public class CodeGeneratorVisitor extends VisitQuery<DefAndInvariants> {
	
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
	public DefAndInvariants visit(Sig x) throws Err{		
		String name = x.label.substring(5);
		String parentName = null;
		
		if(x instanceof PrimSig){
			PrimSig parent = ((PrimSig)x).parent;
			if(parent != null && !parent.builtin){
				parentName = parent.label.substring(5);
				if(parentName.equals("Object"))
					sprintln("***** TODO: Change so type name cannot be 'Object'");
			}
		}
		
		StringBuilder s = new StringBuilder();
		
		s.append(((x.isAbstract != null) ? "abstract " : "")
				+ "public"
				+ (" class " + name)
				+ ((parentName != null) ? (" : " + parentName) : "")
				+ " {\r\n");
		
		SafeList<Decl> decls = x.getFieldDecls();
		System.out.println("* Visit Sig: " + x.label + " (" + decls.size() + " field declarations)");
		System.out.println("* Sig is PrimSig = " + (x instanceof PrimSig)
				+ ". Sig is SubSetSig: " + (x instanceof SubsetSig));
		ident++;
		
		ArrayList<String> invariants = new ArrayList<String>();
		for(Decl decl : decls){
			System.out.println("\r\n Field Declaration: " + decl.names + " (" + decl.names.size() + " fields)");
			DefAndInvariants defAndInvariants = (DefAndInvariants)decl.expr.accept(this);
			
			for(ExprHasName n : decl.names){
				s.append("  public " + defAndInvariants.def + " " + n.label + ";\r\n"); 
				
				StringBuilder invariantAggregated = new StringBuilder();
				for(String inv : defAndInvariants.invariants){
					if(inv.isEmpty()) continue;
					
					String finalInvariant = inv.replace("{def}", n.label);
					System.out.print("  Transforming '" + inv + "' to '" + finalInvariant + "'\r\n");
					
					String before = "";
					if(invariantAggregated.length() != 0)
						before = " && ";
					
					invariantAggregated.append(before + finalInvariant);
				}
				
				invariants.add(invariantAggregated.toString());
			}
		}

		if(invariants.size() > 0){
			System.out.println("  Printing Invariants");
			s.append("\r\n");
			s.append("  [ContractInvariantMethod]\r\n");
			s.append("  private void ObjectInvariant() {\r\n");
			for(String inv : invariants){
				if(false == inv.isEmpty())
				s.append("    Contract.Invariant(" + inv + ");\r\n");
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
		return new DefAndInvariants("??? /* ExprBad */");
	}
	
	@Override
	public DefAndInvariants visit(ExprConstant x) throws Err {
		sprintln("Visit constant expression of type " + x.type());
		DefAndInvariants ret = new DefAndInvariants();
		ret.def = "?";
		if(x.type().equals(ExprConstant.Op.NUMBER)){
		}else if(x.type().toString().equals("{PrimitiveBoolean}")){
			ret.def = "bool";
		}else if(x.type().equals(ExprConstant.Op.FALSE)){
			ret.def = "false";
		}else if(x.type().equals(ExprConstant.Op.TRUE)){
			ret.def = "true";
		}else if(x.type().equals(ExprConstant.Op.STRING)){
			ret.def = "string";
		}else if(x.type().equals(ExprConstant.Op.EMPTYNESS)){

		}else if(x.type().equals(ExprConstant.Op.NEXT)){

		}else if(x.type().equals(ExprConstant.Op.MAX)){

		}else if(x.type().equals(ExprConstant.Op.MIN)){

		}else{
			print("! ECONSTTYPE: " + x.type());
		}
		
		return ret;
	}
	
	@Override
	public DefAndInvariants visit(ExprCall x) throws Err {
		sprintln("Visit call expression: " + x.toString());
		print("[ CALL EXPRESSION]");
		return new DefAndInvariants("??? /* ExprCall */");
	}

	
	@Override
	public DefAndInvariants visit(Field x) throws Err {
		sprintln("Visit field expression: " + x);
		
		DefAndInvariants s;
		
		Expr e = x.type().toExpr();
		if(e instanceof ExprBinary)
			if(((ExprBinary) e).right instanceof Sig){
				sprintln("Field is Binary Expression with right expression of type Sig");
				Sig signature = (Sig)((ExprBinary)e).right;
				s = new DefAndInvariants(signature.label.substring(5));
			}else{
				s = ((ExprBinary)e).right.accept(this);
			}
		else
			s = new DefAndInvariants("Unknown Field Expression");

		s.invariants.add(x.label + " != null");
		s.extra = x.label;
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
				ret.invariants.addAll(t.invariants);
				ret.extra = t.extra;
				break;
			case SETOF:
				t = (DefAndInvariants) x.sub.accept(this);
				ret.def += "ISet<" + t.def + ">";
				ret.invariants.add("{def} != null");				
				ret.invariants.add("Contract.ForAll({def}, e => e != null)");
				ret.invariants.addAll(t.invariants);
				ret.extra = t.extra;
				break;
			case NOOP:
				if(x.sub instanceof Sig)
					ret.def = ((Sig)x.sub).label.substring(5); // No invariant can be extracted here
				else{
					t = (DefAndInvariants) x.sub.accept(this);
					ret.def = t.def;
					ret.invariants.addAll(t.invariants);
					ret.extra = t.extra;
				}
				break;
			default:
				ret.def = "??? /*ExprUnary. Unkown Operator Type: \"" + x.op + "\" (" + x.op.name() + ")*/";
				break;
		
		}
		ident--;
		
		sprintln("Unary Expression returning " + ret);
		
		return ret;
	}	

	@Override
	public DefAndInvariants visit(ExprBinary x) throws Err {
		sprintln("Visit binary expression (OP=" + x.op.name() + ", '" + x.op + "' ) [" + x + "]");
		
		StringBuilder s = new StringBuilder();
		DefAndInvariants ret = new DefAndInvariants();
		
		ident++;
		switch (x.op) {
			case ARROW: // "->" (Set Tuples)
				DefAndInvariants left = x.left.accept(this);
				DefAndInvariants right = x.right.accept(this);
				sprintln("Got left: " + left);
				sprintln("Got right: " + right);
				s.append("ISet<Tuple<");
				s.append(left.def);
				s.append(", ");
				s.append(right.def);
				s.append(">>");
				
				ret.invariants.add("{def} != null");
				
				if(left.extra != null && !left.extra.isEmpty()){
					ret.invariants.add("Contract.ForAll({def}, e => e != null && e.Item1.Equals(" + left.extra + "))");
				}
				if(right.extra != null && !right.extra.isEmpty()){
					ret.invariants.add("Contract.ForAll({def}, e => e != null && e.Item2.Equals(" + right.extra + "))");
				}
								
				ret.def = s.toString();
				break;
			case ANY_ARROW_LONE: // "-> lone" (Tuple) (Lone can be null (0 or 1))
				left = x.left.accept(this);
				right = x.right.accept(this);
				s.append("Map<");
				s.append(left.def);
				s.append(", ");
				s.append(right.def);
				s.append(">");
				
				ret.def = s.toString();
				ret.invariants.add("Contract.ForAll({def}, e => e != null)");
				ret.invariants.addAll(left.invariants);
				ret.invariants.addAll(right.invariants);
				break;
			case JOIN:
				ret.join(x.right.accept(this));
				break;
			default:
				ret.def = "??? /*ExprBinary Unkown Operator Type: \"" + x.op + "\" (" + x.op.name() + ")*/";
				break;
		}
		ident--;
		sprintln("Binary Expression returning " + ret );
		return ret;
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
		
		return new DefAndInvariants("??? /* ExprITE */");
	}
	
	@Override
	public DefAndInvariants visit(ExprLet x) throws Err {
		sprintln("Visit let expression");
		println("  TO DO: 'Let' Expressions");
		return new DefAndInvariants("??? /* ExprLet */");
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
		
		return new DefAndInvariants("??? /* ExprList */");
	}
	
	@Override
	public DefAndInvariants visit(ExprVar x) throws Err {
		sprintln("Visit Variable expression: " + x.toString());
		
		print(x.type() + " " + x.label + " = ");
		
		return new DefAndInvariants("??? /* ExprVar */");
	}
	
	@Override
	public DefAndInvariants visit(ExprQt x) throws Err {
		sprintln("Visit quantified expression: " + x.toString());
		println("!ENOTIMPL:QT:" + x.toString() + "!");
		return new DefAndInvariants("??? /* ExprQt */");
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

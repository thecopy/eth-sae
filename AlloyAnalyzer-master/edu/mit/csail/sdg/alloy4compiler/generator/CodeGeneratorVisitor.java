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
		
		ArrayList<String> invariants = new ArrayList<String>();
		for(Decl decl : decls){
			System.out.println("\r\n Field Declaration: " + decl.names + " (" + decl.names.size() + " fields)");
			NodeInfo defAndInvariants = (NodeInfo)decl.expr.accept(this);
			
			for(ExprHasName n : decl.names){
				s.append("  public " + defAndInvariants.typeName + " " + n.label + ";\r\n"); 
				
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
		
		
		s.append("}\r\n\r\n");
		System.out.println("* Sig visit completed!");

		ident--;
		return new NodeInfo(s.toString());
	}
	
	@Override
	public NodeInfo visit(ExprBad x) throws Err {
		sprintln("Visit bad code.");
		sprintln("  // Illegal code: " + x.toString());
		return new NodeInfo("??? /* ExprBad */");
	}
	
	@Override
	public NodeInfo visit(ExprConstant x) throws Err {
		ident++;
		sprintln("Visit constant expression of type " + x.type());
		NodeInfo ret = new NodeInfo();
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
		sprintln("Type have arity " + x.type().arity());
		if(e instanceof ExprBinary){
			ExprBinary bin = (ExprBinary)e;
			if(x.type().arity() == 2){
				s.typeName = ((Sig)bin.right).label.substring(5);
				s.sig = (PrimSig)bin.right;
			}
			else{ 
				// Arity = 3 since we arent supposed to support higher
				// So assume left is another binary with.
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
				
				s.typeName = "Tuple<";
				s.typeName += left.typeName;
				s.typeName += ",";
				s.typeName += right.typeName;
				s.typeName += ">";
			}
		}else{
			s = new NodeInfo("Unknown Field Expression");
		}
		
		s.invariants.add(x.label + " != null");
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
				ret.invariants.add("{def} != null");
			case LONEOF:
				t = x.sub.accept(this);
				ret.typeName = t.typeName;
				ret.invariants.addAll(t.invariants);
				ret.fieldName = t.fieldName;
				break;
			case SETOF:
				t = (NodeInfo) x.sub.accept(this);
				ret.typeName += "ISet<" + t.typeName + ">";
				ret.invariants.add("{def} != null");				
				ret.invariants.add("Contract.ForAll({def}, e => e != null)");
				ret.invariants.addAll(t.invariants);
				ret.fieldName = t.fieldName;
				break;
			case NOOP:
				if(x.sub instanceof Sig){
					ret.typeName = ((Sig)x.sub).label.substring(5); // No invariant can be extracted here
					ret.sig = (PrimSig)x.sub;
				}else{
					t = (NodeInfo) x.sub.accept(this);
					ret.typeName = t.typeName;
					ret.invariants.addAll(t.invariants);
					ret.fieldName = t.fieldName;
					ret.csharpCode = t.csharpCode;
				}
				break;
			case CLOSURE:
				t = x.sub.accept(this);
				ret.typeName = "Object";
				ret.csharpCode = "Helper.Closure(" + t.fieldName + ")";
				break;
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
		sprintln("Visit binary expression (OP=" + x.op.name() + ", '" + x.op + "' ) [" + x + "]");
		
		StringBuilder s = new StringBuilder();
		NodeInfo ret = new NodeInfo();
		
		switch (x.op) {
			case ARROW: // "->" (Set Tuples)
				s.append("ISet<Tuple<");
				
				if(x.left instanceof Sig){
					sprintln("Left is of type Sig. Will not visit!");
					s.append(((Sig)x.left).label.substring(5));
				}else{
					sprintln("Visiting left, type " + x.left.type() + "(" + x.left.getClass() + ")");
					NodeInfo left = x.left.accept(this);
					s.append(left.typeName);
					if(left.fieldName != null && !left.fieldName.isEmpty()){
						ret.invariants.add("Contract.ForAll({def}, e => e != null && e.Item1.Equals(" + left.fieldName + "))");
					}
				}
				s.append(", ");
				if(x.right instanceof Sig){
					sprintln("Right is of type Sig. Will not visit!");
					s.append(((Sig)x.right).label.substring(5));

				}else{
					sprintln("Visiting right, type " + x.right.type() + "(" + x.right.getClass() + ")");
					NodeInfo right = x.right.accept(this);
					s.append(right.typeName);
					
					if(right.fieldName != null && !right.fieldName.isEmpty()){
						ret.invariants.add("Contract.ForAll({def}, e => e != null && e.Item2.Equals(" + right.fieldName + "))");
					}
				}
				s.append(">>");
				
				ret.invariants.add("{def} != null");								
				ret.typeName = s.toString();
				break;
			case ANY_ARROW_LONE: // "-> lone" (Tuple) (Lone can be null (0 or 1))
				NodeInfo left = x.left.accept(this);
				NodeInfo right = x.right.accept(this);
				s.append("Map<");
				s.append(left.typeName);
				s.append(", ");
				s.append(right.typeName);
				s.append(">");
				
				ret.typeName = s.toString();
				ret.invariants.add("Contract.ForAll({def}, e => e != null)");
				ret.invariants.addAll(left.invariants);
				ret.invariants.addAll(right.invariants);
				break;
			case ONE_ARROW_ONE: // Tuple
				left = x.left.accept(this);
				right = x.right.accept(this);
				s.append("Tuple<");
				s.append(left.typeName);
				s.append(",");
				s.append(right.typeName);
				s.append(">");
				
				ret.typeName = s.toString();
				ret.invariants.add("{def} != null");
				ret.invariants.addAll(left.invariants);
				ret.invariants.addAll(right.invariants);
				break;
			case PLUS: // +, or logical disjunction A (V) B
				left = x.left.accept(this);
				right = x.right.accept(this);
				
				String typeName = ASPHelper.findFirstCommonClass(left.sig, right.sig).typeName;
				
				if(left.typeName.equals(right.typeName))
					typeName = left.typeName;
				
				s.append("ISet<");
				s.append(typeName);
				s.append(">");

				ret.typeName = s.toString();
				ret.invariants.add("{def} != null");
				ret.invariants.addAll(left.invariants);
				ret.invariants.addAll(right.invariants);
				
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
			default:
				ret.typeName = "Object /*ExprBinary Unkown Operator Type: \"" + x.op + "\" (" + x.op.name() + ")*/";
				break;
		}
		sprintln("Binary Expression returning " + ret );
		ident--;
		return ret;
	}
	
	
	@Override
	public NodeInfo visit(ExprITE x) throws Err {
		sprintln("Visit IF-ELSE expression");
		
		sprintln("  if (");
		
			x.cond.accept(this);
		
		sprintln("){");
		
			x.left.accept(this);
		
		sprintln("  }else{");
		
			x.right.accept(this);
		
		sprintln("  }");
		
		return new NodeInfo("??? /* ExprITE */");
	}
	
	@Override
	public NodeInfo visit(ExprLet x) throws Err {
		sprintln("Visit let expression");
		sprintln("  TO DO: 'Let' Expressions");
		return new NodeInfo("??? /* ExprLet */");
	}	
	
	@Override
	public NodeInfo visit(ExprList x) throws Err {
		sprintln("Visit List expression.");
		
		switch(x.op){
			default:
				sprintln("!EOP:ExprList:" + x.op + "!");
				break;
		}
		
		sprintln(x.args.toString());
		
		return new NodeInfo("??? /* ExprList */");
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

package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprHasName;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.SubsetSig;
import edu.mit.csail.sdg.alloy4compiler.generator.CodeGeneratorVisitor;
import edu.mit.csail.sdg.alloy4compiler.generator.InvariantDescriptor.InvariantConstraint;
import edu.mit.csail.sdg.alloy4compiler.generator.NodeInfo;

public final class CodeGenerator {

  private CodeGenerator(
		  Iterable<Sig> sigs, SafeList<Func> funcs, 
		  String originalFilename, PrintWriter out, 
		  boolean checkContracts) throws Exception, Err {

	  System.out.println(" ** Got to constructor of CodeGenerator");
	  System.out.println(" ** Got sigs:  " + sigs);
	  System.out.println(" ** Got funcs:  " + funcs);
	  System.out.println(" ** Got originalFilename:  " + originalFilename);
	  
	  out.println("// This C# file is generated from " + originalFilename + "\r\n");
	  out.println("#undef CONTRACTS_FULL\r\n");
	  
	  out.println("using System;\r\n"
			  	 +"using System.Linq;\r\n"
			  	 +"using System.Collections.Generic;\r\n"
			  	 +"using System.Diagnostics.Contracts;\r\n");
	  
	  CodeGeneratorVisitor v = new CodeGeneratorVisitor(out);
	  for(Sig sig : sigs){
		  if(sig.label.equals("univ") || sig.label.equals("Int")
				  || sig.label.equals("seq/Int")
				  || sig.label.equals("none")
				  || sig.label.equals("String")){
			  
			  continue; // these are hard coded sigs. We dont care about those
		  }
		  
		  String name = sig.label.substring(5);
			String parentName = null;
			
			if(sig instanceof PrimSig){
				PrimSig parent = ((PrimSig)sig).parent;
				if(parent != null && !parent.builtin){
					parentName = parent.label.substring(5);
					if(parentName.equals("Object"))
						System.out.println("***** TODO: Change so type name cannot be 'Object'");
				}
			}
			
			StringBuilder s = new StringBuilder();
			
			s.append(((sig.isAbstract != null) ? "abstract " : "")
					+ "public"
					+ (" class " + name)
					+ ((parentName != null) ? (" : " + parentName) : "")
					+ " {\r\n");
			
			SafeList<Decl> decls = sig.getFieldDecls();
			System.out.println("* Visit Sig: " + sig.label + " (" + decls.size() + " field declarations)");
			
			ArrayList<String> invariants = new ArrayList<String>();
			for(Decl decl : decls){
				System.out.println("\r\n Field Declaration: " + decl.names + " (" + decl.names.size() + " fields, mult=" + decl.expr.mult +")");
				NodeInfo defAndInvariants = (NodeInfo)decl.expr.accept(v);
				
				String finalTypeName = defAndInvariants.typeName;
				
				for(ExprHasName n : decl.names){
					
					s.append("  public " + finalTypeName + " " + n.label + ";\r\n"); 
					
					StringBuilder invariantAggregated = new StringBuilder();
					for(InvariantDescriptor inv : defAndInvariants.invariants){
						if(inv.invariant.isEmpty()) continue; // dont want empty invariants
						
						// If invariant is only for sets, check that this is a set
						if(inv.invariantConstraint.equals(InvariantConstraint.SET_ONLY)){
							
							if(false == (decl.expr.deNOP() instanceof ExprUnary))
									continue; // cannot be set if not ExprUnary.op = SET/SOME
							
							ExprUnary unaryExpr =(ExprUnary )decl.expr.deNOP();
							// If the unary op is not declaring a set or some, then this is not a set, go to the next invariant
							if(false == (unaryExpr.op.equals(ExprUnary.Op.SOMEOF) ||	unaryExpr.op.equals(ExprUnary.Op.SETOF))){
								continue;
							}
						}
						
						// If invariant is only when not a set, check that this is not a set
						if(inv.invariantConstraint.equals(InvariantConstraint.NONSET_ONLY)){
							
							if(decl.expr.deNOP() instanceof ExprUnary){
								
								ExprUnary unaryExpr =(ExprUnary )decl.expr.deNOP();		
								// If the unary op is declaring a set or some, go to the next invariant
								if(unaryExpr.op.equals(ExprUnary.Op.SOMEOF) ||	unaryExpr.op.equals(ExprUnary.Op.SETOF)){
									continue;
								}
								
							}
						}
						
						String finalInvariant = inv.invariant.replace("{def}", n.label);
						System.out.print("  Transforming '" + inv + "' to '" + finalInvariant + "'\r\n");
						
						String before = "";
						if(invariantAggregated.length() != 0)
							before = " && ";
						
						invariants.add(finalInvariant);
						
						invariantAggregated.append(before + finalInvariant);
					}
					
					//invariants.add(invariantAggregated.toString());
				}
			}

			if(invariants.size() > 0){
				System.out.println("  Printing Invariants");
				s.append("\r\n");
				s.append("  [ContractInvariantMethod]\r\n");
				s.append("  private void ObjectInvariant() {\r\n");
				for(String inv : new HashSet<String>(invariants)){
					if(false == inv.isEmpty())
					s.append("    Contract.Invariant(" + inv + ");\r\n");
				}
				s.append("  }\r\n");
			}
			invariants.clear();
			
			// Singleton
			if(sig.isOne != null || sig.isLone != null){
				s.append("  private static " + name + " instance;\r\n");
				s.append("  private " + name + "() {}\r\n");
				s.append("  public static " + name + " Instance {\r\n");
				s.append("    get{\r\n");
				s.append("      if(instance == null) instance = new " + name + "();\r\n");
				s.append("      return instance;\r\n");
				s.append("    }\r\n");
				s.append("  }\r\n");
			}
			
			
			s.append("}\r\n\r\n");
			System.out.println("* Sig visit completed!");
			out.print(s.toString());
	  }
	  
	  System.out.println("  * Handling Functions");
	  out.print("public static class FuncClass {\r\n");
	  for(Func func : funcs){
		  System.out.println("  ** Parsing function " + func.label.substring(5));
		  System.out.println("  *** Resolving function return type...");
		  NodeInfo returnType = func.returnDecl.accept(v);

		  out.print("  public static " + returnType.typeName + " " + func.label.substring(5) + " (");

		  System.out.println("  *** Resolving function parameters...");
		  ArrayList<String> requires = new ArrayList<String>();
		  boolean first = true;
		  for(Decl decl : func.decls){
			  NodeInfo d = decl.expr.accept(v);
			  for(ExprHasName name : decl.names){
				  if(!first)
					  out.print(", ");
				  out.print(d.typeName + " " + name.label);
				  first = false;

				  for(InvariantDescriptor inv : d.invariants)
					  requires.add(inv.invariant.replace("{def}", name.label));
			  }
		  }
		  out.print("){\r\n");
		  
		  for(String req : requires){
			  out.print("    Contract.Requires(" + req + ");\r\n");
		  }
		  for(InvariantDescriptor ensure : returnType.invariants){
			  out.print("    Contract.Ensures(" + 
					  ensure.invariant.replace("{def}", "Contract.Result<" + returnType.typeName + ">()")
					  + ");\r\n");
		  }
		  
		  System.out.println("  *** Resolving function body...");
		  NodeInfo body = func.getBody().accept(v);
		  out.print("\r\n");
		  if(body.csharpCode == null || body.csharpCode.isEmpty()){
			  out.print("    return " + body.fieldName + ";");
		  }else{
			  out.print("    return " + body.csharpCode + ";");
		  }
		  out.print("\r\n  }\r\n");
	  }
	  out.print("}\r\n");
	  
	  
	  System.out.println("  * Printing Helper Class");
	  out.print("public static class Helper {\r\n");
	  out.print("  public static ISet<Tuple<L, R>> Closure<L, R>(ISet<Tuple<L, R>> set) {\r\n");
	  out.print("    ISet<Tuple<L, R>> closure = new HashSet<Tuple<L, R>>();\r\n");
	  out.print("    Tuple<L,R>[] tuplesArray = new Tuple<L,R>[set.Count];\r\n");
	  out.print("    set.CopyTo(tuplesArray, 0);\r\n");
	  out.print("    foreach (Tuple<L, R> tup in set) {\r\n");
	  out.print("      L first = tup.Item1;\r\n");
	  out.print("      R second = tup.Item2;\r\n");
	  out.print("      closure.Add(new Tuple<L, R>(first, second));\r\n");
	  out.print("      for (int i = 0; i < tuplesArray.Length; i++) {\r\n");
	  out.print("        L left = tuplesArray[i].Item1;\r\n");
	  out.print("        if (second.Equals(left)) {\r\n");
	  out.print("          closure.Add(new Tuple<L, R>(first, tuplesArray[i].Item2));\r\n");
	  out.print("        }\r\n");
	  out.print("      }\r\n");
	  out.print("    }\r\n");
	  out.print("    if (closure.Count == set.Count) {\r\n");
	  out.print("      return closure;\r\n");
	  out.print("    }\r\n");
	  out.print("    else {\r\n");
	  out.print("      return Closure(closure);\r\n");
	  out.print("    }\r\n");
	  out.print("  }\r\n");
	  out.print("  public static ISet<Tuple<L, R>> RClosure<L, R>(ISet<Tuple<L, R>> set) {\r\n");
	  out.print("    ISet<Tuple<L, R>> closure = Closure(set);\r\n");
	  out.print("    foreach(Tuple<L, R> tup in set) {\r\n");
	  out.print("      L first = tup.Item1;\r\n");
	  out.print("      R second = tup.Item2;\r\n");
	  out.print("      Object temp1 = (Object)first;\r\n");
	  out.print("      Object temp2 = (Object)second;\r\n");
	  out.print("      L castedSecond = (L)temp2;\r\n");
	  out.print("      R castedFirst = (R)temp1;\r\n");
	  out.print("      closure.Add(new Tuple<L,R>(first,castedFirst));\r\n");
	  out.print("      closure.Add(new Tuple<L,R>(castedSecond,second));\r\n");
	  out.print("    }\r\n");
	  out.print("    return closure;\r\n");
	  out.print("  }\r\n");
	  out.print("}\r\n");
	  
	  out.flush();
	  out.close();
  }
  

  public static void writeCode(Module world, String originalFilename, boolean checkContracts, boolean saveInDist) throws Err {
    try {
      String f;
      String ext = ".cs";
      System.out.println("Save in dist: " + saveInDist);
      if (saveInDist) {
        f = ".\\" + new File(originalFilename).getName() + ext;
      }
      else {
        f = originalFilename + ext;
      }
      File file = new File(f);
      if (file.exists()) {
        file.delete();
      }
      System.out.println("Writes to " + f);
      PrintWriter out = new PrintWriter(new FileWriter(	f, true));
      new CodeGenerator(world.getAllReachableSigs(), world.getAllFunc(), originalFilename, out, checkContracts);
    }
    catch (Throwable ex) {
      if (ex instanceof Err) {
        throw (Err)ex;
      }
      else {
        throw new ErrorFatal("Error writing the generated C# code file.", ex);
      }
    }
  }
}

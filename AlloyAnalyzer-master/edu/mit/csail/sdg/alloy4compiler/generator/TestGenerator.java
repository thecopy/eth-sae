package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kodkod.instance.Tuple;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4compiler.ast.Browsable;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprHasName;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;
import edu.mit.csail.sdg.alloy4compiler.translator.A4TupleSet;

public final class TestGenerator {

  private TestGenerator(
		  A4Solution solution, 
		  Iterable<Sig> sigs, 
		  Iterable<Pair<String, Expr>> assertions, 
		  Iterable<Command> cmds, 
		  String originalFilename, 
		  PrintWriter out) throws Exception {
	  
	  System.out.println(" ** Got to constructor of TestGenerator");
	  System.out.println(" ** Got sigs:  " + sigs);
	  System.out.println(" ** Got assertions:  " + assertions);
	  System.out.println(" ** Got cmds:  " + cmds);
	  

	  out.println("// This C# file is generated from " + originalFilename + "\r\n");
	  out.println("#undef CONTRACTS_FULL\r\n");
	  
	  out.println("using System;\r\n"
			  	 +"using System.Linq;\r\n"
			  	 +"using System.Collections.Generic;\r\n"
			  	 +"using System.Diagnostics.Contracts;\r\n");
	  out.println("public static class Test {");
	  out.println("  public static void Main(string[] args) {");
	  
	  
	  System.out.println(" * Producing environment..."); 
	  Set<Pair<Sig,String>> abstractSigsWithSingl = new HashSet<Pair<Sig,String>>();
	  for(Sig sig : sigs){
		  if(sig.label.equals("univ") || sig.label.equals("Int")
				  || sig.label.equals("seq/Int")
				  || sig.label.equals("none")
				  || sig.label.equals("String")){
			  continue;
		  }
		  
		  String name = sig.label.substring(5);
		  String setName = name + "Set";
		  System.out.println(" A4Tuple For sig " + name);
		  out.println("    " + "var " + setName + " = new HashSet<" + name + ">();");
		  
		  boolean isChildOfAbst = false;
		  for(Pair<Sig,String> p : abstractSigsWithSingl){
			  if(sig.isSameOrDescendentOf(p.a)){
				  isChildOfAbst = true;
				  name = p.b;
				  break;
			  }
		  }
		  
		  if(sig.isAbstract != null){
			  boolean foundSingleton = false;
			  for(Sig s : sigs){
				  if(s.isOne != null && s.isSameOrDescendentOf(sig)){
					  name = s.label.substring(5);
					  foundSingleton = true;
					  abstractSigsWithSingl.add(new Pair(sig,name));
					  break;
				  }
			  }
			  if(foundSingleton){
				  out.println("    " + name + " " + name + "0;");
				  out.println("    " + setName + ".Add(" + name + "0 = " + name + ".Instance);");
			  }
		  } else if(isChildOfAbst) {
			  out.println("    " + setName + ".Add(" + name + "0);");
		  } else {	  
			  A4TupleSet tuples = solution.eval(sig);
			  int i = 0;
			  for(A4Tuple _ : tuples){
				  String instanceName = (name + i++);
				  out.println("    " + name + " " + instanceName + ";");
				  if(sig.isOne != null){
					  out.println("    " + setName + ".Add(" + instanceName + " = " + name + ".Instance);");
				  }else{
					  out.println("    " + setName + ".Add(" + instanceName + " = new " + name + "());");
				  }
			  }
		  }

		  for(Field f : sig.getFields()){
			  A4TupleSet fieldTuples = solution.eval(f);
			  for(A4Tuple field : fieldTuples){
				  String objName = ASTHelper.extractGeneratedInstanceName(field.atom(0));
				  String refName = ASTHelper.extractGeneratedInstanceName(field.atom(1));
				  
				  out.println("    " + objName + "." + f.label + " = " + refName + ";");
			  }
		  }
		  
		  out.println("");
	  }
	  
	  TestGeneratorVisitor v = new TestGeneratorVisitor(out);
	  for(Pair<String, Expr> assertion : assertions){
		  v.setIdent(1);
		  System.out.println(" * Parsing " + assertion.a);
		  //out.println(assertion.b.accept(v).csharpCode);
	  }
	  
	  System.out.println("* Handling commands");
	  for(Command cmd : cmds){
		  System.out.println("  Label: " + cmd.label);
		  System.out.println("  Bitwidth: " + cmd.bitwidth);
		  System.out.println("  Max Sequence: " + cmd.maxseq);
		  System.out.println("  Overall Scope: " + cmd.overall);
		  System.out.println("  Expect: " + cmd.expects);
		  System.out.println("  Is: " + (cmd.check ? "'check'" : "'run'"));
		  v.setExpect(cmd.expects);
		  NodeInfoTest node = cmd.formula.accept(v);
		  
		  out.println("    Contract.Assert(" + node.csharpCode + ", \"" + cmd.label + "\");");
	  }
	  
	  out.println("  }\r\n}");
	  out.flush();
	  out.close();
	  
  }

  public static void writeTest(A4Solution solution, Module world, String originalFilename, boolean saveInDist) throws Err {
    try {
      String f;
      String ext = ".tests.cs";
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
      PrintWriter out = new PrintWriter(new FileWriter(f, true));
      new TestGenerator(solution, world.getAllReachableSigs(), world.getAllAssertions(), world.getAllCommands(), originalFilename, out);
    }
    catch (Throwable ex) {
      if (ex instanceof Err) {
        throw (Err)ex;
      }
      else {
        throw new ErrorFatal("Error writing the generated C# test file.", ex);
      }
    }
  }
}

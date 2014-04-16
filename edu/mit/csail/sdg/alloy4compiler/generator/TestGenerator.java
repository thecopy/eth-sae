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
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
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
	  
	  //System.out.println(" ** Got to constructor of TestGenerator");
	  //System.out.println(" ** Got sigs:  " + sigs);
	  //System.out.println(" ** Got assertions:  " + assertions);
	  //System.out.println(" ** Got cmds:  " + cmds);
	  

	  out.println("// This C# file is generated from " + originalFilename + "\r\n");
	  
	  out.println("using System;\r\n"
			  	 +"using System.Linq;\r\n"
			  	 +"using System.Collections.Generic;\r\n"
			  	 +"using System.Diagnostics.Contracts;\r\n");
	  out.println("public static class Test {");
	  out.println("  public static void Main(string[] args) {");

	  
	  //System.out.println(" * Declaring environment..."); 
	  for(Sig sig : sigs){
		  if(sig.label.equals("univ") || sig.label.equals("Int")
				  || sig.label.equals("seq/Int")
				  || sig.label.equals("none")
				  || sig.label.equals("String")
				  || sig.isAbstract != null){
			  continue;
		  }
		  String name = sig.label.substring(5);
		  
		  A4TupleSet tuples = solution.eval(sig);
		  int i = 0;
		  for(A4Tuple _ : tuples){
			  //System.out.println("Declaring...");
			  String instanceName = (name + i++);
			  out.println("    " + name + " " + instanceName + ";");
		  }
	  }

	  
	  HashSet<String> instantiatedSets = new HashSet<String>();
	  HashSet<String> instantiatedVariables = new HashSet<String>();
	  
	  //System.out.println(" * Instantiating environment..."); 
	  for(Sig sig : sigs){
		  if(sig.label.equals("univ") || sig.label.equals("Int")
				  || sig.label.equals("seq/Int")
				  || sig.label.equals("none")
				  || sig.label.equals("String")
				  || sig.isAbstract != null){
			  continue;
		  }
		  PrimSig primSig = (PrimSig)sig;
		  
		  String name = sig.label.substring(5);
		  String setName = name + "Set";
		  //System.out.println(" A4Tuple For sig " + name);

		  if(!instantiatedSets.contains(setName)){					  
			  out.println("    " + "var " + setName + " = new HashSet<" + name + ">();");
			  instantiatedSets.add(setName);
		  }
		  
		  instantiatedSets.add(setName);
		  
		  A4TupleSet tuples = solution.eval(sig);
		  int i = 0;
		  for(A4Tuple _ : tuples){
			  //System.out.println("Instantiating...");
			  String instanceName = (name + i++);
			  if(!instantiatedVariables.contains(instanceName)){
				  if(sig.isOne != null){
					  out.println("    " + instanceName + " = " + name + ".Instance;");
				  }else{
					  out.println("    " + instanceName + " = new " + name + "();");
				  }
				  instantiatedVariables.add(instanceName);
			  }
			  
			  out.println("    " + setName + ".Add(" + instanceName + ");");
			  
			  PrimSig parent = primSig.parent;
			  while(parent != null && !parent.label.equals("univ")){
				  //System.out.println("Handling parent " + parent);
				  String parentName = parent.label.substring(5);
				  String parentSetName = parentName + "Set";
				  if(!instantiatedSets.contains(parentSetName)){					  
					  out.println("    " + "var " + parentSetName + " = new HashSet<" + parentName + ">();");
					  instantiatedSets.add(parentSetName);
				  }
				  
				  out.println("    " + parentSetName + ".Add(" + instanceName + ");");
				  parent = parent.parent;
			  }
		  }
		  

		  for(Field f : sig.getFields()){
			  A4TupleSet fieldTuples = solution.eval(f);
			  boolean isOne = f.decl().expr.toString().contains("one ") && f.decl().expr.mult == 1;
			  for(A4Tuple field : fieldTuples){
				  int arity = field.arity();
				  String objName = ASTHelper.extractGeneratedInstanceName(field.atom(0));
				  String refName1 = ASTHelper.extractGeneratedInstanceName(field.atom(1));
				  String typeName1 = field.atom(1).substring(0,field.atom(1).indexOf("$"));
				  String refName2 = "";
				  String typeName2 = "";
				  if(arity > 2){
					  refName2 = ASTHelper.extractGeneratedInstanceName(field.atom(2));
					  typeName2 = field.atom(2).substring(0,field.atom(2).indexOf("$"));
				  }
				  if(!instantiatedVariables.contains(refName1)){
					  if(sig.isOne != null){
						  out.println("    " + refName1 + " = " + typeName1 + ".Instance;");
					  }else{
						  out.println("    " + refName1 + " = new " + typeName1 + "();");
					  }
					  instantiatedVariables.add(refName1);
				  }
				  if(refName2 != "" && !instantiatedVariables.contains(refName2)){
					  if(sig.isOne != null){
						  out.println("    " + refName2 + " = " + typeName2 + ".Instance;");
					  }else{
						  out.println("    " + refName2 + " = new " + typeName2 + "();");
					  }
					  instantiatedVariables.add(refName2);
				  }
				  if(arity > 2){
					  if(!instantiatedSets.contains(objName + "." + f.label)){
						  out.println("    " + objName + "." + f.label + " = new HashSet<Tuple<" + typeName1 + ", " + typeName2 +">>();");
						  instantiatedSets.add(objName+ "." + f.label);
					  }
					  out.println("    " + objName + "." + f.label + ".Add(new Tuple<" + typeName1 + ", " + typeName2 +">(" + refName1 + ", " + refName2 + "));");
				  } else if(arity == 2 && !isOne){
					  if(!instantiatedSets.contains(objName + "." + f.label)){
						  out.println("    " + objName + "." + f.label + " = new HashSet<" + typeName1 + ">();");
						  instantiatedSets.add(objName+ "." + f.label);
					  }
					  out.println("    " + objName + "." + f.label + ".Add(" + refName1 + ");");
				  } else {
					  out.println("    " + objName + "." + f.label + " = " + refName1 + ";");
				  }
			  }
		  }
		  
		  out.println("");
	  }
	  
	  Visitor v = new Visitor();
	  for(Pair<String, Expr> assertion : assertions){
		  v.setIdent(1);
		  //System.out.println(" * Parsing " + assertion.a);
		  //out.println(assertion.b.accept(v).csharpCode);
	  }
	  
	  //System.out.println("* Handling commands");
	  for(Command cmd : cmds){
		  //System.out.println("  Label: " + cmd.label);
		  //System.out.println("  Bitwidth: " + cmd.bitwidth);
		  //System.out.println("  Max Sequence: " + cmd.maxseq);
		  //System.out.println("  Overall Scope: " + cmd.overall);
		  //System.out.println("  Expect: " + cmd.expects);
		  //System.out.println("  Is: " + (cmd.check ? "'check'" : "'run'"));
		  v.setExpect(cmd.expects);
		  NodeInfo node = cmd.formula.accept(v);
		  
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

package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprHasName;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

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
		  
		  NodeInfoTest node = cmd.formula.accept(v);
		  
		  out.println("  Contract.Assert(" + node.csharpCode + ");");
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

package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4.ErrorFatal;

import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;

public final class CodeGenerator {

  private CodeGenerator(
		  Iterable<Sig> sigs, SafeList<Func> funcs, 
		  String originalFilename, PrintWriter out, 
		  boolean checkContracts) throws Exception, Err {

	  System.out.println(" ** Got to constructor");
	  System.out.println(" ** Got sigs:  " + sigs);
	  System.out.println(" ** Got funcs:  " + funcs);
	  System.out.println(" ** Got originalFilename:  " + originalFilename);
	  out.println("YAY");
	  
	  for(Sig sig : sigs){
		  if(sig.builtin == false){
			  out.println("public class " + sig.label + "{}");
		  }
	  }
	  
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

// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests23.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    // setup test data
    var DateSet = new HashSet<Date>();

    // check test data
    var d = new Date();
    DateSet.Add(d);
    Contract.Assert(FuncClass.Implication(d, d, DateSet), "Assertion");
  }
}

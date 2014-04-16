// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests22.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    // setup test data
    var DateSet = new HashSet<Date>();

    // check test data
    var tmp = new HashSet<Date>();
    tmp.Add(new Date());
    Contract.Assert(FuncClass.Intersection(DateSet, tmp).Count() == 0, "Assertion");
  }
}

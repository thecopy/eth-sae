// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests13.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    // setup test data
    var ManSet = new HashSet<Man>();

    // check test data
    Contract.Assert(FuncClass.Id(ManSet).Count() == 0, "Assertion");
  }
}

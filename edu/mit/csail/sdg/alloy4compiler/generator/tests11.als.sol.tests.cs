// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests11.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    // setup test data
    var EveSet = new HashSet<Eve>();
    Eve Eve0;
    EveSet.Add(Eve0 = Eve.Instance);

    // check test data
    Contract.Assert(true, "Assertion");
  }
}

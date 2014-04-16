// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests11.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    Eve Eve0;
    var EveSet = new HashSet<Eve>();
    Eve0 = Eve.Instance;
    EveSet.Add(Eve0);

    Contract.Assert(((0.Equals(1))), "Assertion");
  }
}

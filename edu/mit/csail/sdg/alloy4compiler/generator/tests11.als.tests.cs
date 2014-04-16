// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests11.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    S S0;
    A A0;
    A A1;
    Eve Eve0;
    var SSet = new HashSet<S>();
    S0 = new S();
    SSet.Add(S0);

    var ASet = new HashSet<A>();
    A0 = new A();
    ASet.Add(A0);
    A1 = new A();
    ASet.Add(A1);
    A1.field = new HashSet<S>();
    A1.field.Add(S0);
    A0.field2 = S0;
    A1.field2 = S0;

    var EveSet = new HashSet<Eve>();
    Eve0 = Eve.Instance;
    EveSet.Add(Eve0);

    Contract.Assert(((0.Equals(1))), "Assertion");
  }
}

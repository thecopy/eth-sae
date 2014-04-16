// This C# file is generated from C:\Users\Erik Jonsson Thorén\a.als

#undef CONTRACTS_FULL

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    var ASet = new HashSet<A>();
    A A0;
    ASet.Add(A0 = new A());
    A0.field = A0;
    A0.field2 = A0;

    Contract.Assert(, "show");
  }
}

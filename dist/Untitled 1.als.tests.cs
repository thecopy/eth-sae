// This C# file is generated from C:\Users\Erik Jonsson Thorén\workspace2\MariasAlloy\AlloyAnalyzer-master\dist\Untitled 1.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    A A0;
    A A1;
    var ASet = new HashSet<A>();
    A0 = new A();
    ASet.Add(A0);
    A1 = new A();
    ASet.Add(A1);
    A0.fieldn = new HashSet<A>();
    A0.fieldn.Add(A1);
    A1.fieldn = new HashSet<A>();
    A1.fieldn.Add(A0);
    A1.fieldn.Add(A1);
    A0.setPlusSet = A1;
    A1.setPlusSet = A1;

    Contract.Assert((fieldn.Count()) > (2), "show");
  }
}

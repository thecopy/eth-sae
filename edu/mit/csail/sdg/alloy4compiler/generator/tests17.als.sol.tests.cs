// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests17.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    // setup test data
    var ManSet = new HashSet<Man>();

    // check test data
    var tmp = new HashSet<Tuple<Man, Man>>();
    var man = new Man();
    tmp.Add(Tuple.Create<Man, Man>(new Man(), man));
    tmp.Add(Tuple.Create<Man, Man>(new Man(), man));
    Contract.Assert(FuncClass.Id(tmp).Count() == 2, "Assertion");
  }
}

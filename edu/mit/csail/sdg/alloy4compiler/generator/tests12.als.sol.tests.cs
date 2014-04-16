// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests12.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    // setup test data
    var PlatformSet = new HashSet<Platform>();
    Platform Platform0;
    PlatformSet.Add(Platform0 = new Platform());
    var ManSet = new HashSet<Man>();
    Man Man0;
    ManSet.Add(Man0 = new Man());
    Man Man1;
    ManSet.Add(Man1 = new Man());
    Man Man2;
    ManSet.Add(Man2 = new Man());
    Man0.ceiling = Platform0;
    Man1.ceiling = Platform0;
    Man2.ceiling = Platform0;
    Man0.floor = Platform0;
    Man1.floor = Platform0;
    Man2.floor = Platform0;

    // check test data
    Contract.Assert(FuncClass.Above(Man0, Man1), "Assertion");
  }
}

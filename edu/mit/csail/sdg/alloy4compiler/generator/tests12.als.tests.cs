// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests12.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    Platform Platform0;
    Man Man0;
    Man Man1;
    Man Man2;
    var PlatformSet = new HashSet<Platform>();
    Platform0 = new Platform();
    PlatformSet.Add(Platform0);

    var ManSet = new HashSet<Man>();
    Man0 = new Man();
    ManSet.Add(Man0);
    Man1 = new Man();
    ManSet.Add(Man1);
    Man2 = new Man();
    ManSet.Add(Man2);
    Man0.ceiling = Platform0;
    Man1.ceiling = Platform0;
    Man2.ceiling = Platform0;
    Man0.floor = Platform0;
    Man1.floor = Platform0;
    Man2.floor = Platform0;

    Contract.Assert(((0.Equals(1))), "Assertion");
  }
}

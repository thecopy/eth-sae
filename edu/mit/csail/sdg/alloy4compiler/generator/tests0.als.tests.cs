// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests0.als

#undef CONTRACTS_FULL

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    Platform Platform0;
    Platform Platform1;
    Man Man0;
    Woman Woman0;
    Eve Eve0;
    var PlatformSet = new HashSet<Platform>();
    Platform0 = new Platform();
    PlatformSet.Add(Platform0);
    Platform1 = new Platform();
    PlatformSet.Add(Platform1);

    var ManSet = new HashSet<Man>();
    Man0 = new Man();
    ManSet.Add(Man0);
    Man0.ceiling = Platform1;
    Man0.floor = Platform0;

    var DateSet = new HashSet<Date>();

    var WomanSet = new HashSet<Woman>();
    Woman0 = new Woman();
    WomanSet.Add(Woman0);
    var PersonSet = new HashSet<Person>();
    PersonSet.Add(Woman0);

    var EveSet = new HashSet<Eve>();
    Eve0 = Eve.Instance;
    EveSet.Add(Eve0);
    WomanSet.Add(Eve0);
    PersonSet.Add(Eve0);

    Contract.Assert(!(Contract.ForAll(ManSet, m => ManSet.Where(n => FuncClass.Above(m, n)).Count() == 1)), "BelowToo");
  }
}

// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests2.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    From From0;
    To To0;
    Class Class0;
    var FromSet = new HashSet<From>();
    From0 = new From();
    FromSet.Add(From0);

    var ToSet = new HashSet<To>();
    To0 = new To();
    ToSet.Add(To0);

    var ClassSet = new HashSet<Class>();
    Class0 = new Class();
    ClassSet.Add(Class0);
    Class0.field = new HashSet<Tuple<From, To>>();
    Class0.field.Add(new Tuple<From, To>(From0, To0));

    Contract.Assert(((0.Equals(1))), "Assertion");
  }
}

// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests7.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    // setup test data
    var PersonSet = new HashSet<Person>();
    Person Person0;
    PersonSet.Add(Person0 = new Person());
    var MarriedManSet = new HashSet<MarriedMan>();
    MarriedMan MarriedMan0;
    MarriedManSet.Add(MarriedMan0 = new MarriedMan());
    MarriedMan MarriedMan1;
    MarriedManSet.Add(MarriedMan1 = new MarriedMan());
    MarriedMan0.spouse = Person0;
    MarriedMan1.spouse = Person0;

    // check test data
    Contract.Assert(true, "Assertion");
  }
}

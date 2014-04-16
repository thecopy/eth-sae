// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests7.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    Person Person0;
    MarriedMan MarriedMan0;
    MarriedMan MarriedMan1;
    var PersonSet = new HashSet<Person>();
    Person0 = new Person();
    PersonSet.Add(Person0);

    var MarriedManSet = new HashSet<MarriedMan>();
    MarriedMan0 = new MarriedMan();
    MarriedManSet.Add(MarriedMan0);
    MarriedMan1 = new MarriedMan();
    MarriedManSet.Add(MarriedMan1);
    MarriedMan0.spouse = Person0;
    MarriedMan1.spouse = Person0;

    Contract.Assert(((0.Equals(1))), "Assertion");
  }
}

// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests2.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public class From {
}

public class To {
}

public class Class {
  public ISet<Tuple<From, To>> field;

  public Class() {
    field = new HashSet<Tuple<From, To>>();
    field.Add(Tuple.Create<From, To>(new From(), null));
  }

  [ContractInvariantMethod]
  private void ObjectInvariant() {
    Contract.Invariant(field != null && Contract.ForAll(field, e1 => e1.Item1 != null && e1.Item2 != null));
  }
}
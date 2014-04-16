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

  [ContractInvariantMethod]
  private void ObjectInvariant() {
    Contract.Invariant(field != null);
    Contract.Invariant(Contract.ForAll(field, e1 => e1 != null && field.Count(x => x.Item1.Equals(e1.Item1)) >= 1));
  }
}

public static class FuncClass {
}
public static class Helper {
  public static ISet<Tuple<L, R>> Closure<L, R>(ISet<Tuple<L, R>> set) {
    ISet<Tuple<L, R>> closure = new HashSet<Tuple<L, R>>();
    Tuple<L,R>[] tuplesArray = new Tuple<L,R>[set.Count];
    set.CopyTo(tuplesArray, 0);
    foreach (Tuple<L, R> tup in set) {
      L first = tup.Item1;
      R second = tup.Item2;
      closure.Add(new Tuple<L, R>(first, second));
      for (int i = 0; i < tuplesArray.Length; i++) {
        L left = tuplesArray[i].Item1;
        if (second.Equals(left)) {
          closure.Add(new Tuple<L, R>(first, tuplesArray[i].Item2));
        }
      }
    }
    if (closure.Count == set.Count) {
      return closure;
    }
    else {
      return Closure(closure);
    }
  }
  public static ISet<Tuple<L, R>> RClosure<L, R>(ISet<Tuple<L, R>> set) {
    ISet<Tuple<L, R>> closure = Closure(set);
    foreach(Tuple<L, R> tup in set) {
      L first = tup.Item1;
      R second = tup.Item2;
      Object temp1 = (Object)first;
      Object temp2 = (Object)second;
      L castedSecond = (L)temp2;
      R castedFirst = (R)temp1;
      closure.Add(new Tuple<L,R>(first,castedFirst));
      closure.Add(new Tuple<L,R>(castedSecond,second));
    }
    return closure;
  }
  public static ISet<Tuple<R, L>> Transpose<L, R>(ISet<Tuple<L, R>> set) {
    ISet<Tuple<R, L>> transpose = new HashSet<Tuple<R, L>>();
    foreach (Tuple<L, R> tup in set) {
      L first = tup.Item1;
      R second = tup.Item2;
      transpose.Add(new Tuple<R, L>(second, first));
    }
    return transpose;
  }
}

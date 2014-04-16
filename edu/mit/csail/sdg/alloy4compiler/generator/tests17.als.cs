// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests17.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public class Man {
}

public static class FuncClass {
  public static ISet<Tuple<Man, Man>> Id (ISet<Tuple<Man, Man>> friends){
    Contract.Requires(friends != null);
    Contract.Requires(Contract.ForAll(friends, e => e != null));
    Contract.Ensures(Contract.Result<ISet<Tuple<Man, Man>>>() != null);
    Contract.Ensures(Contract.ForAll(Contract.Result<ISet<Tuple<Man, Man>>>(), e1 => e1 != null && Contract.Result<ISet<Tuple<Man, Man>>>().Count(x => x.Item2.Equals(e1.Item2)) == 1));

    return friends;
  }
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
}

// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests11.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public class S {
}

public class A {
  public ISet<S> field;
  public S field2;

  [ContractInvariantMethod]
  private void ObjectInvariant() {
    Contract.Invariant(Contract.ForAll(field, e => e != null));
    Contract.Invariant(field != null);
    Contract.Invariant(field2 != null);
  }
}

public class Eve {
  private static Eve instance;
  private Eve() {}
  public static Eve Instance {
    get{
      if(instance == null) instance = new Eve();
      return instance;
    }
  }
}

public static class FuncClass {
  public static ISet<S> test (A a){
    Contract.Ensures(Contract.Result<ISet<S>>() != null);
    Contract.Ensures(Contract.ForAll(Contract.Result<ISet<S>>(), e => e != null));

    return a.field;
  }
  public static S test2 (A a){
    Contract.Ensures(Contract.Result<S>() != null);

    return a.field.Single();
  }
  public static ISet<S> test3 (A a){
    Contract.Ensures(Contract.Result<ISet<S>>() != null);
    Contract.Ensures(Contract.ForAll(Contract.Result<ISet<S>>(), e => e != null));

    return new HashSet<S>(a.field.Intersect<S>(a.field2));
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
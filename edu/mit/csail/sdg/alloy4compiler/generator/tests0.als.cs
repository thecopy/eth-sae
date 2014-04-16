// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests0.als

#undef CONTRACTS_FULL

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public class Platform {
}

public class Man {
  public Platform ceiling;
  public Platform floor;
  public ISet<Tuple<Platform, Platform>> between;

  [ContractInvariantMethod]
  private void ObjectInvariant() {
    Contract.Invariant(Contract.ForAll(between, e => e.Item2.Equals(this.ceiling)));
    Contract.Invariant(Contract.ForAll(between, e => e != null));
    Contract.Invariant(Contract.ForAll(between, e => e.Item1.Equals(this.floor)));
    Contract.Invariant(between != null);
    Contract.Invariant(floor != null);
    Contract.Invariant(ceiling != null);
  }
}

public class Date {
}

abstract public class Person {
}

public class Woman : Person {
}

public class Eve : Woman {
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
  public static bool Above (Man m, Man n){

    return (m.floor.Equals(n.ceiling));
  }
  public static ISet<Tuple<Date, Date>> Closure (ISet<Tuple<Date, Date>> date){
    Contract.Requires(date != null);
    Contract.Requires(Contract.ForAll(date, e => e != null));
    Contract.Ensures(Contract.Result<ISet<Tuple<Date, Date>>>() != null);
    Contract.Ensures(Contract.ForAll(Contract.Result<ISet<Tuple<Date, Date>>>(), e => e != null));

    return Helper.Closure(date);
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

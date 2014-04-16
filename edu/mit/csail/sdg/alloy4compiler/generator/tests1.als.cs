// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests1.als

#undef CONTRACTS_FULL

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

abstract public class Object {
}

public class Name {
}

public class File : Object {
}

public class Dir : Object {
  public ISet<DirEntry> entries;
  public Dir parent;

  [ContractInvariantMethod]
  private void ObjectInvariant() {
    Contract.Invariant(Contract.ForAll(entries, e => e != null));
    Contract.Invariant(entries != null);
  }
}

public class Root : Dir {
  private static Root instance;
  private Root() {}
  public static Root Instance {
    get{
      if(instance == null) instance = new Root();
      return instance;
    }
  }
}

public class Cur : Dir {
  private static Cur instance;
  private Cur() {}
  public static Cur Instance {
    get{
      if(instance == null) instance = new Cur();
      return instance;
    }
  }
}

public class DirEntry {
  public Name name;
  public Object contents;

  [ContractInvariantMethod]
  private void ObjectInvariant() {
    Contract.Invariant(name != null);
    Contract.Invariant(contents != null);
  }
}

public static class FuncClass {
  public static bool OneParent_buggyVersion (){

    return ;
  }
  public static bool OneParent_correctVersion (){

    return ;
  }
  public static bool NoDirAliases (){

    return ;
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

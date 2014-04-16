// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests8.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    Open Open0;
    Pending Pending0;
    Account Account0;
    Account Account1;
    var OpenSet = new HashSet<Open>();
    Open0 = new Open();
    OpenSet.Add(Open0);
    var StatusSet = new HashSet<Status>();
    StatusSet.Add(Open0);

    var ClosedSet = new HashSet<Closed>();

    var PendingSet = new HashSet<Pending>();
    Pending0 = new Pending();
    PendingSet.Add(Pending0);
    StatusSet.Add(Pending0);

    var AccountSet = new HashSet<Account>();
    Account0 = new Account();
    AccountSet.Add(Account0);
    Account1 = new Account();
    AccountSet.Add(Account1);
    Account0.status = Open0;
    Account1.status = Pending0;

    Contract.Assert(((0.Equals(1))), "Assertion");
  }
}

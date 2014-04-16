// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests8.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

abstract public class Status {
}

public class Open : Status {
}

public class Closed : Status {
}

public class Pending : Status {
}

public class Account {
  public Status status;

  public Account() {
    status = new Open();
  }

  [ContractInvariantMethod]
  private void ObjectInvariant() {
    Contract.Invariant(status != null);
  }
}
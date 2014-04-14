-- abstract classes and inheritance

abstract sig Status { }

sig Open, Closed, Pending extends Status { }

sig Account {
  status: Status
}

assert Assertion {
  0 = 1
}

check Assertion

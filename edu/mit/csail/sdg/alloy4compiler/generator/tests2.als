-- any_arrow_some invariant

sig From {}
sig To {}

sig Class {
  field: From -> some To
}

assert Assertion {
  0 = 1
}

check Assertion

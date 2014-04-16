-- one_of invariant

sig Person {}

sig MarriedMan {
  spouse: one Person
}

assert Assertion {
  0 = 1
}

check Assertion
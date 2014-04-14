-- some_of function precondition

sig Man {}

fun Id[men: some Man]: some Man {
  men
}

assert Assertion {
  0 = 1
}

check Assertion
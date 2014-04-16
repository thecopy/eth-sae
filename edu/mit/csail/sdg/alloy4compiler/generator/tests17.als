-- one_arrow_any function postcondition

sig Man {}

fun Id[friends: Man -> Man]: Man one -> Man {
  friends
}

assert Assertion {
  0 = 1
}

check Assertion
-- function calls

sig Man {}

pred True[man: Man] {
  Id[man] = Id[man]
}

fun Id[man: Man]: Man {
  man
}

assert Assertion {
  0 = 1
}

check Assertion

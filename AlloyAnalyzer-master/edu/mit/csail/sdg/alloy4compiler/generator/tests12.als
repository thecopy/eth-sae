-- predicates

sig Platform {}
sig Man {
  ceiling, floor: Platform
}

pred Above[m, n: Man] {
  m.floor = n.ceiling
}

assert Assertion {
  0 = 1
}

check Assertion
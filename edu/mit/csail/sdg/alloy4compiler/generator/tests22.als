-- set intersection

sig Date {}

fun Intersection[firstDate, secondDate: set Date]: set Date {
  firstDate & secondDate
}

assert Assertion {
  0 = 1
}

check Assertion
-- dependent types

sig Name {}
sig Date {}

sig BirthdayBook {
  known: set Name,
  date: known -> Date
}

assert Assertion {
  0 = 1
}

check Assertion
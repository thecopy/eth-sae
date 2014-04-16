-- implications

sig Date {}

pred Implication[d0: Date, d1: Date, ds: some Date] {
  d0 = d1 implies (d0 in ds and d1 in ds)
}

assert Assertion {
  0 = 1
}

check Assertion
/*  ceilings and floors */

sig Platform {}
sig Man extends Person {
  ceiling, floor: one Platform,
  wifes: one Woman,
  between: floor -> ceiling
}

pred Above[m, n: Man] {
  m.floor = n.ceiling
}

assert BelowToo { all m: Man | one n: Man | m.Above[n] }

check BelowToo for 3 expect 1

/* dates */

sig Date {}
fun Closure[date: Date -> Date]: Date -> Date {^date}

/* genealogy */
abstract sig Person  {}
sig Woman extends Person {}
sig Eve extends Woman {}
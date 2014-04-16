/*  ceilings and floors */
sig S{}
sig A{
	field: set S
	field2: set S
}

sig Platform {}
sig Man {
  ceiling, floor: one Platform,
  between: floor -> ceiling
}

pred test[a: A] one S{
	A.field
}

pred test2[a: A] one S{
	A.field & A.field2
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
one sig Eve extends Woman {}
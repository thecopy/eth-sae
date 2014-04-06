sig A {}
sig B {}
sig D {
	x: set A,
    E: x+x
}

pred checker[s:D] {
	#s.x > 1 && #s.E > 2
}

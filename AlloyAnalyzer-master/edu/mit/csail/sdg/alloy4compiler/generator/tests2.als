sig B{}
sig A{
  field: B
}

assert foo {
  not
    // always holds, if you remove "not", no instance can be found.
    all a:A | let b = a+a | #(b.field) != 0
}
check foo for 3
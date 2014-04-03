sig A {}
sig B {}
sig C {
        field1: A set -> set B,
        field2: A one -> one B,
        field3: field1 + field2
}
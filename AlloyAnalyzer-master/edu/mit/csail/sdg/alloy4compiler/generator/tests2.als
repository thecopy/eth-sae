sig SuperBase {}
sig Base extends SuperBase {}
sig A extends Base {}
sig B extends Base {}
sig C {
		Y0: A+B,
        Y1: A set -> set B,
        Y2: A one -> one B,

        Y3: Y1 + Y2
}
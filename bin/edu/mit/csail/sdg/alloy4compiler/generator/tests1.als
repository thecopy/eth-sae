  //  Simple example
  abstract sig Person {     // Signature
    father: lone Man,       //   A declaration
    mother: lone Woman      //   Another declaration
  }
  sig Man extends Person {
    wife: lone Woman
  }
  sig Woman extends Person {
    husband: lone Man
  }
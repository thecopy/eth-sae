// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests5.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    Name Name0;
    Date Date0;
    Date Date1;
    Date Date2;
    BirthdayBook BirthdayBook0;
    BirthdayBook BirthdayBook1;
    BirthdayBook BirthdayBook2;
    var NameSet = new HashSet<Name>();
    Name0 = new Name();
    NameSet.Add(Name0);

    var DateSet = new HashSet<Date>();
    Date0 = new Date();
    DateSet.Add(Date0);
    Date1 = new Date();
    DateSet.Add(Date1);
    Date2 = new Date();
    DateSet.Add(Date2);

    var BirthdayBookSet = new HashSet<BirthdayBook>();
    BirthdayBook0 = new BirthdayBook();
    BirthdayBookSet.Add(BirthdayBook0);
    BirthdayBook1 = new BirthdayBook();
    BirthdayBookSet.Add(BirthdayBook1);
    BirthdayBook2 = new BirthdayBook();
    BirthdayBookSet.Add(BirthdayBook2);
    BirthdayBook1.known = new HashSet<Name>();
    BirthdayBook1.known.Add(Name0);
    BirthdayBook2.known = new HashSet<Name>();
    BirthdayBook2.known.Add(Name0);
    BirthdayBook1.date = new HashSet<Tuple<Name, Date>>();
    BirthdayBook1.date.Add(new Tuple<Name, Date>(Name0, Date2));
    BirthdayBook2.date = new HashSet<Tuple<Name, Date>>();
    BirthdayBook2.date.Add(new Tuple<Name, Date>(Name0, Date0));
    BirthdayBook2.date.Add(new Tuple<Name, Date>(Name0, Date1));

    Contract.Assert(((0.Equals(1))), "Assertion");
  }
}

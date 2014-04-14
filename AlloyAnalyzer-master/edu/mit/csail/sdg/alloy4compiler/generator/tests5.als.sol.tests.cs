// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests5.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    // setup test data
    var NameSet = new HashSet<Name>();
    Name Name0;
    NameSet.Add(Name0 = new Name());
    var DateSet = new HashSet<Date>();
    Date Date0;
    DateSet.Add(Date0 = new Date());
    Date Date1;
    DateSet.Add(Date1 = new Date());
    Date Date2;
    DateSet.Add(Date2 = new Date());
    var BirthdayBookSet = new HashSet<BirthdayBook>();
    BirthdayBook BirthdayBook0;
    BirthdayBookSet.Add(BirthdayBook0 = new BirthdayBook());
    BirthdayBook BirthdayBook1;
    BirthdayBookSet.Add(BirthdayBook1 = new BirthdayBook());
    BirthdayBook BirthdayBook2;
    BirthdayBookSet.Add(BirthdayBook2 = new BirthdayBook());
    BirthdayBook1.known = new HashSet<Name>();
    BirthdayBook1.known.Add(Name0);
    BirthdayBook2.known = new HashSet<Name>();
    BirthdayBook2.known.Add(Name0);
    BirthdayBook1.date = new HashSet<Tuple<Name, Date>>();
    BirthdayBook1.date.Add(Tuple.Create(Name0, Date2));
    BirthdayBook2.date = new HashSet<Tuple<Name, Date>>();
    BirthdayBook2.date.Add(Tuple.Create(Name0, Date0));
    BirthdayBook2.date.Add(Tuple.Create(Name0, Date1));

    // check test data
    Contract.Assert(true, "Assertion");
  }
}

# src/

Two programs, one fleet, two languages. Either runs alone; both read `data_and_tech/yard.json`.

```bash
scala-cli run --server=false src/Ships.scala -- data_and_tech/yard.json
javac -d out src/Fleet.java && java -cp out navalhistory.Fleet data_and_tech/yard.json
```

## Ships.scala (Scala 3)
Java enums written in Scala (`extends java.lang.Enum`): `Profile`, `Station`, `Hull`, `Source` (1–8). Traits as the interfaces (`Capability`, `Duty`). `Ship` as a case class with a JSON ReadWriter. `Shipyard` reads the yard rows, keeps a steel balance, lays down hulls on the same 1.25× cost curve as the idle game. `Combinations` counts fleets of n ships from the 30 hull×station pairs, from n = 1 without end.

## Fleet.java (Java 17+)
The same fleet with Java's own tools. The six plug points — the places you pull one thing out and put your own in — are listed at the top of the file: the `Profile` interface, the `ShipProfile` record, the `Hull` enum with per-constant bodies, the three functional interfaces injected into `Shipyard` (cost curve, steel source, naming), the yard JSON, the `Source` enum.

## What both say
Fleets of n ships from 30 combinations pass 2 × 10^255 at n = 7,423,450,558. That is the perpetuity: the enums are closed, the combinations are not.

**Wanted.** A third language. A test suite that pins the combinatorics. A hull that is not a warship — a hospital ship, the Helgoland — and the profile it needs, which none of the current ones fit. Whoever adds a constant to `Hull` will find the compiler pointing at every switch that has to learn it; that is the design, not a bug.

// Fleet.java — the Java side of the suite. Same fleet, Java's own tools:
// a record as the profile, an interface as the contract, enums whose
// constants carry their own bodies, and a shipyard whose behaviour is
// plugged in through functional interfaces rather than subclassed.
//
//   scala-cli run src/Fleet.java -- data_and_tech/yard.json
//   (scala-cli compiles the Java alongside Ships.scala; either main runs alone.)
//
// WHERE THE PLUGS ARE — the places you can pull something out and put your own in:
//   1. Profile (interface)    — any class or record that implements it can be a profile; the fleet never checks the type.
//   2. ShipProfile (record)   — the default profile; a record is a class with the plugs already labelled: every component
//                               is a variable you can read and a constructor argument you can inject.
//   3. Hull (enum)            — each constant overrides describe(); add a constant, the switch in Shipyard is exhaustive and
//                               the compiler tells you every place that needs a case.
//   4. Shipyard(costCurve, steelSource, naming) — three functional interfaces passed in: the cost formula, where steel comes
//                               from, how ships are named. Pull one, plug another; the class body does not change.
//   5. yard.json              — the rows are injected, not declared; change the file and the numbers change.
//   6. Source (enum, 1..N)    — a closed list of the sources given, by number; cite by Source.N.

package navalhistory;

import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

public class Fleet {

    // ---- 1. the contract -----------------------------------------------------
    public interface Profile {
        int reachMiles();
        String projection();
        default String rating() { return reachMiles() >= 100 ? "projects power" : reachMiles() >= 20 ? "bombards" : "screens"; }
    }

    // ---- 2. the record: a profile with its plugs labelled ---------------------
    public record ShipProfile(String name, int reachMiles, String projection, int crew) implements Profile {
        public ShipProfile {                                   // compact constructor: the validation plug
            if (reachMiles < 0) throw new IllegalArgumentException("reach < 0: " + name);
            if (crew <= 0) throw new IllegalArgumentException("crew <= 0: " + name);
        }
        public ShipProfile withCrew(int c) { return new ShipProfile(name, reachMiles, projection, c); }  // records are immutable; this is how you "set"
    }

    // ---- 3. enums whose constants carry bodies --------------------------------
    public enum Hull {
        PATROL       ("patrol",        new ShipProfile("Patrol",       6,   "the coast; one gun and nineteen men", 19)) { String describe() { return "Attack class, 1967; Australia built twenty"; } },
        DESTROYER    ("destroyer",     new ShipProfile("Escort",       14,  "the gunline; five-inch guns and Tartar", 330)) { String describe() { return "Perth class, US-built; three"; } },
        LIGHT_CRUISER("cruiser",       new ShipProfile("Scout",        14,  "screens the fleet; six-inch guns", 450)) { String describe() { return "Omaha Girl's class; none since 1945"; } },
        LIGHT_CARRIER("carrier-light", new ShipProfile("AirWing",      300, "twenty aircraft; a small sovereign airbase", 1350)) { String describe() { return "Melbourne, ex-Majestic; retired 1982"; } },
        BATTLESHIP   ("battleship",    new ShipProfile("Bombardment",  24,  "nine sixteen-inch guns, to the horizon", 2700)) { String describe() { return "Mighty Mo's class; nobody builds them"; } },
        SUPERCARRIER ("supercarrier",  new ShipProfile("AirWing",      600, "eighty-five aircraft, to Hanoi", 5000)) { String describe() { return "Connie's class; the reason"; } };

        public final String id; public final ShipProfile profile;
        Hull(String id, ShipProfile p) { this.id = id; this.profile = p; }
        abstract String describe();                            // every constant must answer; the compiler enforces it
        static Hull byId(String id) { for (Hull h : values()) if (h.id.equals(id)) return h; throw new NoSuchElementException(id); }
    }

    public enum Station { YANKEE("Gulf of Tonkin"), DIXIE("off Cam Ranh Bay"), GUNLINE("the coast"), MARKET_TIME("the blockade"), HOME("Garden Island");
        public final String where; Station(String w) { where = w; } }

    // ---- 6. the sources, numbered from 1 as given -----------------------------
    public enum Source {
        N1("USS Missouri (BB-63), Mighty Mo — DANFS"), N2("https://en.wikipedia.org/wiki/USS_Constellation_(CV-64)"),
        N3("https://en.wikipedia.org/wiki/Cambodia"), N4("https://www.youtube.com/watch?v=tvMwcKQWmAE (video, not transcribed here)"),
        N5("https://www.youtube.com/watch?v=-N6744h_JWs (video, not transcribed here)"), N6("https://www.youtube.com/watch?v=_ecXNJP-ERY (video, not transcribed here)"),
        N7("https://www.youtube.com/watch?v=e4FQj0we3nE&t=2693s (video, not transcribed here)"), N8("https://www.youtube.com/watch?v=B1IZ-NTLORI (video, not transcribed here)");
        public final String ref; Source(String r) { ref = r; } public int number() { return ordinal() + 1; }
    }

    // ---- the ship: a record too ----------------------------------------------
    public record Ship(int pennant, String name, Hull hull, Station station, int tons, Source citedBy) {
        String manifest() { return String.format("%03d  %-13s %-22s %,7d t  %-11s %-12s [%d]", pennant, hull, name, tons, hull.profile.rating(), station.where, citedBy.number()); }
    }

    // ---- the yard rows, injected from JSON (no library: a small regex reader for this flat shape) ----
    public record YardRow(String id, String name, int tons, long cost, int rate) {}
    static List<YardRow> readYard(Path p) throws Exception {
        String s = Files.readString(p);
        Matcher m = Pattern.compile("\"id\":\\s*\"([^\"]+)\".*?\"name\":\\s*\"([^\"]+)\".*?\"tons\":\\s*(\\d+).*?\"cost\":\\s*(\\d+).*?\"rate\":\\s*(\\d+)", Pattern.DOTALL).matcher(s);
        List<YardRow> rows = new ArrayList<>();
        while (m.find()) rows.add(new YardRow(m.group(1), m.group(2), Integer.parseInt(m.group(3)), Long.parseLong(m.group(4)), Integer.parseInt(m.group(5))));
        return rows;
    }

    // ---- 4. the shipyard: behaviour plugged in, not inherited -----------------
    public static class Shipyard {
        private final Map<String, YardRow> rows = new LinkedHashMap<>();
        private final BiFunction<YardRow, Integer, Long> costCurve;   // (row, already built) -> cost
        private final LongSupplier steelSource;                        // where steel comes from when asked
        private final Function<Hull, String> naming;                   // how a hull gets a name
        private final List<Ship> fleet = new ArrayList<>();
        public long steel;

        public Shipyard(List<YardRow> yard, long steel, BiFunction<YardRow, Integer, Long> costCurve, LongSupplier steelSource, Function<Hull, String> naming) {
            for (YardRow r : yard) rows.put(r.id(), r);
            this.steel = steel; this.costCurve = costCurve; this.steelSource = steelSource; this.naming = naming;
        }
        int built(Hull h) { return (int) fleet.stream().filter(s -> s.hull() == h).count(); }
        long cost(Hull h) { return costCurve.apply(rows.get(h.id), built(h)); }
        public Optional<Ship> layDown(Hull h, Station st, Source cite) {
            long c = cost(h);
            while (steel < c) { long more = steelSource.getAsLong(); if (more <= 0) return Optional.empty(); steel += more; }
            steel -= c;
            Ship s = new Ship(fleet.size() + 1, naming.apply(h), h, st, rows.get(h.id).tons(), cite);
            fleet.add(s); return Optional.of(s);
        }
        public List<Ship> fleet() { return List.copyOf(fleet); }
        public String report() {
            long tons = fleet.stream().mapToLong(Ship::tons).sum();
            int rate = fleet.stream().mapToInt(s -> rows.get(s.hull().id).rate()).sum();
            return String.format("fleet: %d ships, %,d tons afloat, +%d steel/s, %,d steel left", fleet.size(), tons, rate, steel);
        }
    }

    // ---- combinations, in perpetuity, starting with 1 -------------------------
    /** Fleets of n ships from k hull×station pairs, with repetition: C(k+n-1, n). Iterate n from 1 for as long as you like. */
    static java.math.BigInteger fleetsOf(int k, long n) {   // n is a long: the crossing below is past 2^31
        // C(k+n-1, n) == C(k+n-1, k-1): multiply over the shorter side, k-1 terms, so n may be in the billions.
        java.math.BigInteger acc = java.math.BigInteger.ONE;
        for (int i = 1; i <= k - 1; i++) acc = acc.multiply(java.math.BigInteger.valueOf((long) n + i)).divide(java.math.BigInteger.valueOf(i));
        return acc;
    }

    public static void main(String[] args) throws Exception {
        Path yardPath = Path.of(args.length > 0 ? args[0] : "data_and_tech/yard.json");
        List<YardRow> yard = readYard(yardPath);
        System.out.println("yard: " + yard.size() + " hull classes from " + yardPath);

        // the plugs, plugged: the game's cost curve; a steel source that gives 50,000 up to three times; RAN-style naming
        int[] taps = {3};
        Shipyard y = new Shipyard(yard, 20_000,
            (row, n) -> (long) Math.floor(row.cost() * Math.pow(1.25, n)),
            () -> taps[0]-- > 0 ? 50_000L : 0L,
            h -> "HMAS " + switch (h) { case PATROL -> "Attack"; case DESTROYER -> "Hobart"; case LIGHT_CRUISER -> "Adelaide"; case LIGHT_CARRIER -> "Melbourne"; case BATTLESHIP -> "Never"; case SUPERCARRIER -> "Australia"; });

        for (Hull h : Hull.values()) {
            Station st = switch (h) { case PATROL -> Station.MARKET_TIME; case DESTROYER -> Station.GUNLINE; case SUPERCARRIER -> Station.YANKEE; default -> Station.HOME; };
            Source cite = switch (h) { case BATTLESHIP -> Source.N1; case SUPERCARRIER -> Source.N2; default -> Source.N3; };
            y.layDown(h, st, cite).ifPresentOrElse(s -> System.out.println("laid down  " + s.manifest() + "  — " + h.describe()),
                                                   () -> System.out.printf("no steel   %-13s (%s)%n", h, h.describe()));
        }
        System.out.println(y.report());

        int k = Hull.values().length * Station.values().length;
        System.out.printf("%ncombinations: %d hull×station; fleets of n ships, from 1:%n", k);
        for (int n = 1; n <= 8; n++) System.out.printf("  n=%d  %,d%n", n, fleetsOf(k, n));
        java.math.BigInteger target = new java.math.BigInteger("2" + "0".repeat(255));
        // fleetsOf grows like n^(k-1)/(k-1)!, so the crossing is in the billions: search it, do not walk it.
        long lo = 1, hi = 1; while (fleetsOf(k, hi).compareTo(target) <= 0) hi *= 2;
        while (lo < hi) { long mid = (lo + hi) / 2; if (fleetsOf(k, mid).compareTo(target) > 0) hi = mid; else lo = mid + 1; }
        System.out.printf("  2 × 10^255 is passed at n=%,d ships — and n keeps going; that is the perpetuity, not the enum.%n", lo);
        System.out.println("\nsources cited: " + Arrays.toString(Source.values()));
    }
}

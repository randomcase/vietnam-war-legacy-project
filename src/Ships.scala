//> using scala 3.3.4
//> using dep com.lihaoyi::upickle:3.3.1

// Ships.scala — a class of ships, a shipyard that makes them from injected JSON,
// profiles and interfaces as Java enums in Scala, and the combinations.
//
//   scala-cli run src/Ships.scala -- data_and_tech/yard.json
//
// The yard JSON is the same shape as Venus/templates-war/australia.json#yard,
// so the idle game and this program build from one file.

package navalhistory

import upickle.default.*

// ---- interfaces (Java interfaces, in Scala: traits with no state) ----------

/** What a hull can do. Every Profile enum below implements it. */
trait Capability:
  def reachMiles: Int
  def projection: String

/** What a ship is for, as a role that a Java enum can carry. */
trait Duty:
  def station: String

// ---- Java enums in Scala 3 --------------------------------------------------
// `extends java.lang.Enum[T]` makes these real Java enums: values(), valueOf(),
// ordinal(), usable from Java and from any JSON reader by name.

/** Profiles: the three hulls the table measures against, plus what Australia sent. */
enum Profile(val reachMiles: Int, val projection: String) extends java.lang.Enum[Profile], Capability:
  case Scout        extends Profile(14,  "screens the fleet; six-inch guns")
  case Bombardment  extends Profile(24,  "shore bombardment; sixteen-inch guns, to the horizon")
  case AirWing      extends Profile(600, "a sovereign airbase; eighty-five aircraft, to Hanoi")
  case Escort       extends Profile(14,  "the gunline; five-inch guns and Tartar missiles")
  case Patrol       extends Profile(6,   "the coast; a twenty-millimetre gun and a crew of nineteen")

/** Duties: where the ship goes. */
enum Station(val station: String) extends java.lang.Enum[Station], Duty:
  case YankeeStation extends Station("Gulf of Tonkin, off the northeast coast")
  case DixieStation  extends Station("South China Sea, off Cam Ranh Bay")
  case Gunline       extends Station("the coastal strip, DMZ to the delta")
  case MarketTime    extends Station("the coastal blockade, patrol boats")
  case Home          extends Station("Garden Island, Sydney")

/** Hull classes, keyed to the yard JSON ids. */
enum Hull(val id: String, val profile: Profile) extends java.lang.Enum[Hull]:
  case PatrolBoat   extends Hull("patrol",        Profile.Patrol)
  case Destroyer    extends Hull("destroyer",     Profile.Escort)
  case LightCruiser extends Hull("cruiser",       Profile.Scout)
  case LightCarrier extends Hull("carrier-light", Profile.AirWing)
  case Battleship   extends Hull("battleship",    Profile.Bombardment)
  case Supercarrier extends Hull("supercarrier",  Profile.AirWing)

object Hull:
  def byId(id: String): Option[Hull] = Hull.values.find(_.id == id)

/** The sources as numbered in the brief, from 1. A Java enum is closed by definition; the
  * perpetuity the brief asks for lives in Combinations, which generates from closed sets without end. */
enum Source(val ref: String) extends java.lang.Enum[Source]:
  case N1 extends Source("USS Missouri (BB-63), Mighty Mo — Naval History and Heritage Command, DANFS")
  case N2 extends Source("https://en.wikipedia.org/wiki/USS_Constellation_(CV-64)")
  case N3 extends Source("https://en.wikipedia.org/wiki/Cambodia")
  case N4 extends Source("https://www.youtube.com/watch?v=tvMwcKQWmAE — video; not transcribed, this environment cannot hear it")
  case N5 extends Source("https://www.youtube.com/watch?v=-N6744h_JWs — video; not transcribed")
  case N6 extends Source("https://www.youtube.com/watch?v=_ecXNJP-ERY — video; not transcribed")
  case N7 extends Source("https://www.youtube.com/watch?v=e4FQj0we3nE&t=2693s — video; not transcribed")
  case N8 extends Source("https://www.youtube.com/watch?v=B1IZ-NTLORI — video; not transcribed")
  def number: Int = ordinal + 1

// ---- the yard, JSON-injectable ---------------------------------------------

/** One row of the yard JSON: what a hull costs and returns. */
case class YardRow(id: String, name: String, tons: Int, cost: Long, rate: Int, note: String) derives ReadWriter

/** A ship: a hull, a name, a pennant, a station, and the row it was built from. */
case class Ship(hull: Hull, name: String, pennant: Int, station: Station, tons: Int, profile: Profile) derives ReadWriter:
  def manifest: String = f"$pennant%03d  ${hull.toString}%-13s ${name}%-28s ${tons}%,7d t  ${profile}%-12s ${station.station}"

given ReadWriter[Hull]    = readwriter[String].bimap(_.name, Hull.valueOf)
given ReadWriter[Station] = readwriter[String].bimap(_.name, Station.valueOf)
given ReadWriter[Profile] = readwriter[String].bimap(_.name, Profile.valueOf)

/** The shipyard: reads the yard rows, keeps a steel balance, lays down hulls. */
class Shipyard(rows: Seq[YardRow], var steel: Long):
  private var next = 1
  private val built = scala.collection.mutable.ArrayBuffer.empty[Ship]
  private def row(h: Hull) = rows.find(_.id == h.id).getOrElse(throw IllegalArgumentException(s"no yard row for ${h.id}"))
  private def cost(h: Hull) = math.floor(row(h).cost * math.pow(1.25, built.count(_.hull == h))).toLong  // same curve as the game

  def canAfford(h: Hull): Boolean = steel >= cost(h)

  /** Lay down a hull, or return None if the steel is not there. */
  def layDown(h: Hull, name: String, station: Station): Option[Ship] =
    val c = cost(h)
    if steel < c then None
    else
      steel -= c
      val s = Ship(h, name, next, station, row(h).tons, h.profile)
      next += 1; built += s
      Some(s)

  def fleet: Seq[Ship] = built.toSeq
  def tonsAfloat: Long = built.map(_.tons.toLong).sum
  def ratePerSecond: Int = built.map(s => row(s.hull).rate).sum

// ---- combinations ------------------------------------------------------------

object Combinations:
  /** Every hull × station × profile that is a real ship: a hull carries its own profile,
    * so the real combinations are hull × station; the loose ones are hull × station × profile. */
  def real: Seq[(Hull, Station)] = for h <- Hull.values.toSeq; s <- Station.values.toSeq yield (h, s)
  def loose: Seq[(Hull, Station, Profile)] = for h <- Hull.values.toSeq; s <- Station.values.toSeq; p <- Profile.values.toSeq yield (h, s, p)

  /** Fleets of n ships drawn from the real combinations, with repetition: C(k+n-1, n). */
  def fleetsOf(n: Long): BigInt =   // n is a Long: the crossing below is past 2^31
    // C(k+n-1, n) == C(k+n-1, k-1): multiply over the shorter side, k-1 terms, so n may be in the billions.
    val k = real.size
    (1 until k).foldLeft(BigInt(1))((acc, i) => acc * (BigInt(n) + i) / i)

  /** The user's number, for scale: 2 × 10^255. How many ships in a fleet before the fleet count passes it? */
  def shipsToPass(target: BigInt): Long =
    // fleetsOf grows like n^(k-1)/(k-1)!; the crossing is in the billions, so search it rather than walk it.
    var hi = 1L; while fleetsOf(hi) <= target do hi *= 2
    var lo = 1L
    while lo < hi do { val mid = (lo + hi) / 2; if fleetsOf(mid) > target then hi = mid else lo = mid + 1 }
    lo

// ---- main --------------------------------------------------------------------

@main def ships(args: String*): Unit =
  val yardPath = args.headOption.getOrElse("data_and_tech/yard.json")
  val rows = read[Seq[YardRow]](java.nio.file.Files.readString(java.nio.file.Path.of(yardPath)))
  println(s"yard: ${rows.size} hull classes from $yardPath")

  // Australia, with the steel the game would take to reach a supercarrier, and a fleet in the order it actually bought.
  val yard = Shipyard(rows, steel = 300_000L)
  val orders = Seq(
    (Hull.PatrolBoat,   "HMAS Attack",     Station.MarketTime),
    (Hull.PatrolBoat,   "HMAS Aware",      Station.MarketTime),
    (Hull.Destroyer,    "HMAS Hobart",     Station.Gunline),
    (Hull.Destroyer,    "HMAS Perth",      Station.Gunline),
    (Hull.Destroyer,    "HMAS Brisbane",   Station.Gunline),
    (Hull.LightCarrier, "HMAS Melbourne",  Station.Home),
    (Hull.LightCruiser, "HMAS Hobart (I)", Station.Home),
    (Hull.Battleship,   "never",           Station.Home),
    (Hull.Supercarrier, "never",           Station.YankeeStation),
  )
  for (h, n, s) <- orders do
    yard.layDown(h, n, s) match
      case Some(ship) => println("laid down  " + ship.manifest)
      case None       => println(f"no steel   ${h}%-13s $n%-28s (needs more than ${yard.steel}%,d)")

  println(f"\nfleet: ${yard.fleet.size} ships, ${yard.tonsAfloat}%,d tons afloat, +${yard.ratePerSecond} steel/s, ${yard.steel}%,d steel left")
  java.nio.file.Files.writeString(java.nio.file.Path.of("data_and_tech/fleet.json"), write(yard.fleet, indent = 1) + "\n")
  println("fleet written to data_and_tech/fleet.json")

  println(s"\ncombinations: ${Combinations.real.size} real (hull × station), ${Combinations.loose.size} loose (hull × station × profile)")
  for n <- Seq(1, 5, 31, 63) do println(f"  fleets of $n%2d ships from the real combinations: ${Combinations.fleetsOf(n)}%,d")
  val target = BigInt("2" + "0" * 255)
  println(s"  the user's number, 2 × 10^255, is passed by fleets of ${Combinations.shipsToPass(target)} ships")

package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.MySQLProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import com.github.tototoshi.slick.MySQLJodaSupport._
  import org.joda.time.DateTime
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Brassica.schema ++ Brassicaassembly.schema ++ Brassicasample.schema ++ User.schema ++ Wheat.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Brassica
   *  @param id Database column id SqlType(INT), AutoInc
   *  @param geneid Database column geneid SqlType(VARCHAR), Length(255,true)
   *  @param chr Database column chr SqlType(TEXT)
   *  @param start Database column start SqlType(INT)
   *  @param end Database column end SqlType(INT)
   *  @param strand Database column strand SqlType(TEXT)
   *  @param pacid Database column pacid SqlType(TEXT)
   *  @param pfam Database column pfam SqlType(TEXT)
   *  @param panther Database column panther SqlType(TEXT)
   *  @param kog Database column kog SqlType(TEXT)
   *  @param kegg Database column kegg SqlType(TEXT)
   *  @param ko Database column ko SqlType(TEXT)
   *  @param go Database column go SqlType(TEXT)
   *  @param arabiName Database column arabi_name SqlType(TEXT)
   *  @param arabiSymbol Database column arabi_symbol SqlType(TEXT)
   *  @param arabiDefline Database column arabi_defline SqlType(TEXT) */
  case class BrassicaRow(id: Int, geneid: String, chr: String, start: Int, end: Int, strand: String, pacid: String, pfam: String, panther: String, kog: String, kegg: String, ko: String, go: String, arabiName: String, arabiSymbol: String, arabiDefline: String)
  /** GetResult implicit for fetching BrassicaRow objects using plain SQL queries */
  implicit def GetResultBrassicaRow(implicit e0: GR[Int], e1: GR[String]): GR[BrassicaRow] = GR{
    prs => import prs._
    BrassicaRow.tupled((<<[Int], <<[String], <<[String], <<[Int], <<[Int], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String]))
  }
  /** Table description of table brassica. Objects of this class serve as prototypes for rows in queries. */
  class Brassica(_tableTag: Tag) extends profile.api.Table[BrassicaRow](_tableTag, Some("saas_db"), "brassica") {
    def * = (id, geneid, chr, start, end, strand, pacid, pfam, panther, kog, kegg, ko, go, arabiName, arabiSymbol, arabiDefline) <> (BrassicaRow.tupled, BrassicaRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(geneid), Rep.Some(chr), Rep.Some(start), Rep.Some(end), Rep.Some(strand), Rep.Some(pacid), Rep.Some(pfam), Rep.Some(panther), Rep.Some(kog), Rep.Some(kegg), Rep.Some(ko), Rep.Some(go), Rep.Some(arabiName), Rep.Some(arabiSymbol), Rep.Some(arabiDefline))).shaped.<>({r=>import r._; _1.map(_=> BrassicaRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get, _14.get, _15.get, _16.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc */
    val id: Rep[Int] = column[Int]("id", O.AutoInc)
    /** Database column geneid SqlType(VARCHAR), Length(255,true) */
    val geneid: Rep[String] = column[String]("geneid", O.Length(255,varying=true))
    /** Database column chr SqlType(TEXT) */
    val chr: Rep[String] = column[String]("chr")
    /** Database column start SqlType(INT) */
    val start: Rep[Int] = column[Int]("start")
    /** Database column end SqlType(INT) */
    val end: Rep[Int] = column[Int]("end")
    /** Database column strand SqlType(TEXT) */
    val strand: Rep[String] = column[String]("strand")
    /** Database column pacid SqlType(TEXT) */
    val pacid: Rep[String] = column[String]("pacid")
    /** Database column pfam SqlType(TEXT) */
    val pfam: Rep[String] = column[String]("pfam")
    /** Database column panther SqlType(TEXT) */
    val panther: Rep[String] = column[String]("panther")
    /** Database column kog SqlType(TEXT) */
    val kog: Rep[String] = column[String]("kog")
    /** Database column kegg SqlType(TEXT) */
    val kegg: Rep[String] = column[String]("kegg")
    /** Database column ko SqlType(TEXT) */
    val ko: Rep[String] = column[String]("ko")
    /** Database column go SqlType(TEXT) */
    val go: Rep[String] = column[String]("go")
    /** Database column arabi_name SqlType(TEXT) */
    val arabiName: Rep[String] = column[String]("arabi_name")
    /** Database column arabi_symbol SqlType(TEXT) */
    val arabiSymbol: Rep[String] = column[String]("arabi_symbol")
    /** Database column arabi_defline SqlType(TEXT) */
    val arabiDefline: Rep[String] = column[String]("arabi_defline")

    /** Primary key of Brassica (database name brassica_PK) */
    val pk = primaryKey("brassica_PK", (id, geneid))
  }
  /** Collection-like TableQuery object for table Brassica */
  lazy val Brassica = new TableQuery(tag => new Brassica(tag))

  /** Entity class storing rows of table Brassicaassembly
   *  @param id Database column id SqlType(INT), AutoInc
   *  @param name Database column name SqlType(VARCHAR), Length(255,true)
   *  @param userid Database column userid SqlType(INT)
   *  @param sample Database column sample SqlType(VARCHAR), Length(255,true)
   *  @param createdata Database column createdata SqlType(VARCHAR), Length(255,true)
   *  @param state Database column state SqlType(INT) */
  case class BrassicaassemblyRow(id: Int, name: String, userid: Int, sample: String, createdata: String, state: Int)
  /** GetResult implicit for fetching BrassicaassemblyRow objects using plain SQL queries */
  implicit def GetResultBrassicaassemblyRow(implicit e0: GR[Int], e1: GR[String]): GR[BrassicaassemblyRow] = GR{
    prs => import prs._
    BrassicaassemblyRow.tupled((<<[Int], <<[String], <<[Int], <<[String], <<[String], <<[Int]))
  }
  /** Table description of table brassicaassembly. Objects of this class serve as prototypes for rows in queries. */
  class Brassicaassembly(_tableTag: Tag) extends profile.api.Table[BrassicaassemblyRow](_tableTag, Some("saas_db"), "brassicaassembly") {
    def * = (id, name, userid, sample, createdata, state) <> (BrassicaassemblyRow.tupled, BrassicaassemblyRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(name), Rep.Some(userid), Rep.Some(sample), Rep.Some(createdata), Rep.Some(state))).shaped.<>({r=>import r._; _1.map(_=> BrassicaassemblyRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc */
    val id: Rep[Int] = column[Int]("id", O.AutoInc)
    /** Database column name SqlType(VARCHAR), Length(255,true) */
    val name: Rep[String] = column[String]("name", O.Length(255,varying=true))
    /** Database column userid SqlType(INT) */
    val userid: Rep[Int] = column[Int]("userid")
    /** Database column sample SqlType(VARCHAR), Length(255,true) */
    val sample: Rep[String] = column[String]("sample", O.Length(255,varying=true))
    /** Database column createdata SqlType(VARCHAR), Length(255,true) */
    val createdata: Rep[String] = column[String]("createdata", O.Length(255,varying=true))
    /** Database column state SqlType(INT) */
    val state: Rep[Int] = column[Int]("state")

    /** Primary key of Brassicaassembly (database name brassicaassembly_PK) */
    val pk = primaryKey("brassicaassembly_PK", (id, name))
  }
  /** Collection-like TableQuery object for table Brassicaassembly */
  lazy val Brassicaassembly = new TableQuery(tag => new Brassicaassembly(tag))

  /** Entity class storing rows of table Brassicasample
   *  @param id Database column id SqlType(INT), AutoInc
   *  @param sample Database column sample SqlType(VARCHAR), Length(255,true)
   *  @param accountid Database column accountid SqlType(INT)
   *  @param createdata Database column createdata SqlType(VARCHAR), Length(255,true)
   *  @param state Database column state SqlType(INT) */
  case class BrassicasampleRow(id: Int, sample: String, accountid: Int, createdata: String, state: Int)
  /** GetResult implicit for fetching BrassicasampleRow objects using plain SQL queries */
  implicit def GetResultBrassicasampleRow(implicit e0: GR[Int], e1: GR[String]): GR[BrassicasampleRow] = GR{
    prs => import prs._
    BrassicasampleRow.tupled((<<[Int], <<[String], <<[Int], <<[String], <<[Int]))
  }
  /** Table description of table brassicasample. Objects of this class serve as prototypes for rows in queries. */
  class Brassicasample(_tableTag: Tag) extends profile.api.Table[BrassicasampleRow](_tableTag, Some("saas_db"), "brassicasample") {
    def * = (id, sample, accountid, createdata, state) <> (BrassicasampleRow.tupled, BrassicasampleRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(sample), Rep.Some(accountid), Rep.Some(createdata), Rep.Some(state))).shaped.<>({r=>import r._; _1.map(_=> BrassicasampleRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc */
    val id: Rep[Int] = column[Int]("id", O.AutoInc)
    /** Database column sample SqlType(VARCHAR), Length(255,true) */
    val sample: Rep[String] = column[String]("sample", O.Length(255,varying=true))
    /** Database column accountid SqlType(INT) */
    val accountid: Rep[Int] = column[Int]("accountid")
    /** Database column createdata SqlType(VARCHAR), Length(255,true) */
    val createdata: Rep[String] = column[String]("createdata", O.Length(255,varying=true))
    /** Database column state SqlType(INT) */
    val state: Rep[Int] = column[Int]("state")

    /** Primary key of Brassicasample (database name brassicasample_PK) */
    val pk = primaryKey("brassicasample_PK", (id, accountid))
  }
  /** Collection-like TableQuery object for table Brassicasample */
  lazy val Brassicasample = new TableQuery(tag => new Brassicasample(tag))

  /** Entity class storing rows of table User
   *  @param id Database column id SqlType(INT), AutoInc
   *  @param account Database column account SqlType(VARCHAR), Length(255,true)
   *  @param passowrd Database column passowrd SqlType(VARCHAR), Length(255,true) */
  case class UserRow(id: Int, account: String, passowrd: String)
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Int], e1: GR[String]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[Int], <<[String], <<[String]))
  }
  /** Table description of table user. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends profile.api.Table[UserRow](_tableTag, Some("saas_db"), "user") {
    def * = (id, account, passowrd) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(account), Rep.Some(passowrd))).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc */
    val id: Rep[Int] = column[Int]("id", O.AutoInc)
    /** Database column account SqlType(VARCHAR), Length(255,true) */
    val account: Rep[String] = column[String]("account", O.Length(255,varying=true))
    /** Database column passowrd SqlType(VARCHAR), Length(255,true) */
    val passowrd: Rep[String] = column[String]("passowrd", O.Length(255,varying=true))

    /** Primary key of User (database name user_PK) */
    val pk = primaryKey("user_PK", (id, account))
  }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))

  /** Entity class storing rows of table Wheat
   *  @param id Database column id SqlType(INT), AutoInc
   *  @param geneid Database column geneid SqlType(VARCHAR), Length(255,true)
   *  @param chr Database column chr SqlType(TEXT)
   *  @param start Database column start SqlType(INT)
   *  @param end Database column end SqlType(INT)
   *  @param stand Database column stand SqlType(TEXT)
   *  @param pfam Database column pfam SqlType(TEXT)
   *  @param panther Database column panther SqlType(TEXT)
   *  @param kog Database column kog SqlType(TEXT)
   *  @param kegg Database column kegg SqlType(TEXT)
   *  @param ko Database column ko SqlType(TEXT)
   *  @param go Database column go SqlType(TEXT)
   *  @param arabiName Database column arabi_name SqlType(TEXT)
   *  @param arabiSymbol Database column arabi_symbol SqlType(TEXT)
   *  @param arabiDefline Database column arabi_defline SqlType(TEXT)
   *  @param riceName Database column rice_name SqlType(TEXT)
   *  @param riceSymbol Database column rice_symbol SqlType(TEXT)
   *  @param riceDefline Database column rice_defline SqlType(TEXT) */
  case class WheatRow(id: Int, geneid: String, chr: String, start: Int, end: Int, stand: String, pfam: String, panther: String, kog: String, kegg: String, ko: String, go: String, arabiName: String, arabiSymbol: String, arabiDefline: String, riceName: String, riceSymbol: String, riceDefline: String)
  /** GetResult implicit for fetching WheatRow objects using plain SQL queries */
  implicit def GetResultWheatRow(implicit e0: GR[Int], e1: GR[String]): GR[WheatRow] = GR{
    prs => import prs._
    WheatRow.tupled((<<[Int], <<[String], <<[String], <<[Int], <<[Int], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String]))
  }
  /** Table description of table wheat. Objects of this class serve as prototypes for rows in queries. */
  class Wheat(_tableTag: Tag) extends profile.api.Table[WheatRow](_tableTag, Some("saas_db"), "wheat") {
    def * = (id, geneid, chr, start, end, stand, pfam, panther, kog, kegg, ko, go, arabiName, arabiSymbol, arabiDefline, riceName, riceSymbol, riceDefline) <> (WheatRow.tupled, WheatRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(geneid), Rep.Some(chr), Rep.Some(start), Rep.Some(end), Rep.Some(stand), Rep.Some(pfam), Rep.Some(panther), Rep.Some(kog), Rep.Some(kegg), Rep.Some(ko), Rep.Some(go), Rep.Some(arabiName), Rep.Some(arabiSymbol), Rep.Some(arabiDefline), Rep.Some(riceName), Rep.Some(riceSymbol), Rep.Some(riceDefline))).shaped.<>({r=>import r._; _1.map(_=> WheatRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get, _14.get, _15.get, _16.get, _17.get, _18.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc */
    val id: Rep[Int] = column[Int]("id", O.AutoInc)
    /** Database column geneid SqlType(VARCHAR), Length(255,true) */
    val geneid: Rep[String] = column[String]("geneid", O.Length(255,varying=true))
    /** Database column chr SqlType(TEXT) */
    val chr: Rep[String] = column[String]("chr")
    /** Database column start SqlType(INT) */
    val start: Rep[Int] = column[Int]("start")
    /** Database column end SqlType(INT) */
    val end: Rep[Int] = column[Int]("end")
    /** Database column stand SqlType(TEXT) */
    val stand: Rep[String] = column[String]("stand")
    /** Database column pfam SqlType(TEXT) */
    val pfam: Rep[String] = column[String]("pfam")
    /** Database column panther SqlType(TEXT) */
    val panther: Rep[String] = column[String]("panther")
    /** Database column kog SqlType(TEXT) */
    val kog: Rep[String] = column[String]("kog")
    /** Database column kegg SqlType(TEXT) */
    val kegg: Rep[String] = column[String]("kegg")
    /** Database column ko SqlType(TEXT) */
    val ko: Rep[String] = column[String]("ko")
    /** Database column go SqlType(TEXT) */
    val go: Rep[String] = column[String]("go")
    /** Database column arabi_name SqlType(TEXT) */
    val arabiName: Rep[String] = column[String]("arabi_name")
    /** Database column arabi_symbol SqlType(TEXT) */
    val arabiSymbol: Rep[String] = column[String]("arabi_symbol")
    /** Database column arabi_defline SqlType(TEXT) */
    val arabiDefline: Rep[String] = column[String]("arabi_defline")
    /** Database column rice_name SqlType(TEXT) */
    val riceName: Rep[String] = column[String]("rice_name")
    /** Database column rice_symbol SqlType(TEXT) */
    val riceSymbol: Rep[String] = column[String]("rice_symbol")
    /** Database column rice_defline SqlType(TEXT) */
    val riceDefline: Rep[String] = column[String]("rice_defline")

    /** Primary key of Wheat (database name wheat_PK) */
    val pk = primaryKey("wheat_PK", (id, geneid))
  }
  /** Collection-like TableQuery object for table Wheat */
  lazy val Wheat = new TableQuery(tag => new Wheat(tag))
}

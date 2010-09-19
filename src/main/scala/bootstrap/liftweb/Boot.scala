package bootstrap.liftweb

import net.liftweb.common._
import net.liftweb.http.{LiftRules, S}
import net.liftweb.sitemap.{SiteMap, Menu, Loc}
import net.liftweb.sitemap.Loc._
import net.liftweb.util.Props
import net.liftweb.mapper.{DB, ConnectionManager, Schemifier, DefaultConnectionIdentifier, ConnectionIdentifier}
import java.sql.{Connection, DriverManager}

import review.actor.MailActor
import review.api.ReviewApi
import review.model.{Review, Acceptance}

class Boot {
  def boot {
    MailActor.start

    if (!DB.jndiJdbcConnAvailable_?)
      DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)

    LiftRules.addToPackages("review")
    LiftRules.useXhtmlMimeType = false
    LiftRules.dispatch.prepend(ReviewApi.dispatch)

    val entries = Menu(Loc("Home", List("index"), "Main page")) :: Nil
    LiftRules.setSiteMap(SiteMap(entries: _*))

    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    Schemifier.schemify(true, Schemifier.infoF _, Review, Acceptance)

    S.addAround(DB.buildLoanWrapper)
  }
}

object DBVendor extends ConnectionManager {
  private var pool: List[Connection] = Nil
  private var poolSize = 0
  private val maxPoolSize = 4

  private def createOne: Box[Connection] = try {
    val driverName: String = Props.get("db.driver") openOr
    "org.h2.Driver"

    val dbUrl: String = Props.get("db.url") openOr
    "jdbc:h2:lift_example;AUTO_SERVER=TRUE"

    Class.forName(driverName)

    val dm = (Props.get("db.user"), Props.get("db.password")) match {
      case (Full(user), Full(pwd)) =>
	      DriverManager.getConnection(dbUrl, user, pwd)
      case _ => DriverManager.getConnection(dbUrl)
    }
    Full(dm)
  } catch {
    case e: Exception => e.printStackTrace; Empty
  }

  def newConnection(name: ConnectionIdentifier): Box[Connection] =
    synchronized {
      pool match {
	      case Nil if poolSize < maxPoolSize =>
	        val ret = createOne
          poolSize = poolSize + 1
          ret.foreach(c => pool = c :: pool)
          ret
	      case Nil => wait(1000L); newConnection(name)
	      case x :: xs => try {
          x.setAutoCommit(false)
          Full(x)
        } catch {
          case e => try {
            pool = xs
            poolSize = poolSize - 1
            x.close
            newConnection(name)
          } catch {
            case e => newConnection(name)
          }
        }
      }
    }

  def releaseConnection(conn: Connection): Unit = synchronized {
    pool = conn :: pool
    notify
  }
}



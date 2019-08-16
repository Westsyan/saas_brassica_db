package dao

import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import models.Tables._

import scala.concurrent.{ExecutionContext, Future}

class sampleDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit exec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def addSample(row:BrassicasampleRow) : Future[Unit] = {
    db.run(Brassicasample += row).map(_=>())
  }

  def getByPosition(userid:Int,sample:String) : Future[Seq[BrassicasampleRow]] = {
    db.run(Brassicasample.filter(_.accountid === userid).filter(_.sample === sample).result)
  }

  def updateState(id:Int,state:Int) : Future[Unit] = {
    db.run(Brassicasample.filter(_.id === id).map(_.state).update(state)).map(_=>())
  }

  def getAllSampleByUser(userId:Int) : Future[Seq[BrassicasampleRow]] = {
    db.run(Brassicasample.filter(_.accountid === userId).result)
  }

  def getById(id:Int) : Future[BrassicasampleRow] = {
    db.run(Brassicasample.filter(_.id === id).result.head)
  }

  def deleteById(id:Int) : Future[Unit] = {
    db.run(Brassicasample.filter(_.id === id).delete).map(_=>())
  }

}

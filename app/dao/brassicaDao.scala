package dao

import javax.inject.{Inject, Singleton}
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class brassicaDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit exec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def insert(row:Seq[BrassicaRow]) : Future[Unit] = {
    db.run(Brassica ++= row).map(_=>())
  }

  def getAll: Future[Seq[BrassicaRow]] = {
    db.run(Brassica.result)
  }

  def getById(id: Int): Future[BrassicaRow] = {
    db.run(Brassica.filter(_.id === id).result.head)
  }

  def getByGeneids(geneid:Seq[String]): Future[Seq[BrassicaRow]] = {
    db.run(Brassica.filter(_.geneid.inSetBind(geneid)).result)
  }

  def getByRegion(chr:String,start:Int,end:Int) : Future[Seq[BrassicaRow]] = {
    db.run(Brassica.filter(_.chr === chr).filter(_.start > start).filter(_.end < end).result)
  }

}

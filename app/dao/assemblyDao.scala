package dao

import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import models.Tables._

import scala.concurrent.{ExecutionContext, Future}

class assemblyDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit exec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]  {

  import profile.api._

  def addAssembly(row:BrassicaassemblyRow) : Future[Unit] = {
    db.run(Brassicaassembly += row).map(_=>())
  }

  def getByPosition(userid:Int,name:String) : Future[Seq[BrassicaassemblyRow]] = {
    db.run(Brassicaassembly.filter(_.userid === userid).filter(_.name === name).result)
  }

  def updateState(id:Int,state:Int) : Future[Unit] = {
    db.run(Brassicaassembly.filter(_.id === id).map(_.state).update(state)).map(_=>())
  }

  def getByUser(userid:Int) : Future[Seq[BrassicaassemblyRow]] = {
    db.run(Brassicaassembly.filter(_.userid === userid).result)
  }

  def getById(id:Int) : Future[BrassicaassemblyRow] = {
    db.run(Brassicaassembly.filter(_.id === id).result.head)
  }

  def deleteById(id:Int) : Future[Unit] = {
    db.run(Brassicaassembly.filter(_.id === id).delete).map(_=>())
  }

}

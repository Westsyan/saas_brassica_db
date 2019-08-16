package dao

import javax.inject.Inject
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class userDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit exec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]  {


  import profile.api._

  def  addUser(row:UserRow) : Future[Unit] = {
    db.run(User += row).map(_=>())
  }

  def checkUser(user:String,password:String) : Future[Seq[UserRow]] = {
    db.run(User.filter(_.account === user).filter(_.passowrd === password).result)
  }

  def getById(id:Int) : Future[UserRow] = {
    db.run(User.filter(_.id === id).result.head)
  }

  def updatePassword(id:Int,password:String) : Future[Unit] = {
    db.run(User.filter(_.id === id).map(_.passowrd).update(password)).map(_=>())
  }
}

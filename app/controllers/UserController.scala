package controllers

import dao.userDao
import javax.inject.{Inject, Singleton}
import models.Tables.UserRow
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Session}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class UserController @Inject()(cc: ControllerComponents,
                               userdao: userDao)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  case class UserData(user: String, password: String)

  val userForm = Form(
    mapping(
      "user" -> text,
      "password" -> text
    )(UserData.apply)(UserData.unapply)
  )


  def addUser = Action.async { implicit request =>
    val form = userForm.bindFromRequest.get
    val row = UserRow(0, form.user, form.password)
    userdao.addUser(row).map { x =>
      Ok(Json.toJson("success"))
    }
  }


  def signIn = Action.async { implicit request =>
    val form = userForm.bindFromRequest.get
    userdao.checkUser(form.user, form.password).map { x =>
      val valid = (x.length == 1).toString
      val id = if(x.length == 1) x.head.id else 0
      Ok(Json.obj("valid" -> valid,"id" -> id))
    }
  }

  def signInSuccess(path:String,id:Int) = Action.async{implicit request=>
    val session = new Session
    userdao.getById(id).map{x=>
      Redirect(path).withNewSession.withSession(session + ("userId" -> x.id.toString) + ("user"->x.account))
    }
  }

  case class PasswordData(user:String,password:String,password1:String)

  val PasswordForm = Form(
    mapping(
      "user" -> text,
      "password"-> text,
      "password1" -> text
    )(PasswordData.apply)(PasswordData.unapply)
  )

  def changePassword = Action{implicit request=>
    try{
      val form = PasswordForm.bindFromRequest.get
      val check = Await.result(userdao.checkUser(form.user,form.password),Duration.Inf)
      if(check.length == 1){
        val id = request.session.get("userId").get.toInt
        Await.result(userdao.updatePassword(id,form.password1),Duration.Inf)
        Ok(Json.obj("valid" -> "true"))
      } else {
        Ok(Json.obj("valid"->"false" ,"message" -> "Incorrect password!"))
      }
    }catch {
      case e : Exception => Ok(Json.obj("valid" ->"false","message" -> e.getMessage))
    }
  }

  def signout(path:String) = Action{implicit request=>
    Redirect(path).withNewSession
  }

}

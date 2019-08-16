package controllers

import java.io.File
import java.nio.file.Files

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import utils.{TableUtils, Utils}

import scala.concurrent.ExecutionContext

@Singleton
class UtilsController @Inject()(cc: ControllerComponents)
                               (implicit exec: ExecutionContext) extends AbstractController(cc) {


  case class egData(db: String)

  val egForm = Form(
    mapping(
      "db" -> text
    )(egData.apply)(egData.unapply)
  )

  def getEgGene: Action[AnyContent] = Action { implicit request =>
    val gene = TableUtils.brassicaMap.take(20).map(_.geneid)
    Ok(Json.toJson(gene))
  }


  def getImageByPhotoId(name: String) = Action { implicit request =>
    val ifModifiedSinceStr = request.headers.get(IF_MODIFIED_SINCE)

    val path = Utils.path + "/images/" + name

    val file = if (new File(path).exists()) {
      new File(path)
    } else {
      new File(Utils.path + "/images/zanwu.jpg")
    }

    val lastModifiedStr = file.lastModified().toString
    val MimeType = "image/jpg"
    val byteArray = Files.readAllBytes(file.toPath)
    if (ifModifiedSinceStr.isDefined && ifModifiedSinceStr.get == lastModifiedStr) {
      NotModified
    } else {
      Ok(byteArray).as(MimeType).withHeaders(LAST_MODIFIED -> file.lastModified().toString)
    }
  }


  def downloadPdf(file: String) = Action { implicit request =>
    val name = file.split("/").last
    val filename = new String(("attachment;filename=\"" + name + "\"").getBytes("GBK"), "ISO_8859_1")
    Ok.sendFile(new File(Utils.path + "/reference/" + file)).withHeaders(
      //缓存
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> filename,
      CONTENT_TYPE -> "application/x-download"
    )
  }

  def openPdf(file: String) = Action { implicit request =>
    val name = file.split("/").last
    val filename = new String(("filename=\"" + name + "\"").getBytes("GBK"), "ISO-8859-1")
    Ok.sendFile(new File(Utils.path + "/reference/" + file)).withHeaders(
      //缓存
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> filename,
      CONTENT_TYPE -> "text/plain"
    )
  }

  def getLastzTarfetExample = Action{implicit request=>
    val line = Utils.readFileToString(Utils.path + "/download/Chondrus_crispus.fasta")
    Ok(Json.toJson(line))
  }

  def downloadLastzTarfetExample = Action{implicit request=>
    Ok.sendFile(new File(Utils.path + "/download/Chondrus_crispus.fasta")).withHeaders(
      //缓存
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> "attachment; filename=target.fasta",
      CONTENT_TYPE -> "application/x-download"
    )
  }

  def getLastzQueryExample = Action{implicit request=>
    val line = Utils.readFileToString(Utils.path + "/download/Grateloupia_filicina.fasta")
    Ok(Json.toJson(line))
  }

  def downloadLastzQueryExample = Action{implicit request=>
    Ok.sendFile(new File(Utils.path + "/download/Grateloupia_filicina.fasta")).withHeaders(
      //缓存
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> "attachment; filename=query.fasta",
      CONTENT_TYPE -> "application/x-download"
    )
  }


}


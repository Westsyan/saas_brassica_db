package controllers

import dao.brassicaDao
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.Utils
import models.Tables._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Singleton
class InsertController @Inject()(brassicadao:brassicaDao ,cc: ControllerComponents) extends AbstractController(cc)  {


  def insert = Action{
    val x = Utils.readLines("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data/Brassica.flt")
    val x1 = Utils.readLines("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data/info.txt")

    val infoMap = x1.map{y=>
      val line = y.split("\t")
      (line(1),y)
    }.toMap

   val row = x.map{y=>
      val line = y.split("\t")
      val info = infoMap(line.last)+" "
      val line2 = info.split("\t")

      BrassicaRow(0,line.last,line.head,line(3).toInt,line(4).toInt,line(6),line2.head,line2(4),line2(5),line2(6),line2(7),line2(8),line2(9),
        line2(10),line2(11),line2(12).trim)
    }

    Await.result(brassicadao.insert(row),Duration.Inf)


    Ok(Json.toJson(1))
  }





}

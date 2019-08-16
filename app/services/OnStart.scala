package services

import dao.brassicaDao
import javax.inject._
import play.api.Logger
import utils.TableUtils

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Singleton
class OnStart@Inject()(brassicadao: brassicaDao) {


  Logger.info("开启成功！")

  TableUtils.brassicaMap = Await.result(brassicadao.getAll,Duration.Inf)

  Logger.info("wheatMap 添加成功")

  TableUtils.geneidToId = TableUtils.brassicaMap.map(x=> (x.geneid,x.id)).toMap

  Logger.info("geneidToId 添加成功")


}

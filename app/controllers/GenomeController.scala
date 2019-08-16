package controllers

import java.io.File

import dao.brassicaDao
import javax.inject.{Inject, Singleton}
import models.Tables.BrassicaRow
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils.{ExecCommand, TableUtils, Utils}
import utils.TableUtils.pageForm

import scala.concurrent.ExecutionContext

@Singleton
class GenomeController @Inject()(brassicadao: brassicaDao, cc: ControllerComponents)(implicit exec: ExecutionContext) extends AbstractController(cc) {


  def browseBeforeUS = Action { implicit request =>
    Ok(views.html.english.genome.browse())
  }

  def getAllBrassica = Action { implicit request =>
    val page = pageForm.bindFromRequest.get
    val x = TableUtils.brassicaMap
    val orderX = TableUtils.dealDataByPage(x, page)
    val total = orderX.size
    val tmpX = orderX.slice(page.offset, page.offset + page.limit)
    val row = getJson(tmpX)

    Ok(Json.obj("rows" -> row, "total" -> total))
  }

  def moreInfoUS(id: Int) = Action.async { implicit request =>
    brassicadao.getById(id).map { x =>
      Ok(views.html.english.genome.moreInfo(x))
    }
  }

  def moreInfoByGeneIdUS(geneId: String) = Action { implicit request =>
    val id = TableUtils.geneidToId(geneId)
    Redirect(routes.GenomeController.moreInfoUS(id))
  }

  def getSeqsByGeneid(geneid: String) = Action { implicit request =>
    val command = if (new File(Utils.windowsPath).exists()) {
      Array(Utils.path + "/tools/samtools-0.1.19/samtools.exe faidx " + Utils.path + "/blastData/cds/cds.fasta " + geneid,
        Utils.path + "/tools/samtools-0.1.19/samtools.exe faidx " + Utils.path + "/blastData/pep/pep.fasta " + geneid,
        Utils.path + "/tools/samtools-0.1.19/samtools.exe faidx " + Utils.path + "/blastData/trans/trans.fasta " + geneid)
    } else {
      Array("samtools faidx " + Utils.path + "/blastData/cds/cds.fasta " + geneid,
        "samtools faidx " + Utils.path + "/blastData/pep/pep.fasta " + geneid,
        "samtools faidx " + Utils.path + "/blastData/cds/trans/trans.fasta " + geneid)
    }

    val exec = new ExecCommand()
    exec.exec(command)
    val seqs = exec.getOutStr.split(">").tail.map(_.split("\n")).map(x=> ">" + x.head + "\n" + x.tail.mkString)
    Ok(Json.obj("cds" ->  seqs.head,"pep" ->  seqs(1),"trans" ->  seqs(2)))
  }

  def conditionSearchBeforeUS = Action{implicit request=>
    Ok(views.html.english.genome.search())
  }

  def getJson(x: Seq[BrassicaRow]): Seq[JsValue] = {
    x.map { z =>
      Json.obj("id" -> z.id, "geneid" -> z.geneid, "chr" -> z.chr, "start" -> z.start, "end" -> z.end,
        "strand" -> z.strand, "pfam" -> z.pfam, "panther" -> z.panther, "kog" -> z.kog, "kegg" -> z.kegg, "ko" -> z.ko,
        "go" -> z.go, "arabi_name" -> z.arabiName, "arabi_symbol" -> z.arabiSymbol, "arabi_defline" -> z.arabiDefline)
    }
  }

  case class geneIdData(gene: String)

  val geneIdForm = Form(
    mapping(
      "gene" -> text
    )(geneIdData.apply)(geneIdData.unapply)
  )

  def searchByIds : Action[AnyContent] = Action.async { implicit request =>
    val data = geneIdForm.bindFromRequest.get
    val id = data.gene.split(",").map(_.trim).distinct
    brassicadao.getByGeneids(id).map{x=>
      Ok(Json.toJson(getJson(x)))
    }
  }

  case class regionData(chr: String, start: String, end: String)

  val regionForm = Form(
    mapping(
      "chr" -> text,
      "start" -> text,
      "end" -> text
    )(regionData.apply)(regionData.unapply)
  )

  def searchByRegion = Action.async { implicit request =>
    val data = regionForm.bindFromRequest.get
    brassicadao.getByRegion(data.chr, data.start.toInt, data.end.toInt).map{x=>
      Ok(Json.toJson(getJson(x)))
    }
  }


}

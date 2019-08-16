package controllers

import java.io.File
import java.nio.file.Files

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents, RangeResult}
import utils.Utils.windowsPath
import utils.{ExecCommand, Utils}

import scala.concurrent.ExecutionContext

@Singleton
class DownloadController @Inject()(cc: ControllerComponents)(implicit exec: ExecutionContext) extends AbstractController(cc) {



  def downloadBeforeUS = Action{implicit request=>
    Ok(views.html.english.download.download())
  }

  def downloadExample(example: String) = Action { implicit request =>
    val filename = "\"" + example + "\""
    Ok.sendFile(new File(Utils.path + "/example/" + example)).withHeaders(
      //缓存
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> ("attachment; filename=" + filename),
      CONTENT_TYPE -> "application/x-download"
    )
  }

  //断点续传
  def linkIgvData(path: String) = Action { implicit request =>
    RangeResult.ofFile(new File(Utils.path + "/igvData/" + path), request.headers.get(RANGE), Some("application/octet-stream"))
  }

  def download(file: String) = Action { implicit request =>
    val name = file.split("/").last
    val filename = "\"" + name + "\""
    Ok.sendFile(new File(Utils.path + "/download/" + file)).withHeaders(
      //缓存
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> ("attachment; filename=" + filename),
      CONTENT_TYPE -> "application/x-download"
    )
  }


  def downloadBlastByRange(name: String, range: String, blastType: String) = Action {
    val fasta = blastType match {
      case "gene" => "cds/cds.fasta"
      case "genome" => "genome/brassica.fasta"
      case "pep" => "pep/pep.fasta"
    }

    val execCommand = new ExecCommand

    range.split("Range").tail.foreach { x =>
      val r = x.trim.split(":")
      val ra = name + ":" + r.last.split("to").map(_.trim).mkString("-")
      val command = if (new File(windowsPath).exists()) {
        Utils.path + "/tools/samtools-0.1.19/samtools.exe faidx " + Utils.path + "/blastData/" + fasta + " " + ra
      } else {
        "samtools faidx " + Utils.path + "/blastData/" + fasta + " " + ra
      }
      execCommand.exec(command)
    }

    val seq = execCommand.getOutStr

    Ok(seq).withHeaders(
      //缓存
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> ("attachment; filename=" + name + ".fasta"),
      CONTENT_TYPE -> "application/x-download"
    )

  }

  def downloadBlastByName(name: String, blastType: String) = Action {

    val fasta = blastType match {
      case "gene" => "cds/cds.fasta"
      case "genome" => "genome/brassica.fasta"
      case "pep" => "pep/pep.fasta"
    }

    val execCommand = new ExecCommand
    val command = if (new File(windowsPath).exists()) {
      Utils.path + "/tools/samtools-0.1.19/samtools.exe faidx " + Utils.path + "/blastData/" + fasta + " " + name
    } else {
      "samtools faidx " + Utils.path + "/blastData/" + fasta + " " + name
    }
    val tmpDir = Files.createTempDirectory("tmpDir").toString
    val seqFile = new File(tmpDir + "/seq.fasta")
    execCommand.execo(command, seqFile)
    Ok.sendFile(seqFile).withHeaders(
      //缓存
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> ("attachment; filename=" + name + ".fasta"),
      CONTENT_TYPE -> "application/x-download"
    )

  }


}

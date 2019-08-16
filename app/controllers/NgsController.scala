package controllers

import java.io.File

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.ByteString
import dao._
import javax.inject.{Inject, Singleton}
import models.Tables._
import org.apache.commons.io.FileUtils
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.core.parsers.Multipart.{FileInfo, FilePartHandler}
import utils.{CompactAlgorithm, ExecCommand, Utils}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class NgsController @Inject()(cc: ControllerComponents, sampledao: sampleDao,assemblydao: assemblyDao)(implicit exec: ExecutionContext) extends AbstractController(cc) {


  def upload = Action { implicit request =>
    Ok(views.html.english.platform.ngs.upload())
  }

  private def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType) =>

      val file = new File(Utils.tmpPath, Utils.random)
      val path = file.toPath
      val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
      val accumulator: Accumulator[ByteString, IOResult] = Accumulator(fileSink)
      accumulator.map {
        case IOResult(count, status) =>
          FilePart(partName, filename, contentType, file)
      }
  }

  def getFastq(path: String, outputPath: String, name: String): Unit = {
    val suffix = name.split('.').last
    if (suffix == "gz") {
      FileUtils.writeStringToFile(new File(outputPath), "")
      CompactAlgorithm.unGzipFile(path, outputPath)
      new File(path).delete()
    } else {
      FileUtils.moveFile(new File(path), new File(outputPath))
    }
  }


  case class paraData(sample: String, encondingType: String, stepMethod: String, adapter: Option[String],
                      seed_mismatches: Option[Int], palindrome_clip_threshold: Option[Int],
                      simple_clip_threshold: Option[Int], trimMethod: String, window_size: Option[Int],
                      required_quality: Option[Int], minlenMethod: String, minlen: Option[Int],
                      leadingMethod: String, leading: Option[Int], trailingMethod: String, trailing: Option[Int],
                      cropMethod: String, crop: Option[Int], headcropMethod: String, headcrop: Option[Int])


  val paraForm = Form(
    mapping(
      "sample" -> text,
      "encondingType" -> text,
      "stepMethod" -> text,
      "adapter" -> optional(text),
      "seed_mismatches" -> optional(number),
      "palindrome_clip_threshold" -> optional(number),
      "simple_clip_threshold" -> optional(number),
      "trimMethod" -> text,
      "window_size" -> optional(number),
      "required_quality" -> optional(number),
      "minlenMethod" -> text,
      "minlen" -> optional(number),
      "leadingMethod" -> text,
      "leading" -> optional(number),
      "trailingMethod" -> text,
      "trailing" -> optional(number),
      "cropMethod" -> text,
      "crop" -> optional(number),
      "headcropMethod" -> text,
      "headcrop" -> optional(number)
    )(paraData.apply)(paraData.unapply)
  )

  case class quantData(bias: String, bootstrap_samples: String, seed: String, lib_type: String)

  val quantForm = Form(
    mapping(
      "bias" -> text,
      "bootstrap_samples" -> text,
      "seed" -> text,
      "lib_type" -> text
    )(quantData.apply)(quantData.unapply)
  )

  def uploadData = Action(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
    val path = Utils.path
    val file = request.body.files
    val paradata = paraForm.bindFromRequest.get
    val userId = request.session.get("userId").head.toInt
    val type1 = paradata.encondingType
    val type2 = "-" + type1
    val sample = paradata.sample
    val sa = BrassicasampleRow(0, sample, userId, Utils.date2, 0)
    Await.result(sampledao.addSample(sa), Duration.Inf)
    val row = Await.result(sampledao.getByPosition(userId, sample), Duration.Inf).head
    try {
      val run = Future {
        val outPath = Utils.outPath(userId, row.id)
        val in1 = file.head.ref.getPath
        val name1 = file.head.filename
        val in2 = file(1).ref.getPath
        val name2 = file(1).filename
        val out1 = outPath + "/raw.data_1.fastq"
        val out2 = outPath + "/raw.data_2.fastq"
        getFastq(in1, out1, name1)
        getFastq(in2, out2, name2)
        new File(outPath + "/tmp").mkdir()
        val deploy = mutable.Buffer(row.id, type1, paradata.stepMethod, paradata.adapter.get, paradata.seed_mismatches.getOrElse(2),
          paradata.palindrome_clip_threshold.getOrElse(30), paradata.simple_clip_threshold.getOrElse(10), paradata.trimMethod,
          paradata.window_size.getOrElse(20), paradata.required_quality.getOrElse(20), paradata.minlenMethod, paradata.minlen.getOrElse(35),
          paradata.leadingMethod, paradata.leading.getOrElse(3), paradata.trailingMethod, paradata.trailing.getOrElse(20),
          paradata.cropMethod, paradata.crop.getOrElse(0), paradata.headcropMethod, paradata.headcrop.getOrElse(0))

        FileUtils.writeLines(new File(outPath + "/deploy.txt"), deploy.asJava)
        runCmd1(row, quantForm.bindFromRequest.get)

      }
    } catch {
      case e: Exception => Await.result(sampledao.updateState(row.id, 2), Duration.Inf)
    }

    Ok(Json.obj("valid" -> "true"))
  }

  def runCmd1(row: BrassicasampleRow, form: quantData) = {
    val outPath = Utils.outPath(row.accountid, row.id)
    val deploy = FileUtils.readLines(new File(outPath, "deploy.txt")).asScala
    val command1 = PETrimmomatic(outPath, deploy)
    val command2 = s"${Utils.path}/tools/FastQC/fastqc $outPath/raw.data_1.fastq $outPath/raw.data_2.fastq -o $outPath/"
    val command3 = s"perl ${Utils.path}/tools/kallisto/kallisto_quant.pl -reference ${Utils.path}/ngsData/kallisto/trans_index.fasta " +
      s"-forward $outPath/raw.data_1.fastq -reverse $outPath/raw.data_2.fastq -bias ${form.bias} -bootstrap_samples ${form.bootstrap_samples} -seed ${form.seed} " +
      s"-lib_type ${form.lib_type} -cpu 2 -isoforms $outPath/isoforms_expr.txt -genes $outPath/genes_expr.txt"

    val command = new ExecCommand
    val tmp = outPath + "/tmp"

    command.exect(Array(command1, command2, command3), outPath)

    if (command.isSuccess) {
      FileUtils.deleteDirectory(new File(tmp))
      Await.result(sampledao.updateState(row.id, 1), Duration.Inf)
      getLog(outPath, command.getErrStr)

    } else {
      FileUtils.deleteDirectory(new File(tmp))
      Await.result(sampledao.updateState(row.id, 2), Duration.Inf)
      FileUtils.writeStringToFile(new File(outPath, "log.txt"), command.getErrStr)
    }
  }

  case class sampleData(sample: String)

  val sampleForm = Form(
    mapping(
      "sample" -> text
    )(sampleData.apply)(sampleData.unapply)
  )

  def checkSample = Action.async { implicit request =>
    val userId = request.session.get("userId").head.toInt
    val data = sampleForm.bindFromRequest.get
    val sample = data.sample
    sampledao.getByPosition(userId, sample).map { y =>
      val valid = if (y.isEmpty) {
        "true"
      } else {
        "false"
      }
      Ok(Json.obj("valid" -> valid))
    }
  }

  def getLog(outPath: String, output: String): Unit = {
    val trans = output.split("Input Read Pairs")
    val input = ("Input Read Pairs" + trans.drop(1).head).split("Both Surviving")
    val both = ("Both Surviving" + input.drop(1).head).split("Forward Only Surviving")
    val forward = ("Forward Only Surviving" + both.drop(1).head).split("Reverse Only Surviving")
    val reverse = ("Reverse Only Surviving" + forward.drop(1).head).split("Dropped")
    val drop = ("Dropped" + reverse.drop(1).head).split("TrimmomaticPE")
    val PE = "TrimmomaticPE" + drop.drop(1).head
    val tri = mutable.Buffer(input.head, both.head, forward.head, reverse.head, drop.head, PE)
    val logs = tri
    FileUtils.writeLines(new File(outPath, "log.txt"), logs.asJava)
  }

  def PETrimmomatic(outPath: String, data: mutable.Buffer[String]): String = {
    val path = Utils.path + "/tools"
    val in1 = outPath + "/raw.data_1.fastq"
    val in2 = outPath + "/raw.data_2.fastq"
    val tmpDir = outPath + "/tmp"
    val out1 = outPath + "/r1_paired_out.fastq"
    val unout1 = tmpDir + "/r1_unpaired_out.fastq"
    val out2 = outPath + "/r2_paired_out.fastq"
    val unout2 = tmpDir + "/r2_unpaired_out.fastq"
    var command = s"java -jar $path/Trimmomatic-0.39/trimmomatic-0.39.jar PE -threads 1 " +
      s"${data(1)} $in1 $in2 $out1 $unout1 $out2 $unout2 "
    if (data(2) == "yes") {
      val adapter = path + "/Trimmomatic-0.39/adapters/" + data(3)
      command += s"ILLUMINACLIP:$adapter:${data(4)}:${data(5)}:${data(6)} "
    }
    if (data(7) == "yes") {
      command += s"SLIDINGWINDOW:${data(8)}:${data(9)} "
    }
    if (data(10) == "yes") {
      command += s"MINLEN:${data(11)} "
    }
    if (data(12) == "yes") {
      command += s"LEADING:${data(13)} "
    }
    if (data(14) == "yes") {
      command += s"TRAILING:${data(15)} "
    }
    if (data(16) == "yes") {
      command += s"CROP:${data(17)} "
    }
    if (data(18) == "yes") {
      command += s"HEADCROP:${data(19)} "
    }
    command
  }

  def samplePage = Action { implicit request =>
    Ok(views.html.english.platform.ngs.sample())
  }

  def getAllSample = Action.async { implicit request =>
    val userId = request.session.get("userId").head.toInt
    sampledao.getAllSampleByUser(userId).map { x =>
      val json = x.map { y =>
        Json.obj("id" -> y.id, "sample" -> y.sample, "createdate" -> y.createdata, "state" -> y.state)
      }
      Ok(Json.toJson(json))
    }
  }

  def downloadSample(id: Int, filename: String) = Action { implicit request =>
    val row = Await.result(sampledao.getById(id), Duration.Inf)
    val file = new File(Utils.path + "/ngsData/user/" + row.accountid + "/sample/" + row.id + "/" + filename)
    Ok.sendFile(file).withHeaders(
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> ("attachment; filename=" + row.sample + "_" + filename),
      CONTENT_TYPE -> "application/x-download"
    )
  }

  def downloadAssembly(id: Int, filename: String) = Action { implicit request =>
    val row = Await.result(assemblydao.getById(id), Duration.Inf)
    val file = new File(Utils.path + "/ngsData/user/" + row.userid + "/assembly/" + row.id + "/" + filename)
    Ok.sendFile(file).withHeaders(
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> ("attachment; filename=" + row.name + "_" + filename),
      CONTENT_TYPE -> "application/x-download"
    )
  }

  def openFastqc(name: String) = Action { implicit request =>
    val userId = request.session.get("userId").head.toInt
    val html = FileUtils.readLines(new File(Utils.path + "/ngsData/user/" + userId + "/sample/" + name)).asScala
    Ok(html.mkString("\n")).as(HTML)
  }

  def openLogFile(id: Int): Action[AnyContent] = Action { implicit request =>
    val row = Await.result(sampledao.getById(id), Duration.Inf)
    val path = Utils.outPath(row.accountid, id)
    val json = getLogFile(path)
    Ok(Json.toJson(json))
  }

  def openAssemblyLogFile(id: Int): Action[AnyContent] = Action { implicit request =>
    val row = Await.result(assemblydao.getById(id), Duration.Inf)
    val path = Utils.path + "/ngsData/user/" + row.userid + "/assembly/" +  id
    val json = getLogFile(path)
    Ok(Json.toJson(json))
  }

  def getLogFile(path:String) = {
    val log = FileUtils.readLines(new File(path, "/log.txt")).asScala
    var html =
      """
        |<style>
        |   .logClass{
        |       font-size : 16px;
        |       font-weight:normal;
        |   }
        |</style>
      """.stripMargin
    html += "<b class='logClass'>" + log.mkString("</b><br><b class='logClass'>") + "</b>"
     Json.obj("log" -> html)
  }

  def deleteSample(id: Int) = Action { implicit request =>
    try {
      Await.result(sampledao.deleteById(id), Duration.Inf)
      val userId = request.session.get("userId").head.toInt
      Utils.deleteDirectory(Utils.path + "/ngsData/user/" + userId + "/sample/" + id)
      Ok(Json.obj("valid" -> "true"))
    } catch {
      case e: Exception => Ok(Json.obj("valid" -> "false", "message" -> e.getMessage))
    }
  }

  def deleteAssembly(id: Int) = Action { implicit request =>
    try {
      Await.result(assemblydao.deleteById(id), Duration.Inf)
      val userId = request.session.get("userId").head.toInt
      Utils.deleteDirectory(Utils.path + "/ngsData/user/" + userId + "/assembly/" + id)
      Ok(Json.obj("valid" -> "true"))
    } catch {
      case e: Exception => Ok(Json.obj("valid" -> "false", "message" -> e.getMessage))
    }
  }

  def assembly = Action { implicit request =>
    Ok(views.html.english.platform.ngs.assembly())
  }

  def getAllSampleName = Action.async { implicit request =>
    val userId = request.session.get("userId").head.toInt
    sampledao.getAllSampleByUser(userId).map { x =>
      val json = x.filter(_.state == 1).map(_.sample)
      Ok(Json.toJson(json))
    }
  }

  case class assemblyData(name: String, sample: Seq[String])

  val assemblyForm = Form(
    mapping(
      "name" -> text,
      "sample" -> seq(text)
    )(assemblyData.apply)(assemblyData.unapply)
  )

  def runAssembly = Action { implicit request =>
    val form = assemblyForm.bindFromRequest.get
    val userId = request.session.get("userId").head.toInt
    val row = BrassicaassemblyRow(0, form.name, userId,form.sample.mkString(","), Utils.date2, 0)

    Await.result(assemblydao.addAssembly(row), Duration.Inf)
    val id = Await.result(assemblydao.getByPosition(userId, form.name), Duration.Inf).head.id
    val path = Utils.path + "/ngsData/user/" + userId + "/assembly/" + id
    val samplePath = Utils.path + "/ngsData/user/" + userId + "/sample/"
    try {
      new File(path).mkdirs()
      val sampleMap = Await.result(sampledao.getAllSampleByUser(userId), Duration.Inf).map(x => (x.sample, x.id)).toMap
      val samples = form.sample.map { x =>
        x + " "  + samplePath  + sampleMap(x) + "/genes_expr.txt "
      }.mkString(" ")
      val command = s"perl ${Utils.path}/tools/kallisto/kallisto_abundance_estimates_to_matrix.pl " + samples +
        s" --tpm_out $path/kallisto_TPM.matrix.txt --fpkm_out $path/kallisto_FPKM.matrix.txt --count_out $path/kallisto_counts.matrix.txt"

      val tmp = path + "/tmp"
      new File(tmp).mkdirs()
      val exec = new ExecCommand()
      exec.exect(command,tmp)

      if(exec.isSuccess){
        Await.result(assemblydao.updateState(id,1),Duration.Inf)
      }else{
        Await.result(assemblydao.updateState(id,2),Duration.Inf)
      }
      FileUtils.deleteDirectory(new File(tmp))
      FileUtils.writeStringToFile(new File(path, "log.txt"), exec.getErrStr)

    }catch{
      case e:Exception =>
        Await.result(assemblydao.updateState(id,2),Duration.Inf)
        FileUtils.writeStringToFile(new File(path, "log.txt"), e.getMessage)
    }

    Ok(Json.obj("valid" -> "true"))
  }

  case class nameData(name: String)

  val nameForm = Form(
    mapping(
      "name" -> text
    )(nameData.apply)(nameData.unapply)
  )

  def checkName = Action.async { implicit request =>
    val userId = request.session.get("userId").head.toInt
    val data = nameForm.bindFromRequest.get
    val name = data.name
    assemblydao.getByPosition(userId, name).map { y =>
      val valid = if (y.isEmpty) {
        "true"
      } else {
        "false"
      }
      Ok(Json.obj("valid" -> valid))
    }
  }

  def getAllAssembly = Action.async { implicit request =>
    val userId = request.session.get("userId").head.toInt
    assemblydao.getByUser(userId).map { x =>
      val json = x.map { y =>
        Json.obj("id" -> y.id, "name" -> y.name,"sample" ->y.sample, "createdate" -> y.createdata, "state" -> y.state)
      }
      Ok(Json.toJson(json))
    }
  }

  def result = Action{implicit request=>
    Ok(views.html.english.platform.ngs.result())
  }

}

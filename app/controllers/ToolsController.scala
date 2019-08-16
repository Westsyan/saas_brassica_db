package controllers

import java.io.File
import java.nio.file.Files

import javax.inject.{Inject, Singleton}
import org.apache.commons.io.FileUtils
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import utils.{ExecCommand, Utils}

import scala.collection.JavaConverters._
import scala.collection.mutable

@Singleton
class ToolsController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {


  def blastBeforeUS: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.english.tools.blast())
  }

  case class blastData(blastType: String, method: String, queryText: String, db: String, evalue: String, wordSize: String, maxTargetSeqs: String)

  val blastForm = Form(
    mapping(
      "blastType" -> text,
      "method" -> text,
      "queryText" -> text,
      "db" -> text,
      "evalue" -> text,
      "wordSize" -> text,
      "maxTargetSeqs" -> text
    )(blastData.apply)(blastData.unapply)
  )

  def blastRun = Action(parse.multipartFormData) { implicit request =>
    val data = blastForm.bindFromRequest.get
    val tmpDir = Files.createTempDirectory("tmpDir").toString
    val seqFile = new File(tmpDir, "seq.fa")
    data.method match {
      case "text" =>
        FileUtils.writeStringToFile(seqFile, data.queryText)
      case "file" =>
        val file = request.body.file("file").get
        file.ref.moveTo(seqFile, replace = true)
    }

    val outXml = new File(tmpDir, "out.xml")
    val outHtml = new File(tmpDir, "out.html")
    val outTable = new File(tmpDir, "table.xls")
    val execCommand = new ExecCommand

    val blastType = data.blastType match {
      case "blastn" => "blastn"
      case "blastp" => "blastp"
      case "blastx" => "blastx"
    }

    var db = ""

    val seqlist = tmpDir + "/seqlist.txt"

    if (data.db.contains("genome")) {
      db = Utils.path + "/blastData/genome/brassica"
      FileUtils.copyFile(new File(Utils.path + "/enrichData/chr.txt"), new File(seqlist))
    } else if (data.db.contains("cds")) {
      db = Utils.path + "/blastData/cds/brassica"
      FileUtils.copyFile(new File(Utils.path + "/enrichData/gene.txt"), new File(seqlist))
    } else {
      db = Utils.path + "/blastData/pep/brassica"
      FileUtils.copyFile(new File(Utils.path + "/enrichData/gene.txt"), new File(seqlist))
    }

    val blast2Html = data.blastType match {
      case "blastx" => "blastx2html"
      case _ => "blast2html"
    }

    val command1 = Utils.path + "/tools/ncbi-blast-2.6.0+/bin/%s%s -query ".format(blastType, Utils.suffix) + seqFile.getAbsolutePath + " -db " +
      db + " -seqidlist " + seqlist + " -outfmt 5 -evalue " + data.evalue + " -max_target_seqs " + data.maxTargetSeqs +
      " -word_size " + data.wordSize + " -out " + outXml.getAbsolutePath
    val jinja = if (data.db.contains("genome")) "blastnGenome" else blastType
    val command2 = "python " + Utils.path + s"/tools/blast2html/$blast2Html.py -i " + outXml.getAbsolutePath + " -o " +
      outHtml.getAbsolutePath +
      " --template %s/tools/blast2html/%s.jinja".format(Utils.path, jinja)

    val btt = Utils.path + "/tools/Blast2table -format 10 -xml " + outXml.getAbsolutePath + " -header -top > " + outTable.getAbsoluteFile
    val bttf = new File(tmpDir, "blastToTable.sh")


    FileUtils.writeStringToFile(bttf, btt)
    val command3 = "sh " + bttf.getAbsoluteFile
    execCommand.exect(Array(command1, command2), tmpDir)
    if (execCommand.isSuccess) {
      val html = FileUtils.readFileToString(outHtml)
      // val excel = FileUtils.readFileToString(outTable)
      val excel = ""
      // Utils.deleteDirectory(tmpDir)

      Ok(Json.obj("html" -> tmpDir.replaceAll("\\\\", "/"), "excel" -> excel))
    } else {
      Utils.deleteDirectory(tmpDir)
      Ok(Json.obj("valid" -> "false", "message" -> execCommand.getErrStr))
    }
  }

  def blastResultBefore(path: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(views.html.english.tools.blastResult(path))
  }

  def blastResult(path: String): Action[AnyContent] = Action {
    implicit request =>
      val html = Utils.readFileToString(new File(path + "/out.html"))
      Utils.deleteDirectory(path)
      Ok(Json.obj("html" -> (html + "\n" + Utils.scriptHtml)))
  }


  def seqFetchBeforeUS: Action[AnyContent] = Action {
    implicit request =>
      Ok(views.html.english.tools.seqFetch())
  }

  case class regData(species: String, region: String)

  val regForm = Form(
    mapping(
      "species" -> text,
      "region" -> text
    )(regData.apply)(regData.unapply)
  )

  def seqRegion = Action {
    implicit request =>
      val data = regForm.bindFromRequest.get
      val tmpDir = Files.createTempDirectory("tmpDir").toString
      val outFile = new File(tmpDir, "data.txt")
      val execCommand = new ExecCommand
      val command = if (new File(Utils.windowsPath).exists()) {
        Utils.path + "/tools/samtools-0.1.19/samtools.exe faidx " + Utils.path + "/blastData/" + data.species + " " + data.region
      } else {
        "samtools faidx " + Utils.path + "/blastData/" + data.species + " " + data.region
      }
      execCommand.execo(command, outFile)
      if (execCommand.isSuccess) {
        val dataStr = FileUtils.readFileToString(outFile)
        Utils.deleteDirectory(tmpDir)
        Ok(Json.obj("valid" -> "true", "data" -> dataStr))
      } else {
        Utils.deleteDirectory(tmpDir)
        Ok(Json.obj("valid" -> "false", "message" -> execCommand.getErrStr))
      }
  }

  def enrichmentBeforeUS = Action {
    implicit request =>
      Ok(views.html.english.tools.enrichment())
  }

  case class enrichData(method: String, dataType: String, gene: String, db: String, pValue: String)

  val enrichForm = Form(
    mapping(
      "method" -> text,
      "dataType" -> text,
      "gene" -> text,
      "db" -> text,
      "pValue" -> text
    )(enrichData.apply)(enrichData.unapply)
  )


  def enrichment = Action(parse.multipartFormData) {
    implicit request =>
      val data = enrichForm.bindFromRequest.get
      val tmpDir = Files.createTempDirectory("tmpDir").toString
      val seqFile = new File(tmpDir, "tmp.txt")
      data.dataType match {
        case "text" =>
          val geneId = data.gene.split(",").map(_.trim).distinct.toBuffer
          FileUtils.writeLines(seqFile, geneId.asJava)
        case "file" =>
          val file = request.body.file("file").get
          file.ref.moveTo(seqFile, replace = true)
      }

      data.method match {
        case "kegg" => kegg(data, tmpDir, seqFile.getAbsolutePath)
        case "go" => go(data, tmpDir, seqFile.getAbsolutePath)
      }
  }

  def kegg(data: enrichData, tmpDir: String, study: String): Result = {

    val population = Utils.enrichPath + "/gene.txt"
    val association = Utils.enrichPath + "kegg.txt"

    val output = new File(tmpDir, "KEGG_enrichment.txt")
    val o = output.getAbsolutePath
    // println(study,population,association,m,n,o,c,pval)
    //QVALUE在unix转译文本后可以使用
    val execCommand = new ExecCommand
    val command = "perl " + Utils.path + "/tools/identify.pl -study=" + study + " -population=" + population +
      " -association=" + association + " -m=b" + " -n=BH" + " -o=" + o + " -c=5" + " -maxp=" + data.pValue
    execCommand.exect(command, tmpDir)

    val (status, jsons) = if (execCommand.isSuccess) {
      val keggInfo = Utils.readLines(output)
      val json = keggInfo.filter(_.split("\t").length == 9).map {
        x =>
          val all = x.split("\t")
          val hyper = "<a target='_blank' href='" + all(8) + "'>linked</a><a style='display: none'>" + all(8) + "</a>"
          Json.obj("term" -> all.head, "database" -> all(1), "id" -> all(2), "input_num" -> all(3), "back_num" -> all(4),
            "p-value" -> all(5), "correct_pval" -> all(6), "input" -> all(7), "hyperlink" -> hyper)
      }.drop(1)
      (1, json)
    } else (0, mutable.Buffer[JsObject]())

    Utils.deleteDirectory(tmpDir)
    Ok(Json.obj("status" -> status, "data" -> jsons, "message" -> execCommand.getErrStr))
  }

  def go(data: enrichData, tmpDir: String, study: String): Result = {

    val population = Utils.enrichPath + "/gene.txt"
    val association = Utils.enrichPath + "go.txt"

    val o = new File(tmpDir, "GO_enrichment.txt")
    val execCommand = new ExecCommand
    val command = "python " + Utils.path + "/tools/goatools-0.5.7/scripts/find_enrichment.py --alpha=0.05 " +
      " --pval=" + data.pValue + " --output " + o.getAbsolutePath + " " + study + " " + population + " " + association
    execCommand.exect(command, tmpDir)
    val (status, jsons) = if (execCommand.isSuccess) {
      val goInfo = Utils.readLines(o).drop(1)
      val json = goInfo.map {
        x =>
          val all = x.split("\t")
          val goLink = "<a target='_blank' href='http://amigo.geneontology.org/amigo/term/" + all(0) + "'>" + all(0) + "</a>"
          Json.obj("id" -> goLink, "enrichment" -> all(1), "description" -> all(2), "ratio_in_study" -> all(3),
            "ratio_in_pop" -> all(4), "p_uncorrected" -> all(5), "p_bonferroni" -> all(6), "p_holm" -> all(7),
            "p_sidak" -> all(8), "p_fdr" -> all(9), "namespace" -> all(10), "genes_in_study" -> all(11))
      }
      (1, json)
    } else (0, mutable.Buffer[JsObject]())
    Utils.deleteDirectory(tmpDir)
    Ok(Json.obj("status" -> status, "data" -> jsons, "message" -> execCommand.getErrStr))
  }

  def geneWiseBeforeUS = Action {
    implicit request =>
      Ok(views.html.english.tools.genewise())
  }


  case class GeneWiseData(proteinSeq: String, dnaSeq: String, para: String, pretty: String, genes: String, trans: String,
                          cdna: String, embl: String, ace: String, gff: String, diana: String, init: String,
                          splice: String, random: String, alg: String)

  val GeneWiseForm = Form(
    mapping(
      "proteinSeq" -> text,
      "dnaSeq" -> text,
      "para" -> text,
      "pretty" -> text,
      "genes" -> text,
      "trans" -> text,
      "cdna" -> text,
      "embl" -> text,
      "ace" -> text,
      "gff" -> text,
      "diana" -> text,
      "init" -> text,
      "splice" -> text,
      "random" -> text,
      "alg" -> text
    )(GeneWiseData.apply)(GeneWiseData.unapply)
  )

  def genewise = Action { implicit request =>
    val data = GeneWiseForm.bindFromRequest.get
    val tmpDir = Files.createTempDirectory("tmpDir").toString
    val proteinFile = tmpDir + "/pro.fa"
    FileUtils.writeStringToFile(new File(proteinFile), data.proteinSeq)
    val dnaFile = tmpDir + "/dna.fa"
    FileUtils.writeStringToFile(new File(dnaFile), data.dnaSeq)

    val outFile = new File(tmpDir, "result.txt")

    val execCommand = new ExecCommand
    val command = "genewise " + proteinFile + " " + dnaFile + " -kbyte 4000 " + " -init " + data.init + " -null  " +
      data.random + " -alg " + data.alg + data.para + data.pretty + data.genes + data.trans + data.cdna + data.embl +
      data.ace + data.gff + data.diana + data.splice
    execCommand.execot(command, outFile, tmpDir)

    if (execCommand.isSuccess) {
      val result = FileUtils.readFileToString(outFile)
      Utils.deleteDirectory(tmpDir)
      Ok(Json.toJson(result))
    } else {
      Utils.deleteDirectory(tmpDir)
      Ok(Json.obj("valid" -> "false", "message" -> execCommand.getErrStr))
    }
  }


  def muscleBeforeUS = Action {
    implicit request =>
      Ok(views.html.english.tools.muscle())
  }

  case class MuscleData(method: String, queryText: String, tree: String)

  val MuscleForm = Form(
    mapping(
      "method" -> text,
      "queryText" -> text,
      "tree" -> text
    )(MuscleData.apply)(MuscleData.unapply)
  )

  def muscle = Action(parse.multipartFormData) { implicit request =>
    val data = MuscleForm.bindFromRequest.get
    val tmpDir = Files.createTempDirectory("tmpDir").toString
    val seqFile = new File(tmpDir, "seq.fa")
    data.method match {
      case "text" =>
        FileUtils.writeStringToFile(seqFile, data.queryText)
      case "file" =>
        val file = request.body.file("file").get
        file.ref.moveTo(seqFile, replace = true)
    }
    val outFile = tmpDir + "/result.txt"
    val execCommand = new ExecCommand
    val commandBuffer = "muscle -in " + seqFile.getAbsolutePath + " -verbose -log  -fasta  -out " + outFile + " -group "
    val treeFile = new File(tmpDir, "tree.txt")
    val tree = data.tree match {
      case "none" => " "
      case "tree1" => " -tree1 " + treeFile.getAbsolutePath
      case "tree2" => " -tree2 " + treeFile.getAbsolutePath
    }
    val command1 = commandBuffer + tree
    execCommand.exect(command1, tmpDir)
    if (execCommand.isSuccess) {
      val trees = if (!treeFile.exists()) "" else FileUtils.readFileToString(treeFile)
      val result = FileUtils.readFileToString(new File(outFile))
      Ok(Json.obj("out" -> result, "tree" -> trees))
    } else {
      Utils.deleteDirectory(tmpDir)
      Ok(Json.obj("valid" -> "false", "message" -> execCommand.getErrStr))
    }

  }

  def lastzBeforeUS = Action {
    implicit request =>
      Ok(views.html.english.tools.lastz())
  }


  def primerBeforeUS = Action { implicit request =>
    Ok(views.html.english.tools.primer())
  }

  case class primerData(chr: String, start: String, end: String, SEQUENCE_TARGET: String, PRIMER_PRODUCT_SIZE_RANGE: String,
                        PRIMER_OPT_SIZE: String,
                        PRIMER_MAX_SIZE: String, PRIMER_MIN_SIZE: String, PRIMER_OPT_TM: String, PRIMER_MAX_TM: String,
                        PRIMER_MIN_TM: String, PRIMER_OPT_GC_PERCENT: String, PRIMER_MAX_GC: String, PRIMER_MIN_GC: String,
                        PRIMER_MAX_NS_ACCEPTED: String, PRIMER_MAX_POLY_X: String, PRIMER_INTERNAL_MAX_POLY_X: String,
                        PRIMER_MAX_SELF_ANY: String, PRIMER_MAX_SELF_END: String, PRIMER_PAIR_MAX_COMPL_ANY: String,
                        PRIMER_PAIR_MAX_COMPL_END: String)

  val primerForm = Form(
    mapping(
      "chr" -> text,
      "start" -> text,
      "end" -> text,
      "SEQUENCE_TARGET" -> text,
      "PRIMER_PRODUCT_SIZE_RANGE" -> text,
      "PRIMER_OPT_SIZE" -> text,
      "PRIMER_MAX_SIZE" -> text,
      "PRIMER_MIN_SIZE" -> text,
      "PRIMER_OPT_TM" -> text,
      "PRIMER_MAX_TM" -> text,
      "PRIMER_MIN_TM" -> text,
      "PRIMER_OPT_GC_PERCENT" -> text,
      "PRIMER_MAX_GC" -> text,
      "PRIMER_MIN_GC" -> text,
      "PRIMER_MAX_NS_ACCEPTED" -> text,
      "PRIMER_MAX_POLY_X" -> text,
      "PRIMER_INTERNAL_MAX_POLY_X" -> text,
      "PRIMER_MAX_SELF_ANY" -> text,
      "PRIMER_MAX_SELF_END" -> text,
      "PRIMER_PAIR_MAX_COMPL_ANY" -> text,
      "PRIMER_PAIR_MAX_COMPL_END" -> text
    )(primerData.apply)(primerData.unapply)
  )

  def primer = Action { implicit request =>

    var valid = "false"
    var message = ""
    var result = ""
    try {
      val data = primerForm.bindFromRequest.get
      val tmp = Files.createTempDirectory("tmpDir").toString
      val outFile = new File(tmp, "data.txt")
      val exec = new ExecCommand()
      val command1 = if (new File(Utils.windowsPath).exists()) {
        Utils.path + "/tools/samtools-0.1.19/samtools.exe faidx " + Utils.path + "/blastData/genome/wheat.fasta " + data.chr
      } else {
        "samtools faidx " + Utils.path + "/blastData/genome/wheat.fasta " + data.chr
      }
      exec.execo(command1, outFile)

      if (exec.isSuccess) {
        val fasta = FileUtils.readLines(outFile).asScala
        val input =
          s"""SEQUENCE_ID=example
             |SEQUENCE_TEMPLATE=${fasta.tail.mkString.slice(data.start.toInt, data.end.toInt)}
             |SEQUENCE_TARGET=${data.SEQUENCE_TARGET}
             |PRIMER_TASK=generic
             |PRIMER_PICK_LEFT_PRIMER=1
             |PRIMER_PICK_INTERNAL_OLIGO=1
             |PRIMER_PICK_RIGHT_PRIMER=1
             |PRIMER_OPT_SIZE=${data.PRIMER_OPT_SIZE}
             |PRIMER_MIN_SIZE=${data.PRIMER_MIN_SIZE}
             |PRIMER_MAX_SIZE=${data.PRIMER_MAX_SIZE}
             |PRIMER_OPT_TM=${data.PRIMER_OPT_TM}
             |PRIMER_MAX_TM=${data.PRIMER_MAX_TM}
             |PRIMER_MIN_TM=${data.PRIMER_MIN_TM}
             |PRIMER_OPT_GC_PERCENT=${data.PRIMER_OPT_GC_PERCENT}
             |PRIMER_MAX_GC=${data.PRIMER_MAX_GC}
             |PRIMER_MIN_GC=${data.PRIMER_MIN_GC}
             |PRIMER_MAX_NS_ACCEPTED=${data.PRIMER_MAX_NS_ACCEPTED}
             |PRIMER_MAX_POLY_X=${data.PRIMER_MAX_POLY_X}
             |PRIMER_INTERNAL_MAX_POLY_X=${data.PRIMER_INTERNAL_MAX_POLY_X}
             |PRIMER_MAX_SELF_ANY=${data.PRIMER_MAX_SELF_ANY}
             |PRIMER_MAX_SELF_END=${data.PRIMER_MAX_SELF_END}
             |PRIMER_PAIR_MAX_COMPL_ANY=${data.PRIMER_PAIR_MAX_COMPL_ANY}
             |PRIMER_PAIR_MAX_COMPL_END=${data.PRIMER_PAIR_MAX_COMPL_END}
             |PRIMER_PRODUCT_SIZE_RANGE=${data.PRIMER_PRODUCT_SIZE_RANGE.split(",").mkString(" ")}
             |P3_FILE_FLAG=1
             |SEQUENCE_INTERNAL_EXCLUDED_REGION=${data.SEQUENCE_TARGET}
             |PRIMER_EXPLAIN_FLAG=1
             |=""".stripMargin

        FileUtils.writeLines(new File(tmp, "setting.txt"), input.split("\n").map(_.trim).toBuffer.asJava)

        val command = s"primer3_core -format_output -default_version=2 -io_version=4 -strict_tags -output=$tmp/out.txt " +
          s"-error=$tmp/error.txt $tmp/setting.txt"

        exec.exect(command, tmp)

        if (exec.isSuccess) {
          try {
            valid = "true"
            result = getPrimerResult(tmp, data.start.toInt)
          } catch {
            case e: Exception =>
              valid = "fasle"
              message = Utils.readLines(tmp + "/out.txt").mkString
          }
        } else {
          message = FileUtils.readFileToString(new File(tmp, "error.txt"))
        }
      } else {
        message = exec.getErrStr
      }
      Utils.deleteDirectory(tmp)
    } catch {
      case e: UnsupportedOperationException => message = "Parameter cannot be empty!"
    }

    Ok(Json.obj("valid" -> valid, "message" -> message, "result" -> result))
  }


  def getPrimerResult(tmp: String, start: Int): String = {
    val f = FileUtils.readLines(new File(tmp, "out.txt")).asScala

    val i = f.filter(_.contains("INTERNAL OLIGO EXCLUDED REGIONS")).head
    val i2 = f.filter(_.contains("KEYS (in order of precedence):")).head

    val s = f.slice(f.indexOf(i) + 2, f.indexOf(i2))

    val seqs = s.grouped(3).map { x =>
      val seq = x.head.split(" ")
      val c1 = "<tr><td class='length'>" + (seq.init.last.toInt + start) + "</td> " +
        seq.last.split("").map(y => "<td>" + y + "</td>").mkString + "</tr>"

      val index = x.head.indexOf(seq.last.head) - 1

      val c2 = "<tr>" + x(1).split("").drop(index).map {
        case y if y == "*" => "<td class='blue'>" + y + "</td>"
        case y if y == "x" => "<td class='blue'>" + y + "</td>"
        case y if y == ">" => "<td class='orange'>" + y + "</td>"
        case y if y == "<" => "<td class='orange'>" + y + "</td>"
        case y if y == "^" => "<td class='orange'>" + y + "</td>"
        case y: String => "<td>" + y + "</td>"
      }.mkString + "</tr>"
      val c3 = "<tr><td>" + x.last + "</td></tr>"
      c1 + c2 + c3
    }.mkString

    val head = "OLIGO            start  len      tm     gc%  any_th  3'_th hairpin seq"
    val t1 = f.indexOf(head) + 1
    val t2 = f.filter(_.contains("SEQUENCE SIZE")).head
    val t = f.slice(t1, f.indexOf(t2))

    val r1 = t.map { x =>
      val td = x.split("\\s+")
      "<tr class='tableText'><td>" + td.take(2).mkString("\t") + "</td><td>" + (td(2).toInt + start) + "</td>" +
        td.drop(3).map { y =>
          "<td>" + y + "</td>"
        }.mkString + "</tr>"
    }.mkString

    val a1 = f.indexOf("ADDITIONAL OLIGOS") + 3
    val a2 = f.indexOf("Statistics")

    val rw = f.slice(a1, a2).grouped(5).map(_.take(3)).toArray
    val r2 = rw.map { x =>
      val array = x.map(_.split("\\s+"))
      val a = array.head.tail +: array.tail

      a.map { td =>
        "<tr><td>" + td.head + "</td><td>" + td.slice(2, 4).mkString("\t") + "</td><td>" + (td(3).toInt + start) + "</td>" +
          td.drop(4).map { y =>
            "<td>" + y + "</td>"
          }.mkString + "</tr>"
      }.mkString
    }.mkString

    val seqSizeIndex = f.indexOf(t2)
    val seqSize = "<p>" + f(seqSizeIndex) + "</p><p>" + f(seqSizeIndex + 1) + "</p><br>"

    val productSize = "<p>" + f(seqSizeIndex + 3) + "</p><p>" + f(seqSizeIndex + 4) + "</p><p>" + f(seqSizeIndex + 5) + "</p>"

    val statIndex = f.indexOf("Statistics")

    val stat = f.slice(statIndex + 4, statIndex + 7).map { x =>
      "<tr>" + x.split("\\s+").map(y => "<td>" + y + "</td>").mkString + "</tr>"
    }.mkString

    val pairIndex = f.indexOf("Pair Stats:")
    val pairStats = f.slice(pairIndex + 1, pairIndex + 3).map(x => "<p>" + x + "</p>")


    val html =
      s"""
         |        <hr>
         |        <h3>Primer design results:</h3>
         |        <div class="table-responsive panel-collapse collapse in " >
         |            <table class="table" style="word-wrap: break-word">
         |                <thead>
         |                    <tr class="tableHead">
         |                        <th data-field="oligos">Oligos</th>
         |                        <th data-field="start">Start position</th>
         |                        <th data-field="length">Length</th>
         |                        <th data-field="tm">Tm</th>
         |                        <th data-field="gc">GC percent</th>
         |                        <th data-field="any">Self any</th>
         |                        <th data-field="end">Self end</th>
         |                        <th data-field="end">Hairpin</th>
         |                        <th data-field="seq">Sequence</th>
         |                    </tr>
         |                </thead>
         |                <tbody>
         |                ${r1.mkString}
         |                </tbody>
         |            </table>
         |        </div>
         |        $seqSize
         |
         |        <div>
         |
         |            <h3>Primer design results in the sequence:</h3>
         |
         |            $productSize
         |            <table>
         |                <tbody id="seq">
         |                ${seqs.mkString}
         |
         |                </tbody>
         |            </table>
         |        </div>
         |
         |
         |        <h3>Note:</h3>
         |        <table>
         |            <tbody>
         |                <tr>
         |                    <td class="blue" style="width: 80px;">xxxxxxx</td>
         |                    <td>excluded region for internal oligo</td>
         |                </tr>
         |                <tr>
         |                    <td class="orange">>>>>>></td>
         |                    <td>left primer</td>
         |                </tr>
         |                <tr>
         |                    <td class="orange"><<<<<<</td>
         |                    <td>right primer</td>
         |                </tr>
         |                <tr>
         |                    <td class="orange">^^^^^^^^</td>
         |                    <td>right primer</td>
         |                </tr>
         |
      |            </tbody>
         |        </table>
         |
      |
      |        <h3>ADDITIONAL OLIGOS:</h3>
         |        <div class="table-responsive panel-collapse collapse in " >
         |            <table class="table table-hover table-striped" style="word-wrap: break-word">
         |                <thead>
         |                    <tr>
         |                    <th></th>
         |                        <th>Oligos</th>
         |                        <th>Start position</th>
         |                        <th>Length</th>
         |                        <th>Tm</th>
         |                        <th>GC percent</th>
         |                        <th>Self any</th>
         |                        <th>Self end</th>
         |                        <th>Hairpin</th>
         |                        <th>Sequence</th>
         |                    </tr>
         |                </thead>
         |                <tbody id="additionalTable">
         |                ${r2.mkString}
         |
      |                </tbody>
         |            </table>
         |        </div>
         |
      |        <h3>Statistics:</h3>
         |        <div class="table-responsive panel-collapse collapse in " >
         |            <table class="table table-hover table-striped" style="word-wrap: break-word">
         |                <thead>
         |                    <tr>
         |                        <th>Direction</th>
         |                        <th>Considered</th>
         |                        <th>Too many Ns</th>
         |                        <th>In target</th>
         |                        <th>In excel reg</th>
         |                        <th>Not ok reg</th>
         |                        <th>Bad GC%</th>
         |                        <th>No GC clamp</th>
         |                        <th>TM too low</th>
         |                        <th>TM too high</th>
         |                        <th>High any_th compl</th>
         |                        <th>High 3'_th compl</th>
         |                        <th>Highhair-pin</th>
         |                        <th>Poly x</th>
         |                        <th>High end stab</th>
         |                        <th>Ok</th>
         |                    </tr>
         |                </thead>
         |                <tbody id="additionalTable">
         |                $stat
         |
      |
      |                </tbody>
         |            </table>
         |        </div>
         |
      |
      |        <h3>Pair Stats:</h3>
         |       ${pairStats.mkString}
    """.stripMargin

    html

  }

  case class LastzData(targetText: Option[String], queryText: Option[String])

  val lastzForm = Form(
    mapping(
      "targetText" -> optional(text),
      "queryText" -> optional(text)
    )(LastzData.apply)(LastzData.unapply)
  )


  def lastz = Action(parse.multipartFormData) { implicit request =>
    val data = lastzForm.bindFromRequest.get
    val tmpDir = Files.createTempDirectory("tmpDir").toString
    val targetFile = new File(tmpDir, "target.fa")
    data.targetText.map { targetText =>
      FileUtils.writeStringToFile(targetFile, targetText, "UTF-8")
    }.getOrElse {
      val file = request.body.file("targetFile").get
      file.ref.moveTo(targetFile, replace = true)
    }
    val queryFile = new File(tmpDir, "query.fa")
    data.queryText.map { queryText =>
      FileUtils.writeStringToFile(queryFile, queryText, "UTF-8")
    }.getOrElse {
      val file = request.body.file("queryFile").get
      file.ref.moveTo(queryFile, replace = true)
    }
    val command = s"perl ${Utils.path}/tools/mummer/SV_finder_2.2.1/bin/SV_finder.pl $targetFile $queryFile --prefix data --tfix target --qfix query --nocomp --outdir output --locate"
    println(command)
    val execCommand = new ExecCommand()
    execCommand.exect(command, tmpDir)
    val json = if (execCommand.isSuccess) {
      val file1 = new File(tmpDir, "output/02.Lastz/Target-Query.parallel.png")
      val base641 = Utils.getBase64Str(file1)
      val file2 = new File(tmpDir, "output/02.Lastz/Target-Query.xoy.png")
      val base642 = Utils.getBase64Str(file2)
      val axtFile = new File(tmpDir, "output/02.Lastz/all.axt")
      val axtString = FileUtils.readFileToString(axtFile)
      Json.obj("base641" -> base641, "base642" -> base642, "axtString" -> axtString)
    } else {
      Json.obj("valid" -> "false", "message" -> execCommand.getErrStr)
    }
    // FileUtils.deleteDirectory(new File(tmpDir))
    Ok(json)
  }

}

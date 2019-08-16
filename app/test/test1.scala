package test

import java.io.File

import org.apache.commons.io.FileUtils
import utils.Utils

import scala.collection.JavaConverters._

object test1 {

  def main(args: Array[String]): Unit = {
    val pep = Utils.readFileToString("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data/BrapaFPsc_277_v1.3.transcript_primaryTranscriptOnly.fa")

   val row = pep.split(">").tail.map{x=>
      val line = x.split("\n")
      val head =  line.head.split('.').take(2).mkString(".")
      head + "\n" + line.tail.mkString("\n")
    }.mkString("\n>")

    FileUtils.writeStringToFile(new File("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data/trans.fasta"),row)
  }

  def flyPEP = {
    val pep = Utils.readFileToString("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data/BrapaFPsc_277_v1.3.protein_primaryTranscriptOnly.fa")

    val row = pep.split(">").tail.map{x=>
      val line = x.split("\n")
      val head =  line.head.split('.').take(2).mkString(".")
      head + "\n" + line.tail.mkString("\n")
    }.mkString("\n>")

    FileUtils.writeStringToFile(new File("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data/pep.fasta"),row)



  }

  def fltCDS = {
    val cds = Utils.readFileToString("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data/BrapaFPsc_277_v1.3.cds_primaryTranscriptOnly.fa")

    val row = cds.split(">").tail.map{x=>
      val line = x.split("\n")
      val head =  line.head.split('.').take(2).mkString(".")
      head + "\n" + line.tail.mkString("\n")
    }.mkString("\n>")

    FileUtils.writeStringToFile(new File("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data/cds.fasta"),row)

  }

  def fltInfo = {

    val x = Utils.readLines("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data/BrapaFPsc_277_v1.3.annotation_info.txt")
    val row =  x.tail.map { x =>
      val line = x.split("\t")
      (line(1), x)
    }.toMap.values.toBuffer
    FileUtils.writeLines(new File("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data//info.txt"),row.asJava)

  }

  def fltGFF = {
    val x = Utils.readLines("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data/BrapaFPsc_277_v1.3.gene_exons.gff3")
    val row =  x.filter(_.contains("gene")).map{x=>
      val c = x.split("\t")
      val last = c.last.split("=").last

      c.init.mkString("\t") + "\t" + last
    }
    FileUtils.writeLines(new File("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data//Brassica.flt"),row.asJava)
  }

  def getJBGFF = {
    val x = Utils.readLines("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data/BrapaFPsc_277_v1.3.gene_exons.gff3")


    val row =  x.drop(3).filter(!_.contains("gene")).map{x=>
      val c = x.split("\t")
      val last = c.last.split(";")
      val last1 = if(c.last.contains("Name")){
        last.find(_.contains("ID")).get
      }else{
        last.find(_.contains("Parent")).get
      }
      c.init.mkString("\t") + "\t" + last1
    }


    FileUtils.writeLines(new File("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data//Brassica.gff"),row.asJava)

  }

}

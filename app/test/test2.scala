package test

import java.io.File

import org.apache.commons.io.FileUtils
import utils.Utils

import scala.collection.JavaConverters._

object test2 {

  def main(args: Array[String]): Unit = {

    val line = Utils.readFileToString("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data/BrapaFPsc_277_v1.3.transcript.fa")

    val row = line.split(">").tail.map{x=>
      val r = x.split("\n")
      ">" + r.head.split(" ").head + "\n" + r.tail.mkString("\n")
    }.mkString("\n")


    FileUtils.writeStringToFile(new File("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data/data.fasta"),row)


  }


  def t1 = {

    val line = Utils.readLines("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data/BrapaFPsc_277_v1.3.gene_exons.gff3")

    val lines = line.filter(x=> x.contains("mRNA"))

    val row = lines.map{x=>
      val c = x.split("\t").last.split(";").filter(_.contains("Name")).head.split("=").last

      c.take(c.lastIndexOf('.')) + "\t" + c
    }

    FileUtils.writeLines(new File("D:\\四川省农科院小麦-油菜数据库\\油菜数据库\\annotation\\data/data.txt"),row.asJava)

  }
}

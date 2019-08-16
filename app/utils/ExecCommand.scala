package utils

import java.io.File

import scala.sys.process._

class ExecCommand {
  var isSuccess: Boolean = false
  val err = new StringBuilder
  val out = new StringBuilder
  val log = ProcessLogger(out append _ append "\n", err append _ append "\n")

  def exec(command: String): Unit = {
    val exitCode = command ! log
    println(getInfo(command, exitCode))
    if (exitCode == 0) isSuccess = true
  }

  def exec(command1: String, command2: String) : Unit = {
    val exitCode = (command1 #&& command2) ! log
    if (exitCode == 0) isSuccess = true
  }

  def exec(command: Array[String]) : Unit = {
    val exitArray = command.map { x =>
      val exitCode = Process(x) ! log
      println(getInfo(x, exitCode))
      exitCode
    }
    val invalid = exitArray.count(!_.equals(0))
    if (invalid == 0) isSuccess = true

  }

  def execo(command: String, outFile: File) : Unit = {
    val exitCode = (command #> outFile) ! log
    println(getInfo(command, exitCode))
    if (exitCode == 0) isSuccess = true
  }

  def execot(command: String, outFile: File,tmpDir: String) : Unit = {
    val exitCode = (Process(command,new File(tmpDir)) #> outFile) ! log
    println(getInfo(command, exitCode))
    if (exitCode == 0) isSuccess = true
  }

  def exect(command: String, tmpDir: String) : Unit = {
    val exitCode = Process(command, new File(tmpDir)) ! log
    println(getInfo(command, exitCode))
    if (exitCode == 0) isSuccess = true
  }


  def exect(command: Array[String], tmpDir: String) : Unit = {
    import scala.util.control.Breaks._
    var exit = 0
    breakable({
      for (x <- command) {
        val exitCode = Process(x, new File(tmpDir)) ! log
        println(getInfo(x, exitCode))
        exit += exitCode
        if (exit != 0) break()
      }
    })

    if (exit == 0) isSuccess = true
  }

  def getInfo(command: String, exitCode: Int): String = {
    val commands = command.split(" ").take(3)
    val proname = if (commands.head == "python" || commands.head == "perl" || commands.head == "sh") {
      commands.drop(1).head.split("/").last
    } else if (commands.head == "java") {
      commands.drop(2).head.split("/").last
    } else {
      commands.head.split("/").last
    }
    val info = if (exitCode == 0) {
      s"[info] Programe $proname run success!"
    } else {
      s"[error] Programe $proname run falied!"
    }

    info
  }

  def getErrStr : String = {
    err.toString()
  }

  def getOutStr : String = {
    out.toString()
  }


}

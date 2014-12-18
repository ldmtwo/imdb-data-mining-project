/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sparkfileutils
import collection.JavaConversions._
import org.apache.spark.storage.StorageLevel
import scala.collection.JavaConversions._
import filereorganizer.FileLineIndexer
import filereorganizer.JTest
import filereorganizer.Movie
import java.io.File
import java.util.ArrayList
import java.util.HashSet
import java.util.LinkedList
import org.apache.spark._
import scala.collection.mutable.ListBuffer

object Main {
  def main(arg: Array[String]) {
    var m = new Movie("", "")
    var NPARTS = arg(1).toInt
    val DIR = arg(0)
    val conf = new SparkConf().setAppName("AppNameHere")

    val sc: SparkContext = new SparkContext(conf)
    //for(f <- new File("lib").listFiles){
    //  sc.addJar(f.getAbsolutePath)
    //  System.load(f.getAbsolutePath)
    //}

    val rdd = sc.parallelize(new File(DIR).listFiles)
    var rdd2 = rdd
    for( i <- Range(0, 3, 1)){
    rdd2 = rdd2.map { f =>
      if (f.isDirectory) f.listFiles.toSeq
      else {
        Seq(f)
      }
    }.flatMap { identity }
  }
  rdd2 = rdd2.repartition(NPARTS).persist(StorageLevel.DISK_ONLY)

  var rdd3 = rdd2.map{ f =>
    if (f.isDirectory) {

      var list = new ArrayList[File]
      var q = new LinkedList[Array[File]]
      var i = 0
      val DIR = arg(0)
      (new File(DIR).listFiles).map { f =>
        list.add(f)
        if (f.isDirectory) q.add(f.listFiles)
      }
      while (q.nonEmpty) {
        try {
          val listOfArrays = (q.removeFirst).map { f =>
            list.add(f)
            if (f.isDirectory) q.add(f.listFiles)
          }
        } catch {
          case e: Exception => e.printStackTrace
        }
      }
      list.toSeq
    } else {
      Seq(f)
    }
  }.persist(StorageLevel.DISK_ONLY) //.flatMap { identity }.filter(f => f.isDirectory)

  var rdd4 = rdd3.flatMap { identity }

  //    var set = new HashSet[File]
  //    var files = sc.makeRDD(set.toSeq)
  //    var q = new LinkedList[Array[File]]
  //    var i = 0
  //    val DIR = arg(0)
  //    (new File(DIR).listFiles).map { f =>
  //      set.add(f)
  //      if (f.isDirectory) q.add(f.listFiles)
  //    }
  //    var time = System.currentTimeMillis
  //    //    var rt = Runtime.getRuntime
  //    while (q.nonEmpty) {
  //      try {
  //        if (System.currentTimeMillis - time > 60000) {
  //          FileLineIndexer.printMemory
  //          JTest.printMemoryInfo(set.size, time)
  //          time = System.currentTimeMillis
  //          files = sc.makeRDD(set.toSeq)
  //          files.saveAsTextFile("file:////mnt/hgfs/vmshare/files.out." + i + DIR.split('/').last)
  //          i = i + 1
  //          set.clear
  //        }
  //        val listOfArrays = (q.removeFirst).par.map { f =>
  //          set.add(f)
  //          if (f.isDirectory) q.add(f.listFiles)
  //        }
  //      } catch {
  //        case e: Exception => e.printStackTrace
  //      }
  //    }
  //
//  println(rdd4.count + " files")
  rdd4.saveAsTextFile("file:////mnt/hgfs/vmshare/files.out." + DIR.split('/').last)

  sc.stop()
}

}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sparkfileutils
import scala.math.random
import collection.JavaConversions._
import scala.collection.JavaConversions._
import filereorganizer.Movie
import java.io.File
import java.util.HashMap
import java.util.LinkedList
import org.apache.spark._
import filereorganizer.FileLineIndexer

object ParseIMDB {

  def main(arg: Array[String]) {

    var m=new Movie("","")
    val conf = new SparkConf().setAppName("AppNameHere")
    val sc: SparkContext = new SparkContext(conf)

    val DIR = arg(0)
    val obj = (new File(DIR).listFiles).toSeq.par.map { f =>
      val name = f.getName

      val index = name match {
        case "language.list"                => new FileLineIndexer().readLanguageFile(f)
        case "miscellaneous-companies.list" => new FileLineIndexer().readCompanyFile(f)
        case "countries.list"               => new FileLineIndexer().readCountryFile(f)
        case "certificates.list"            => new FileLineIndexer().readCertificateFile(f)
        case "genres.list"                  => new FileLineIndexer().readGenreFile(f)
        case "keywords.list"                => new FileLineIndexer().readKeywordFile(f)
        case "locations.list"               => new FileLineIndexer().readCountryFile(f)
        case "movies.list"                  => new FileLineIndexer().readMoviesFile(f)
        case "production-companies.list"    => new FileLineIndexer().readCompanyFile(f)
        case "release-dates.list"           => new FileLineIndexer().readReleaseDateFile(f)
        case "running-times.list"           => new FileLineIndexer().readRunTimeFile(f)
        case "aka-titles.list"              => new FileLineIndexer().readAkaTitleFile(f)
        case "iso-aka-titles.list"          => new FileLineIndexer().readAkaTitleFile(f)
        case "actors.list"                  => new FileLineIndexer().readOtherPersonFile(f)
        case "actresses.list"               => new FileLineIndexer().readOtherPersonFile(f)
        case "cinematographers.list"        => new FileLineIndexer().readOtherPersonFile(f)
        case "miscellaneous.list"           => new FileLineIndexer().readOtherPersonFile(f)
        case "aka-names.list"               => new FileLineIndexer().readOtherPersonFile(f)
        case "composers.list"               => new FileLineIndexer().readOtherPersonFile(f)
        case "directors.list"               => new FileLineIndexer().readOtherPersonFile(f)
        case "producers.list"               => new FileLineIndexer().readOtherPersonFile(f)
        case "production-designers.list"    => new FileLineIndexer().readOtherPersonFile(f)
        case "writers.list"                 => new FileLineIndexer().readOtherPersonFile(f)
        case "ratings.list"                 => new FileLineIndexer().readRatingFile(f)
        case _ =>
          println("skipping unknown file: " + f)
          null
      }
      if(index!=null){
      val t =(name, index)
      
        sc.makeRDD(List(t)).saveAsObjectFile("file://db."+name)

        println(t._2.titleArray.size + " files in " + t._1)
      }
    }

    obj.foreach { t =>
    }

    sc.stop()
  }

}
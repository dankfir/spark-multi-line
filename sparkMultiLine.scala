package com.vitco

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by HongYuChai on 2017/8/18.
  */

object logAnalysis_catalina {
  Logger.getLogger("org").setLevel(Level.ERROR)


  def main(args: Array[String]): Unit = {
    val root = this.getClass.getResource("/")
    val conf = new SparkConf().setAppName("logAnalysis").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    val sourceRdd = sc.textFile(root + "catalina/catalina*.log")
    var log = new ArrayBuffer[String]()//用于临时保存要合并的行

    val preprocessRDD = sourceRdd
      .map(line => {
        var tlog = " "
        if (log.length < 2) {//合并两行
          log += line
          if (log.length == 2) {
            if (log(0).contains(": ")) tlog = log(1) + " " + log(0)
            else tlog = log(0) + " " + log(1)
            log = new ArrayBuffer[String]()
          }
        }
        tlog//合并后的行
      })
      .filter(!_.equals(" "))
    import sqlContext.implicits._
    val logDF = preprocessRDD.toDF()
    logDF.show()

    import com.databricks.spark.csv._
    logDF.repartition(1).saveAsCsvFile(root + "catalina.csv")

  }

}



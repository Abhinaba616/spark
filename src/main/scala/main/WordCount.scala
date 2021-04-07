package main

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.{Encoder, Encoders}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.functions._
import org.apache.spark.sql._
import org.apache.spark.storage.StorageLevel

case class Person(Name: String,
                  Sex: String,
                  Age: String,
                  Height: String,
                  Weight: String)

object WordCount {


  implicit val enc: Encoder[Person] = Encoders.product[Person]



  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Spark").setMaster("local")
    val sc = new SparkContext(conf)
    val spark = SparkSession
      .builder()
      .appName("Spark SQL basic")
      .config("spark.some.config.option", "some-value")
      .getOrCreate()
   import spark.implicits._

    val counts = sc.textFile("/home/xs108-abhdas/Documents/example.txt").flatMap(_.split(" ")).map(s => (s,1)).reduceByKey(_+_)



    println(counts.collect.foreach(println))
    println(counts.take(5).foreach(println))

    val df = counts.toDF("words","count")
    println(df.show())
    println(df.select("words").show())
    df.createOrReplaceTempView("wordcounts")
    val sqldf = spark.sql("Select * From wordcounts where count > 1")
    println(sqldf.show())


    val biostats_df = spark.read.option("header","true").csv("/home/xs108-abhdas/Downloads/biostats.csv").toDF().persist(StorageLevel.MEMORY_AND_DISK)   //persistence
    biostats_df.createOrReplaceTempView("biostats_df")
    val query1 = spark.sql("Select * From biostats_df Where Age >40").show()


    println("Dataframe:" + biostats_df.show())
    println(biostats_df.printSchema())

    val biostats_ds = biostats_df.as[Person]
    biostats_ds.createOrReplaceTempView(("biostats_ds"))
    val query2 = spark.sql("Select * From biostats_ds where sex = 'M'").show()
    // org.apache.spark.sql.catalyst.encoders.OuterScopes.addOuterScope(this)


    // val biostats_ds = spark.read.csv("/home/xs108-abhdas/Downloads/biostats.csv").as[Person]


    import spark.implicits._



    /*val biostats_ds: Dataset[Person]= spark.read
      .format("csv")
      .option("header", "true")
      .option("delimiter", ";")
      .option("inferSchema", "true")
      .load("/home/xs108-abhdas/Downloads/biostats.csv")
      .as[Person] */


    println("Dataset:" + biostats_ds.show())
    spark.close()
    sc.stop()
  }
}

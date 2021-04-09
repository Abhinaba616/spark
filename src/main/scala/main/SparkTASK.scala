package main

import org.apache.spark.{SparkConf, SparkContext}
//import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.{Encoder, Encoders}
//import org.apache.spark.sql.types.StructType
//import org.apache.spark.sql.functions._
//import org.apache.spark.sql._
import org.apache.spark.sql.execution.streaming.FileStreamSource.Timestamp
import org.apache.spark.storage.StorageLevel

case class Person(Name: String,
                  Sex: String,
                  Age: String,
                  Height: String,
                  Weight: String)

case class Employee(_corrupt_record: String,
                    emailAddress: String,
                    firstName: String,
                    lastName: String,
                    phoneNumber: String,
                    userId: Long)

case class User( registration_dttm: Timestamp,
                 id: Integer,
                 first_name: String,
                 last_name: String,
                 email: String,
                 gender: String,
                 ip_address:String,
                 cc: String,
                 country: String,
                 birthdate: String,
                 salary: Double,
                 title: String,
                 comments: String)



 object SparkTASK {


  implicit val enc: Encoder[Person] = Encoders.product[Person]



  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Spark").setMaster("local")
    val sc = new SparkContext(conf)
    sc.setLogLevel("OFF")
    val spark = SparkSession
      .builder()
      .master("local")
      .appName("Spark SQL basic")
      .config(conf)
      .getOrCreate()
    spark.sparkContext.setLogLevel("OFF")
    import spark.implicits._



    // WordCount
     val counts = sc.textFile("/home/xs108-abhdas/Documents/example.txt").flatMap(_.split(" ")).map(s => (s, 1)).reduceByKey(_ + _)
     println(counts.collect.foreach(println))
     println(counts.take(5).foreach(println))
     val df = counts.toDF("words", "count")
     println(df.show())
     println(df.select("words").show())
     df.createOrReplaceTempView("wordcounts")
     spark.sql("Select * From wordcounts where count > 1").show()

    // Loading csv data
     val biostats_df = spark.read.option ("inferSchema", "true").option ("header", "true").csv ("/home/xs108-abhdas/Downloads/biostats.csv").toDF ().persist (StorageLevel.MEMORY_AND_DISK) //persistence
     //using coalesce for partitioning
     biostats_df.createOrReplaceTempView ("biostats_df")
     spark.sql ("SELECT * FROM biostats_df WHERE Age BETWEEN 35 AND 45 ").show () //sql query over csv file to get all records having age >35
     println ("Dataframe:" + biostats_df.show () )
     biostats_df.printSchema ()

     val biostats_ds = biostats_df.as[Person]
     biostats_ds.createOrReplaceTempView ("biostats_ds")
     spark.sql ("Select * From biostats_ds where sex = 'M'").show () //sql query over csv file to get all records having sex = male
     println ("Dataset:" + biostats_ds.show () )

   // loading json data
    val jsonSampledf = spark.read.option("inferSchema","true").option("multiLines","true").json("/home/xs108-abhdas/Downloads/sample.json").toDF().persist(StorageLevel.MEMORY_AND_DISK)
    jsonSampledf.printSchema()
    jsonSampledf.createOrReplaceTempView("employee")
    jsonSampledf.collect()
    jsonSampledf.show()
    spark.sql("SELECT COUNT(userId) as total_records FROM employee").show()
    spark.sql("SELECT userId FROM employee WHERE firstName = 'racks' ").show()  //sql query over json file to search for a first name

   val jsonSample_ds = jsonSampledf.as[Employee]
   jsonSample_ds.createOrReplaceTempView("jsonsample_DS")
   spark.sql("SELECT firstName,emailAddress FROM jsonsample_DS where userID ='4' ").show()
   println("jsonSampleDS" + jsonSample_ds.show())

   // loading parquet data
   import spark.implicits._
   val users_df = spark.read.option("multiLines","true").option("inferSchema","true").parquet("/home/xs108-abhdas/Downloads/userdata5.parquet").toDF().persist(StorageLevel.MEMORY_AND_DISK)
   val rdd1 =users_df.repartition(2)
   println(rdd1.rdd.partitions.length)
   val excluColumns= "comments"
   users_df.drop(excluColumns).show()
   users_df.printSchema()
   //Employeedf.write.parquet("/home/xs108-abhdas/Downloads/Employee.parquet")
   //val parquetFileDF = spark.read.parquet("/home/xs108-abhdas/Downloads/users.parquet")
   users_df.createOrReplaceTempView("parquetFile")
   spark.sql("SELECT id,first_name,last_name FROM parquetFile WHERE country = 'India' ").show()

   val users_ds = users_df.as[User]
   users_ds.createOrReplaceTempView("users_DS")
   spark.sql("SELECT first_name,last_name,email,gender,country FROM users_DS where gender = 'Male' AND country ='United States' ").show()

    spark.close()
    sc.stop()
  }
}


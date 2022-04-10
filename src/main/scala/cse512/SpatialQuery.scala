package cse512

import org.apache.spark.sql.SparkSession

object SpatialQuery extends App {

  def ST_Contains(pointString:String, queryRectangle:String) : Boolean = {
    val point = pointString.split(",")
    val point_x = point(0).trim().toDouble
    val point_y = point(1).trim().toDouble

    val rectangle = queryRectangle.split(",")
    val rectangle_x_1 = rectangle(0).trim().toDouble
    val rectangle_y_1 = rectangle(1).trim().toDouble
    val rectangle_x_2 = rectangle(2).trim().toDouble
    val rectangle_y_2 = rectangle(3).trim().toDouble

    var min_x: Double = 0
    var max_x: Double = 0
    if(rectangle_x_1 < rectangle_x_2) {
      min_x = rectangle_x_1
      max_x = rectangle_x_2
    } else {
      min_x = rectangle_x_2
      max_x = rectangle_x_1
    }

    var min_y: Double = 0
    var max_y: Double = 0
    if(rectangle_y_1 < rectangle_y_2) {
      min_y = rectangle_y_1
      max_y = rectangle_y_2
    } else {
      min_y = rectangle_y_2
      max_y = rectangle_y_1
    }

    if(point_x >= min_x && point_x <= max_x && point_y >= min_y && point_y <= max_y) {
      return true
    } else {
      return false
    }
  }
  def ST_Within(pointString1: String, pointString2: String, distance: Double): Boolean = {

    var point1 = new Array[String](2)
    point1 = pointString1.split(",")
    val x1 = point1(0).trim.toDouble
    val y1 = point1(1).trim.toDouble

    var point2 = new Array[String](2)
    point2 = pointString2.split(",")
    val x2 = point2(0).trim.toDouble
    val y2 = point2(1).trim.toDouble

    val d = math.sqrt(math.pow((x1 - x2), 2) + math.pow((y1 - y2), 2))

    if (d <= distance)
      return true
    else
      return false
  }

  def runRangeJoinQuery(spark: SparkSession, arg1: String, arg2: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter", "\t").option("header", "false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    val rectangleDf = spark.read.format("com.databricks.spark.csv").option("delimiter", "\t").option("header", "false").load(arg2);
    rectangleDf.createOrReplaceTempView("rectangle")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Contains", (queryRectangle: String, pointString: String) => ((true)))

    val resultDf = spark.sql("select * from rectangle,point where ST_Contains(rectangle._c0,point._c0)")
    resultDf.show()

    return resultDf.count()
  }

  def runDistanceQuery(spark: SparkSession, arg1: String, arg2: String, arg3: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter", "\t").option("header", "false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Within", (pointString1: String, pointString2: String, distance: Double) => ((true)))

    val resultDf = spark.sql("select * from point where ST_Within(point._c0,'" + arg2 + "'," + arg3 + ")")
    resultDf.show()

    return resultDf.count()
  }

  def runDistanceJoinQuery(spark: SparkSession, arg1: String, arg2: String, arg3: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter", "\t").option("header", "false").load(arg1);
    pointDf.createOrReplaceTempView("point1")

    val pointDf2 = spark.read.format("com.databricks.spark.csv").option("delimiter", "\t").option("header", "false").load(arg2);
    pointDf2.createOrReplaceTempView("point2")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Within", (pointString1: String, pointString2: String, distance: Double) => ((true)))
    val resultDf = spark.sql("select * from point1 p1, point2 p2 where ST_Within(p1._c0, p2._c0, " + arg3 + ")")
    resultDf.show()

    return resultDf.count()
  }
}

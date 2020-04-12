package cse512

import org.apache.spark.sql.SparkSession

object SpatialQuery extends App{
	def runRangeQuery(spark: SparkSession, arg1: String, arg2: String): Long = {

		val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
		pointDf.createOrReplaceTempView("point")

		// YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
		spark.udf.register("ST_Contains",(queryRectangle:String, pointString:String)=>((true)))

		val resultDf = spark.sql("select * from point where ST_Contains('"+arg2+"',point._c0)")
		resultDf.show()

		return resultDf.count()
	}

	def runRangeJoinQuery(spark: SparkSession, arg1: String, arg2: String): Long = {

		val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
		pointDf.createOrReplaceTempView("point")

		val rectangleDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg2);
		rectangleDf.createOrReplaceTempView("rectangle")

		// YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
		spark.udf.register("ST_Contains",(queryRectangle:String, pointString:String)=>((true)))

		val resultDf = spark.sql("select * from rectangle,point where ST_Contains(rectangle._c0,point._c0)")
		resultDf.show()

		return resultDf.count()
	}

	def pointsWithinDistanceD = (pointString1:String, pointString2:String, distance:Double) => {
		val pointsA = pointString1.split(",")

		val x_coord_a = pointsA(0).toDouble
		val y_coord_a = pointsA(1).toDouble

		val pointsB = pointString2.split(",")

		val x_coord_b = pointsB(0).toDouble
		val y_coord_b = pointsB(1).toDouble

		val distanceBetweenPoints = Math.sqrt(Math.pow(x_coord_b - x_coord_a, 2) + Math.pow(y_coord_b - y_coord_a, 2) )
		distanceBetweenPoints
	}

	def arePointsWithinDistanceD = (pointString1:String, pointString2:String, distance:Double) => {
		val pointsA = pointString1.split(",")

		val x_coord_a = pointsA(0).toDouble
		val y_coord_a = pointsA(1).toDouble

		val pointsB = pointString2.split(",")

		val x_coord_b = pointsB(0).toDouble
		val y_coord_b = pointsB(1).toDouble

		val distanceBetweenPoints = Math.sqrt(Math.pow(x_coord_b - x_coord_a, 2) + Math.pow(y_coord_b - y_coord_a, 2) )
		if (distanceBetweenPoints <= distance) true else false
	}

	def runDistanceQuery(spark: SparkSession, arg1: String, arg2: String, arg3: String): Long = {

		val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
		pointDf.createOrReplaceTempView("point")

		// YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
		spark.udf.register("ST_Within",(pointString1:String, pointString2:String, distance:Double)=>(arePointsWithinDistanceD(pointString1, pointString2, distance)))
		val resultDf = spark.sql("select * from point where ST_Within(point._c0,'"+arg2+"',"+arg3+")")
		resultDf.withColumn("distance", pointsWithinDistanceD(col("_c0"), arg2, arg3.toDouble))
		resultDf.show()

		return resultDf.count()
	}


	def runDistanceJoinQuery(spark: SparkSession, arg1: String, arg2: String, arg3: String): Long = {

		val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
		pointDf.createOrReplaceTempView("point1")

		val pointDf2 = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg2);
		pointDf2.createOrReplaceTempView("point2")

		// YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
		spark.udf.register("ST_Within",(pointString1:String, pointString2:String, distance:Double)=>( arePointsWithinDistanceD(pointString1, pointString2, distance)))
		val resultDf = spark.sql("select * from point1 p1, point2 p2 where ST_Within(p1._c0, p2._c0, "+arg3+")")
		resultDf.show()

		return resultDf.count()
	}
}

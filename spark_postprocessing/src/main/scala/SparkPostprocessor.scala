import org.apache.spark.sql.functions._

object SparkPostprocessor extends App with SparkSessionWrapper {

  val StreamedDf = spark.read.parquet("D:/results")
  StreamedDf
    .coalesce(1)
    .dropDuplicates()
    .where(col("_id").isNotNull)
    .write
    .mode("overwrite")
    .parquet("D:/results/postprocess")
}

import org.apache.spark.sql.functions._
import ProcessInfo._
import org.apache.spark.sql.types.{
  ArrayType,
  DoubleType,
  IntegerType,
  StringType,
  StructType
}
object VacanciesProcessing extends App with SparkSessionWrapper {

  val schema = new StructType()
    .add("_id", StringType, nullable = false)
    .add("title", StringType, nullable = false)
    .add("vacancy_url", StringType, nullable = false)
    .add("experience", StringType, nullable = true)
    .add("work_schedule", StringType, nullable = false)
    .add("work_schedule_add", StringType, nullable = false)
    .add("salary", ArrayType(StringType), nullable = true)
    .add("salary_type", StringType, nullable = true)
    .add("employer", ArrayType(StringType), nullable = false)
    .add("employer_address", ArrayType(StringType), nullable = false)
    .add("description", ArrayType(StringType), nullable = false)
    .add("strong_fields", ArrayType(StringType), nullable = true)
    .add("key_skills", ArrayType(StringType), nullable = true)
    .add("employer_page", StringType, nullable = false)
    .add("employer_rating", StringType, nullable = true)
    .add("emp_feedback_number", StringType, nullable = true)
    .add("work_var_contract", StringType, nullable = true)
    .add(
      "work_var_parttime",
      ArrayType(StringType, containsNull = true),
      nullable = true
    )
    .add("vacancy_date", StringType, nullable = false)

  val includeVacancies = spark.read
    .format("csv")
    .option("header", value = true)
    .load("src/main/source/include.csv")

  val trashVacancies = spark.read
    .format("csv")
    .option("header", value = true)
    .load("src/main/source/exclude.csv")

  import spark.implicits._

  val trashVacanciesSeq =
    trashVacancies.select("title").as[String].collect().toSeq

  val includeVacanciesRlike =
    includeVacancies.select("title").as[String].collect().toSeq.mkString("|")

  val vacanciesHH = spark.read
    .schema(schema)
    .format("json")
    .option("multiline", value = true)
    .load("src/main/source/it_rus_main.hh_it.json")
    .withColumn("_id", col("_id").cast(IntegerType))
    .withColumn(
      "emp_feedback_number",
      col("emp_feedback_number").cast(IntegerType)
    )
    .withColumn(
      "title",
      regexp_replace(
        lower(col("title")),
        "[\\p{Punct}&&[^.$]&&[^#$]&&[^+$]]",
        " "
      )
    )
    .withColumn("title", regexp_replace(lower(col("title")), "\\s+", " "))
    .filter(col("title").rlike(includeVacanciesRlike))
    .filter(!col("title").isin(trashVacanciesSeq: _*))
    .withColumn("employer", concat_ws("", col("employer")))
    .withColumn(
      "city_from_url",
      cityMap(
        regexp_replace(
          regexp_extract(col("vacancy_url"), "//[a-z]+", 0),
          "//",
          ""
        )
      )
    )
    .withColumn("employer_city", col("employer_address")(0))
    .withColumn(
      "description",
      regexp_replace(concat_ws(" ", col("description")), "  ", "\n")
    )
    .withColumn(
      "employer_rating",
      regexp_replace(col("employer_rating"), ",", ".").cast(DoubleType)
    )
    .withColumn(
      "emp_feedback_number",
      col("emp_feedback_number").cast(IntegerType)
    )
    .withColumn("currency", currencyMap(col("salary")(size(col("salary")) - 1)))
    .withColumn("salary_type", salaryMap(col("salary_type")))
    .withColumn("work_var_parttime", concat_ws("", col("work_var_parttime")))
    .withColumn("salary", concat_ws("", col("salary")))
    .withColumn(
      "low_level_salary_NET",
      when(
        col("salary").contains("от"),
        when(
          col("salary_type") === "GROSS",
          regexp_extract(
            regexp_replace(col("salary"), "\\s|\\xa0", ""),
            "\\d+",
            0
          )
            * 0.87
        )
          .otherwise(
            regexp_extract(
              regexp_replace(col("salary"), "\\s|\\xa0", ""),
              "\\d+",
              0
            )
          )
      )
    )
    .withColumn(
      "low_level_salary_NET",
      when(col("currency") === "USD", col("low_level_salary_NET") * USD)
        .when(col("currency") === "EUR", col("low_level_salary_NET") * EUR)
        .otherwise(col("low_level_salary_NET"))
    )
    .withColumn(
      "low_level_salary_NET",
      col("low_level_salary_NET").cast(IntegerType)
    )
    .withColumn(
      "high_level_salary_NET",
      when(
        col("salary").contains("до"),
        when(
          col("salary_type") === "GROSS",
          regexp_replace(
            regexp_extract(
              regexp_replace(col("salary"), "\\s|\\xa0", ""),
              "до\\d+",
              0
            ),
            "до",
            ""
          )
            * 0.87
        )
          .otherwise(
            regexp_replace(
              regexp_extract(
                regexp_replace(col("salary"), "\\s|\\xa0", ""),
                "до\\d+",
                0
              ),
              "до",
              ""
            )
          )
      )
    )
    .withColumn(
      "high_level_salary_NET",
      when(col("currency") === "USD", col("high_level_salary_NET") * USD)
        .when(col("currency") === "EUR", col("high_level_salary_NET") * EUR)
        .otherwise(col("high_level_salary_NET"))
    )
    .withColumn(
      "high_level_salary_NET",
      col("high_level_salary_NET").cast(IntegerType)
    )
    .withColumn(
      "vacancy_date",
      regexp_replace(col("vacancy_date"), "\\xa0", " ")
    )
    .withColumn("temp_day", regexp_extract(col("vacancy_date"), "\\d+", 0))
    .withColumn(
      "temp_month",
      monthsMap(regexp_extract(col("vacancy_date"), "[а-я]+", 0))
    )
    .withColumn("temp_year", regexp_extract(col("vacancy_date"), "\\d{4}", 0))
    .withColumn(
      "vacancy_date",
      concat(
        col("temp_year"),
        lit("-"),
        col("temp_month"),
        lit("-"),
        col("temp_day")
      )
    )
    .withColumn(
      "month_for_partition",
      concat(col("temp_year"), col("temp_month")).cast(IntegerType)
    )
    .drop("temp_day", "temp_month", "temp_year", "employer_address")

//  =================================VACANCY GROUPS=====================================
    .withColumn(
      "ds",
      when(
        col("title").rlike(groupsMap("Data science")),
        lit("Data science")
      )
    )
    .withColumn(
      "support",
      when(
        col("title").rlike(groupsMap("Cпециалист техподдержки")),
        lit("Cпециалист техподдержки")
      )
    )
    .withColumn(
      "qa",
      when(col("title").rlike(groupsMap("QA специалист")), lit("QA специалист"))
    )
    .withColumn(
      "devops",
      when(
        col("title").rlike(groupsMap("Devops специалист")),
        lit("Devops специалист")
      )
    )
    .withColumn(
      "_1c",
      when(col("title").rlike(groupsMap("1C специалист")), lit("1C специалист"))
    )
    .withColumn(
      "sad",
      when(
        col("title").rlike(groupsMap("Системный администратор")),
        lit("Системный администратор")
      )
    )
    .withColumn(
      "sb",
      when(
        col("title").rlike(groupsMap("Специалист по IT безопасности")),
        lit("Специалист по IT безопасности")
      )
    )
    .withColumn(
      "ba",
      when(
        col("title").rlike(groupsMap("Бизнес аналитик")),
        lit("Бизнес аналитик")
      )
    )
    .withColumn(
      "sa",
      when(
        col("title").rlike(groupsMap("Системный аналитик")),
        lit("Системный аналитик")
      )
    )
    .withColumn(
      "a",
      when(
        col("title").rlike(groupsMap("Аналитик")),
        lit("Аналитик")
      )
    )
    .withColumn(
      "dev",
      when(
        col("title").rlike(groupsMap("Программист")),
        lit("Программист")
      )
    )
    .withColumn(
      "de",
      when(
        col("title").rlike(groupsMap("Data Engineer")),
        lit("Data Engineer")
      )
    )
    .withColumn(
      "bd",
      when(
        col("title").rlike(groupsMap("Специлист по БД")),
        lit("Специлист по БД")
      )
    )
    .withColumn(
      "head",
      when(
        col("title").rlike(groupsMap("IT руководитель")),
        lit("IT руководитель")
      )
    )
    .withColumn(
      "vacancy_group",
      array_compact(array(group_columns.map((c: String) => col(c)): _*))
    )
    .drop(group_columns: _*)

  vacanciesHH
    .coalesce(1)
    .write
    .format("parquet")
    .mode("append")
    .parquet("D:/results")

}

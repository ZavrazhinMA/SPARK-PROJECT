import org.apache.spark.sql.Column
import org.apache.spark.sql.functions.typedLit

object ProcessInfo {

  val monthsMap: Column = typedLit(Map(
    "января" -> "1",
    "февраля" -> "2",
    "марта" -> "3",
    "апреля" -> "4",
    "мая" -> "5",
    "июня" -> "6",
    "июля" -> "7",
    "августа" -> "8",
    "сентября" -> "9",
    "октября" -> "10",
    "ноября" -> "11",
    "декабря" -> "12"
  ))

  val currencyMap: Column = typedLit(Map(
    "₽" -> "RUB",
    "$" -> "USD",
    "€" -> "EUR"
  ))

  val salaryMap: Column = typedLit(Map(
    "на руки" -> "NET",
    "до вычета налогов" -> "GROSS"
  ))
  val USD: Double = 91.6
  val EUR: Double = 98.0
}

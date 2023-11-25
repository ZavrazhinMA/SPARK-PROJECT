import org.apache.spark.sql.Column
import org.apache.spark.sql.functions.typedLit

object ProcessInfo {

  val monthsMap: Column = typedLit(
    Map(
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
    )
  )

  val currencyMap: Column = typedLit(
    Map(
      "₽" -> "RUB",
      "$" -> "USD",
      "€" -> "EUR"
    )
  )

  val salaryMap: Column = typedLit(
    Map(
      "на руки" -> "NET",
      "до вычета налогов" -> "GROSS"
    )
  )
  val USD: Double = 88.6
  val EUR: Double = 96.6

  val cityMap: Column = typedLit(
    Map(
      "hh" -> "Москва",
      "spb" -> "Санкт-Перербург",
      "kazan" -> "Казань",
      "ekaterinburg" -> "Екатеринбург",
      "chelyabinsk" -> "Челябинск",
      "krasnodar" -> "Краснодар",
      "nn" -> "Нижний Новгород",
      "novosibirsk" -> "Новосибирск",
      "rostov" -> "Ростов-на-Дону",
      "samara" -> "Самара",
      "voronezh" -> "Воронеж"
    )
  )
  val group_columns: Seq[String] =
    Seq(
      "ds",
      "qa",
      "_1c",
      "support",
      "devops",
      "sad",
      "sb",
      "ba",
      "sa",
      "a",
      "dev",
      "de",
      "bd",
      "head"
    )

  val groupsMap: Map[String, String] = Map(
    "Data science" -> "data sci.*?|\\bml\\b|nlp|\\bcv\\b|recsys|rec sys|research.*?|машин.*? обуч.*?|машин.*? зрен.*?|machin.*? learn.*?|comput.*? vision",
    "Cпециалист техподдержки" -> "поддержк.*?|support|service",
    "QA специалист" -> "\\bqa\\b|тестир|тест|test|aqa|qaa|automation",
    "Devops специалист" -> "devops",
    "1C специалист" -> "1c|1с",
    "Системный администратор" -> "сисадмин|систем.*? адм.*?|адм.*? систем.*?|систем.*? инжен.*?|сетев.*? инжен.*?|\\bsre\\b|sysops|linux",
    "Специалист по IT безопасности" -> "информаци.*? безопас.*?|защит.*? информ.*?|информ.*? защит.*?|безопас.*? информ.*?",
    "Бизнес аналитик" -> "бизнес аналит.*?|business analy.*?|аналит.*? бизнес|бизнес .*? аналит.*?",
    "Системный аналитик" -> ".*?систем.*? аналит.*?|system.*? analy.*?|аналит.*? системный|ситем.*? .*? аналит.*?\"",
    "Аналитик" -> "аналит.*?| analys.*?|\\bbi\\b",
    "Программист" -> "разраб.*?| develop.*?|программист|backend|frontend|fullstack|full stack|python|java|c#|c++|ruby|\\.net|javascript|\\bgo\\b|\\bphp\\b|golang",
    "Data Engineer" -> "data engin.*?|bigdata|big data|spark",
    "Специлист по БД" -> "баз.*? данных.*?|sql|oracle|\\bбд\\b",
    "IT руководитель" -> "руковод.*? ит\\b|руковод.*? it\\b|начальн.*? ит\\b|начальн.*? it\\b|руководитель проектов ит|руководитель проектов it|начальник отдела it|директор по it"
  )
}

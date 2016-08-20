import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import scalaj.http.{Http, HttpResponse}

class Nike(val bearer: String) {
  val base = "https://api.nike.com/sport/v3/me/"

  implicit val valueReads: Reads[Value] = (
    (__ \ "start_epoch_ms").read[Long] and
    (__ \ "end_epoch_ms").read[Long] and
    (__ \ "value").read[Double]
  )(Value)

  implicit val metricReads: Reads[Metric] = (
    (__ \ "type").read[String] and
    (__ \ "values").lazyRead(list[Value])
  )(Metric)

  implicit val activityReads: Reads[Activity] = (
    (__ \ "id").read[String] and
    (__ \ "start_epoch_ms").read[Long] and
    (__ \ "end_epoch_ms").read[Long] and
    (__ \ "metrics").lazyRead(list[Metric])
  )(Activity)

  implicit val pageReads: Reads[Page] = (
    (__ \ "activities").lazyRead(list[Activity]) and
    (__ \ "paging" \ "before_time").readNullable[Long]
  )(Page)

  // Get paged activities before given timestamp
  def activitiesBeforeTime(time: Long): Page = {
    val response = get("activities/before_time/" + time + "?types=run&limit=5")
    val json = Json.parse(response.body)

    Json.fromJson[Page](json)(pageReads).get
  }

  // Get all activities
  def allActivitiesBeforeTime(time: Long): List[Activity] = {
    // Get one page of activities
    val page = activitiesBeforeTime(time)

    // Recursively fetch all earlier activities
    val next = page.before_time.map(beforeTime =>
      allActivitiesBeforeTime(beforeTime)
    )

    page.activities ++ next.getOrElse(Nil)
  }

  // Get activity by id
  def activity(id: String): Activity = {
    val response = get("activity/" + id + "?metrics=ALL")
    val json = Json.parse(response.body)

    Json.fromJson[Activity](json)(activityReads).get
  }

  /**
    * Get request
    *
    * @param path
    * @return
    */
  def get(path: String): HttpResponse[String] =
    Http(base + path)
      .header("Authorization", "Bearer " + bearer)
      .asString
}

// Models

case class Page(activities: List[Activity], before_time: Option[Long])

case class Activity(id: String, start: Long, end: Long, metrics: List[Metric]) {
  // Group metric values by position
  def metricsByPosition: Iterable[((Long, Long), Option[Double], Option[Double], Option[Double])] = {
    val lats = valuesByTime(valuesForType("latitude"))
    val lons = valuesByTime(valuesForType("longitude"))
    val eles = valuesByTime(valuesForType("elevation"))

    // Join latitude, longitude, and elevation on time
    for (time <- lats.keys) yield {
      (time, lats.get(time), lons.get(time), eles.get(time))
    }
  }

  // Convert List to a time-indexed Map
  def valuesByTime(values: List[Value]): Map[(Long, Long), Double] =
    values
      .map {
        case Value(s, e, v) =>
          (s, e) -> v
      }
      .toMap

  // Find metrics by type
  def metricsByType(`type`: String): Option[Metric] =
    metrics.find(_.`type` == `type`)

  // Find values by type
  def valuesForType(`type`: String): List[Value] =
    metricsByType(`type`).map(_.values).getOrElse(Nil)

  // Check if all elements are equal
  def equal[T](x: T*): Boolean =
    (x zip x.tail).foldLeft(true) {
      case (acc, (a, b)) =>
        acc && a == b
    }
}

case class Metric(`type`: String, values: List[Value])

case class Value(start_epoch_ms: Long, end_epoch_ms: Long, value: Double) {
  def time = (start_epoch_ms, end_epoch_ms)
}

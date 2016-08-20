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
  )(Value.apply _)

  implicit val metricReads: Reads[Metric] = (
    (__ \ "type").read[String] and
    (__ \ "values").lazyRead(list[Value])
  )(Metric.apply _)

  implicit val activityReads: Reads[Activity] = (
    (__ \ "id").read[String] and
    (__ \ "app_id").read[String] and
    (__ \ "metrics").lazyRead(list[Metric])
  )(Activity.apply _)

  // Get all activities
  def activities(): List[Activity] = {
    val response = get("activities/before_id/2020000000003054526120012040361214670307?types=jogging,run&limit=5")
    val json = Json.parse(response.body)

    implicit val valueFormat = Json.format[Value]
    implicit val metricFormat = Json.format[Metric]
    implicit val activityFormat = Json.format[Activity]

    Json.fromJson[List[Activity]]((json \ "activities").get).get
  }

  // Get activity by id
  def activity(id: String): Activity = {
    val response = get("activity/" + id + "?metrics=ascent,descent,distance,elevation,heart_rate,latitude,longitude,nikefuel,pace,speed,grade,power,rpe")
    val json = Json.parse(response.body)

    implicit val valueFormat = Json.format[Value]
    implicit val metricFormat = Json.format[Metric]
    implicit val activityFormat = Json.format[Activity]

    Json.fromJson[Activity](json).get
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

case class Activity(id: String, app_id: String, metrics: List[Metric]) {
  // Group metric values by position
  def metricsByPosition: List[(Double, Double, Double, (Long, Long))] = {
    val lats = valuesByType("latitude")
    val lons = valuesByType("longitude")
    val eles = valuesByType("elevation")

    // Join latitude, longitude, and elevation on time
    for (v1 <- lats; v2 <- lons; v3 <- eles if equal(v1.time, v2.time, v3.time)) yield {
      (v1.value, v2.value, v3.value, v1.time)
    }
  }

  // Find metrics by type
  def metricsByType(`type`: String): Option[Metric] =
    metrics.find(_.`type` == `type`)

  // Find values by type
  def valuesByType(`type`: String): List[Value] =
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

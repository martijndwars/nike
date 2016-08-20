import org.joda.time.format.ISODateTimeFormat
import scopt.OptionParser

object Main {
  def main(args: Array[String]) = {
    val parser = new OptionParser[Config]("nike") {
      head("nike", "1.0")

      cmd("list")
        .action((_, c) => c.copy(mode = "list"))
        .text("Show all activities")
        .children(
          opt[Long]("before-time")
            .abbr("bt")
            .action((v, c) => c.copy(beforeTime = Some(v)))
            .text("Only show activities before given epoch timestamp")
        )

      cmd("show")
        .action((_, c) => c.copy(mode = "show"))
        .text("Show single activity")
        .children(
          arg[String]("id")
            .action((v, c) => c.copy(id = v))
            .text("Activity identifier")
        )
    }

    parser.parse(args, Config()) match {
      case Some(config) =>
        config.mode match {
          case "list" =>
            list(config.beforeTime.getOrElse(System.currentTimeMillis))
          case "show" =>
            show(config.id)
          case _ =>
        }
      case None =>
    }
  }

  /**
    * List all activities before given time
    */
  def list(before: Long): Unit = {
    val activities = new Nike("uop7XXkGqwBRcZyRmBmqb0JoCZ1E")
      .allActivitiesBeforeTime(before)

    activities.foreach(activity =>
      println(activity.id + "|" + ISODateTimeFormat.dateTimeNoMillis().print(activity.end))
    )
  }

  /**
    * Show activity
    */
  def show(id: String): Unit = {
    val activity = new Nike("uop7XXkGqwBRcZyRmBmqb0JoCZ1E").activity(id)

    println(Converter.convert(activity))
  }
}

case class Config(mode: String = "", beforeTime: Option[Long] = None, id: String = "")

import org.joda.time.format.ISODateTimeFormat

object Main {
  def main(args: Array[String]) = {
    args(0) match {
      case "list" =>
        if (args.length >= 2) {
          list(args(1).toLong)
        } else {
          list()
        }
      case "show" =>
        show(args(1))
    }
  }
  
  /**
    * List all activities before given time (or current time)
    */
  def list(before: Long = System.currentTimeMillis): Unit = {
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

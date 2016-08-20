object Main {
  def main(args: Array[String]) = {
    args(0) match {
      case "list" =>
        list()
      case "show" =>
        show(args(1))
    }
  }

  /**
    * List activities
    */
  def list(): Unit = {
    val activities = new Nike("uop7XXkGqwBRcZyRmBmqb0JoCZ1E").activities()

    activities.foreach(activity =>
      println(activity.id + "|" + activity.app_id)
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

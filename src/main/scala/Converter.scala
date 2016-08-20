import org.joda.time.format.ISODateTimeFormat

import scala.xml.Elem

object Converter {
  def convert(activity: Activity): Elem =
    <gpx creator="Nike+ Export">
      <trk>
        <trkseg>
          {activity.metricsByPosition.map { case (time, lat, lon, ele) =>
            <trkpt lat={lat.toString} lon={lon.toString}>
              <time>{ISODateTimeFormat.dateTimeNoMillis().print(time._1)}</time>
              {if (ele.isDefined) <ele>{ele.get}</ele>}
            </trkpt>
          }}
        </trkseg>
      </trk>
    </gpx>
}

import org.joda.time.format.ISODateTimeFormat

import scala.xml.Elem

object Converter {
  def convert(activity: Activity): Elem =
    <gpx creator="Nike+ Export">
      <trk>
        <trkseg>
          {activity.metricsByPosition.map { case (lat, lon, ele, time) =>
            <trkpt lat={lat.toString} lon={lon.toString}>
              <time>
                {ISODateTimeFormat.dateTimeNoMillis().print(time._1)}
              </time>
              <ele>
                {ele}
              </ele>
            </trkpt>
          }}
        </trkseg>
      </trk>
    </gpx>
}

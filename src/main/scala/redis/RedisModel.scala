package redis

import io.circe.Decoder

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object RedisModel {
  val yyyyMMdd: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

  case class BluepipeDate(
    effective_date: LocalDate,
    created_at: Long
  )

  object Codecs {
    implicit val decodeLocalDate: Decoder[LocalDate] =
      Decoder[Int].map(_.toString).map(LocalDate.parse(_, yyyyMMdd))
  }
}

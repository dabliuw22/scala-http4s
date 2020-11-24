package com.leysoft.products.adapter.out.doobie.util

import java.time.{OffsetDateTime, ZoneId}

import doobie.enum.JdbcType._
import doobie.util.{Get, Put}

object TimeUtil {

  private val zoned = ZoneId.systemDefault()

  implicit class OffsetDateTimeSystemZone(dateTime: OffsetDateTime) {

    def withSystemZone: OffsetDateTime =
      dateTime.toInstant.atZone(zoned).toOffsetDateTime
  }

  implicit val offsetDateTimeGet: Get[OffsetDateTime] = Get.Basic
    .one(
      TimestampWithTimezone,
      List.empty,
      _.getObject(_, classOf[OffsetDateTime])
    )
    .map(_.withSystemZone)

  implicit val offsetDateTimePut: Put[OffsetDateTime] = Put.Basic
    .one(TimestampWithTimezone, _.setObject(_, _), _.updateObject(_, _))
}

package com.twitter.algebird_internal.tests

import com.twitter.algebird_internal.thriftscala._

import com.twitter.algebird.{Monoid, SketchMap, SketchMapMonoid, SketchMapParams, HyperLogLog}
import com.twitter.algebird_internal.bijection.{SketchMapBijection, ThriftImplicits}
import com.twitter.bijection.{Bijection, Conversion}

import org.specs._
import scala.util.Random

import Conversion.asMethod

class TsmSketchMapSpec extends SpecificationWithJUnit {
  import ThriftImplicits._
  import HyperLogLog.long2Bytes

  implicit val monoid = SketchMap.monoid[Long, Long](
    SketchMapParams(seed = 1, eps = 0.01, delta = 0.01, heavyHittersCount = 10)
  )
  implicit val bij: Bijection[SketchMap[Long, Long], TsmSketchMap] = new SketchMapBijection(monoid)

  val random = new Random()

  "Thrift SketchMap" should {
    "convert from algebird CMS and back" in {
      "zero" in {
        val sketchMap = monoid.zero
        val converted = sketchMap.as[TsmSketchMap].as[SketchMap[Long, Long]]

        converted.heavyHitterKeys mustEqual (sketchMap.heavyHitterKeys)
        converted.totalValue mustEqual (sketchMap.totalValue)
      }
    }
  }
}

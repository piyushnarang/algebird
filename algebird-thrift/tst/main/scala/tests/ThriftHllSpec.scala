package com.twitter.algebird_internal.tests

import com.twitter.algebird_internal.thriftscala._

import com.twitter.algebird.{Monoid, HyperLogLog, HLL, HyperLogLogMonoid}
import com.twitter.algebird_internal.bijection.{ThriftImplicits, HyperLogLogBijection}
import com.twitter.bijection.{Bijection, Conversion}

import Conversion.asMethod

import org.specs._
import scala.util.Random

class ThriftHllSpec extends SpecificationWithJUnit {
  import ThriftImplicits._

  implicit def string2Bytes(string: String) = string.as[Array[Byte]]

  implicit val monoid: HyperLogLogMonoid = new HyperLogLogMonoid(12)
  implicit val bijection: Bijection[HLL, ThriftHll] = new HyperLogLogBijection

  "ThriftHll" should {
    "serialize" in {
      "zero" in {
        val zero = monoid.zero
        zero.as[ThriftHll].as[HLL] mustEqual zero
      }

      "one" in {
        val one = monoid.create("twitter.com")
        one.as[ThriftHll].as[HLL] mustEqual one
      }

      "two" in {
        val one = monoid.create("twitter.com")
        val two = monoid.create("vine.com")
        val result = Monoid.plus(one, two)

        result.as[ThriftHll].as[HLL] mustEqual result
      }
    }
  }
}

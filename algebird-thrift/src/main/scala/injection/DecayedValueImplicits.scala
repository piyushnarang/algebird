package com.twitter.algebird_internal.injection

import com.twitter.bijection.{Injection, Bijection}
import com.twitter.algebird._
import com.twitter.algebird_internal.thriftscala.{DecayedValue => ThriftDV}

case class DecayedValueConfig(eps: Double)
object DecayedValueConfig {
  implicit val default = DecayedValueConfig(0.001)
}

trait DecayedValueImplicits {
  import ThriftImplicits._
  implicit def getDecayedValueMonoid(implicit config: DecayedValueConfig): DecayedValueMonoid =
    new DecayedValueMonoid(config.eps)

  implicit def toThrift: Bijection[DecayedValue, ThriftDV] = {
    Bijection.build[DecayedValue, ThriftDV] { d =>
      ThriftDV(d.value, d.scaledTime)
    } { t =>
      DecayedValue(t.value, t.scaledTime)
    }
  }
  implicit def toBytes: Injection[DecayedValue, Array[Byte]] = {
    Injection.connect[DecayedValue, ThriftDV, Array[Byte]]
  }
}
object DecayedValueImplicits extends DecayedValueImplicits

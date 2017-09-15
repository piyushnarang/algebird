package com.twitter.algebird_internal.injection

import com.twitter.algebird.{HyperLogLog, HLL, HyperLogLogMonoid}
import com.twitter.algebird_internal.thriftscala.{Hll => ThriftHll}
import com.twitter.bijection.{Bijection, Conversion, Injection}

import Conversion.asMethod

import java.nio.ByteBuffer

/**
 * Thrift HLL <--> Algebird HLL
 */
object HyperLogLogBijection extends Bijection[HLL, ThriftHll] {
  override def apply(hll: HLL) = ThriftHll(HyperLogLog.toBytes(hll).as[ByteBuffer])
  override def invert(thrift: ThriftHll) = HyperLogLog.fromByteBuffer(thrift.bytes.asReadOnlyBuffer)
}

/**
 * Use this class to tune the implicit monoid available on
 * HyperLogLog.
 */
case class HyperLogLogBits(val bits: Int) extends java.io.Serializable {
  require(bits >= 1 && bits <= 24, "Must use between 1 and 24 bits for a HyperLogLogMonoid")
}

/**
 * Mix this trait into your class to get an implicit monoid and
 * serialization injection for HyperLogLog.
 */
trait HyperLogLogImplicits {
  import ThriftImplicits._

  /**
   * Approximate error: 1.04 / sqrt(2^bits)
   *                  ~= 1%
   *
   * Space consumption: 2^bits = 4kb
   */
  implicit def hllMonoid$(implicit hllBits: HyperLogLogBits): HyperLogLogMonoid =
    new HyperLogLogMonoid(hllBits.bits)

  implicit val hllToThrift$ : Bijection[HLL, ThriftHll] = HyperLogLogBijection
  implicit val hllBijection$ : Injection[HLL, Array[Byte]] =
    Injection.connect[HLL, ThriftHll, Array[Byte]]
}

object HyperLogLogImplicits extends HyperLogLogImplicits

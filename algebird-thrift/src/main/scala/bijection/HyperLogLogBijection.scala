package com.twitter.algebird_internal.bijection

import com.twitter.algebird.{HyperLogLog, HLL}
import com.twitter.algebird_internal.thriftscala._
import com.twitter.bijection.{Bijection, Conversion}

import Conversion.asMethod

import java.nio.ByteBuffer

/**
 * Thrift HLL <--> Algebird HLL
 */
class HyperLogLogBijection extends Bijection[HLL, ThriftHll] {
  override def apply(hll: HLL): ThriftHll = ThriftHll(HyperLogLog.toBytes(hll).as[ByteBuffer])
  override def invert(thrift: ThriftHll): HLL =
    HyperLogLog.fromByteBuffer(thrift.bytes.asReadOnlyBuffer)
}

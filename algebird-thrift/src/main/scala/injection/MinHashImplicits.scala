package com.twitter.algebird_internal.injection

import com.twitter.bijection.{Injection, Conversion}
import com.twitter.algebird._
import com.twitter.algebird_internal.thriftscala.{MinHashSig => ThriftMinHash}
import Conversion.asMethod
import java.nio.ByteBuffer
import scala.util.Try

/**
 * Thrift MinHash <--->Algebird MinHashSignature
 */
class MinHashInjection(implicit hasher: MinHasher[AnyRef])
    extends Injection[MinHashSignature, ThriftMinHash] {
  override def apply(sig: MinHashSignature) = {
    ThriftMinHash(sig.bytes.as[ByteBuffer], hasher.numHashes, hasher.numBands, hasher.hashSize)
  }

  override def invert(thrift: ThriftMinHash) = Try {
    checkConsistentWithMinHasher(thrift)
    MinHashSignature(thrift.bytes.as[Array[Byte]])
  }

  private def checkConsistentWithMinHasher(thrift: ThriftMinHash) {
    if (thrift.numOfHashes != hasher.numHashes || thrift.numOfBands != hasher.numBands || thrift.hashSize != hasher.hashSize)
      throw new RuntimeException(
        "Inconsistent parameter in hasher parameters: In monoid(numHashes=" + hasher.numHashes + ", bands=" + hasher.numBands
          + "), In Thrift(numHashes=" + thrift.numOfHashes + ", bands=" + thrift.numOfBands
      )
  }
}

case class MinHasherConfig(targetThreshold: Double, maxBytes: Int, hashSizeInBits: Int)

trait MinHashImplicits {
  import ThriftImplicits._

  implicit def getMinHasherMonoid(implicit config: MinHasherConfig): MinHasher[AnyRef] = {
    config.hashSizeInBits match {

      /**
       * Here using casting is because the hierarchy of current implementation of MinHasher
       * MinHasher[T] is thhe super class of MinHaser16 and MinHasher32
       * MinHasher16 extends MinHasher[Char], MinHasher32 extends MinHasher[Int]
       * The Type parameter has nothing to do with the type of elements that can be hashed
       * They are just used to compare signature. Eg. MinHasher16 has a hashsize of 16bits,therefore each hash in the
       * signature is converted to 2bytes char to determine the min hash
       * for comparing signature
       **/
      case 16 =>
        new MinHasher16(config.targetThreshold, config.maxBytes).asInstanceOf[MinHasher[AnyRef]]
      case 32 =>
        new MinHasher32(config.targetThreshold, config.maxBytes).asInstanceOf[MinHasher[AnyRef]]
      case _ => throw new RuntimeException
    }
  }

  implicit def minHasherToBytes(
    implicit monoid: MinHasher[AnyRef]
  ): Injection[MinHashSignature, Array[Byte]] = {
    new MinHashInjection()(monoid).andThen {
      Injection.connect[ThriftMinHash, Array[Byte]]
    }
  }

}

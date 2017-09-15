package com.twitter.algebird_internal.bijection

import com.twitter.algebird_internal.thriftscala._

import com.twitter.algebird.{SketchMap, AdaptiveVector}
import com.twitter.algebird.matrix.AdaptiveMatrix

import com.twitter.bijection.{AbstractInjection, Conversion, Injection}
import com.twitter.bijection.scrooge.CompactScalaCodec

import java.nio.ByteBuffer

import Conversion.asMethod
import scala.util.{Success, Failure}

/**
 * Thrift Object type Bijections.
 */
object ThriftImplicits {

  /**
   * Type bijections.
   */
  implicit val longThriftInjection: Injection[Long, TsmObject] =
    new AbstractInjection[Long, TsmObject] {
      override def apply(value: Long): TsmObject = TsmObject.LongValue(value)
      override def invert(value: TsmObject) = value match {
        case TsmObject.LongValue(long) => Success(long)
        case _ => Failure(new Exception("Not a long value: " + value.toString))
      }
    }

  implicit val stringThriftBijection: Injection[String, TsmObject] =
    new AbstractInjection[String, TsmObject] {
      override def apply(value: String): TsmObject = TsmObject.StringValue(value)
      override def invert(value: TsmObject) = value match {
        case TsmObject.StringValue(string) => Success(string)
        case _ => Failure(new Exception("Not a string value: " + value.toString))
      }
    }

  implicit val bytesThriftBijection: Injection[Array[Byte], TsmObject] =
    new AbstractInjection[Array[Byte], TsmObject] {
      override def apply(value: Array[Byte]): TsmObject = TsmObject.BytesValue(value.as[ByteBuffer])
      override def invert(value: TsmObject) = value match {
        case TsmObject.BytesValue(bytes) => Success(bytes.as[Array[Byte]])
        case _ => Failure(new Exception("Not a bytes value: " + value.toString))
      }
    }

  /**
   * Thrift Codecs
   */
  implicit val tsmObjectCodec: Injection[TsmObject, Array[Byte]] = CompactScalaCodec(TsmObject)

  implicit val adaptiveMatrixCodec: Injection[TsmAdaptiveMatrix, Array[Byte]] = CompactScalaCodec(
    TsmAdaptiveMatrix
  )
  implicit val adaptiveVectorCodec: Injection[TsmAdaptiveVector, Array[Byte]] = CompactScalaCodec(
    TsmAdaptiveVector
  )

  implicit val hllCodec: Injection[ThriftHll, Array[Byte]] = CompactScalaCodec(ThriftHll)

  implicit val sketchMapCodec: Injection[TsmSketchMap, Array[Byte]] = CompactScalaCodec(
    TsmSketchMap
  )
  implicit val sketchMapHeavyHitterCodec: Injection[TsmHeavyHitter, Array[Byte]] =
    CompactScalaCodec(TsmHeavyHitter)
}

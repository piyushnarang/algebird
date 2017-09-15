package com.twitter.algebird_internal.injection

import com.twitter.algebird_internal.{thriftscala => t}
import t.Item

import com.twitter.bijection.{Conversion, Injection}
import com.twitter.bijection.scrooge.CompactScalaCodec

import java.nio.ByteBuffer

import Conversion.asMethod
import scala.util.{Success, Failure, Try}

/**
 * Implicit injections from Algebird's thrift types to bytes.
 */
object ThriftImplicits extends ThriftImplicits

trait ThriftImplicits {
  private def fail[T](typeString: String, t: Any): Try[T] =
    Failure {
      new Exception("Not a %s value: %s".format(typeString, t.toString))
    }

  implicit val unit2Item: Injection[Unit, Item] =
    Injection.build[Unit, Item](_ => Item.EmptyValue(t.Empty())) {
      case Item.EmptyValue(_) => Success(())
      case v => fail("unit", v)
    }

  implicit val short2Item: Injection[Short, Item] =
    Injection.build[Short, Item](Item.ShortValue(_)) {
      case Item.ShortValue(v) => Success(v)
      case v => fail("short", v)
    }

  implicit val int2Item: Injection[Int, Item] =
    Injection.build[Int, Item](Item.IntValue(_)) {
      case Item.IntValue(v) => Success(v)
      case v => fail("int", v)
    }

  implicit val long2Item: Injection[Long, Item] =
    Injection.build[Long, Item](Item.LongValue(_)) {
      case t.Item.LongValue(v) => Success(v)
      case v => fail("long", v)
    }

  implicit val double2Item: Injection[Double, Item] =
    Injection.build[Double, Item](Item.DoubleValue(_)) {
      case t.Item.DoubleValue(v) => Success(v)
      case v => fail("double", v)
    }

  implicit val string2Item: Injection[String, Item] =
    Injection.build[String, Item](Item.StringValue(_)) {
      case t.Item.StringValue(v) => Success(v)
      case v => fail("string", v)
    }

  implicit val bytes2Item: Injection[Array[Byte], Item] =
    Injection.build[Array[Byte], Item] { bytes =>
      Item.BytesValue(bytes.as[ByteBuffer])
    } {
      case t.Item.BytesValue(v) => Success(v.as[Array[Byte]])
      case v => fail("bytes", v)
    }

  /**
   * Thrift Codecs
   */
  implicit val itemCodec: Injection[Item, Array[Byte]] =
    CompactScalaCodec(Item)

  implicit val adaptiveMatrixCodec: Injection[t.AdaptiveMatrix, Array[Byte]] =
    CompactScalaCodec(t.AdaptiveMatrix)
  implicit val adaptiveVectorCodec: Injection[t.AdaptiveVector, Array[Byte]] =
    CompactScalaCodec(t.AdaptiveVector)
  implicit val hllCodec: Injection[t.Hll, Array[Byte]] =
    CompactScalaCodec(t.Hll)
  implicit val sketchMapCodec: Injection[t.SketchMap, Array[Byte]] =
    CompactScalaCodec(t.SketchMap)
  implicit val heavyHitterCodec: Injection[t.HeavyHitter, Array[Byte]] =
    CompactScalaCodec(t.HeavyHitter)
  implicit val bloomFilterCodec: Injection[t.BloomFilter, Array[Byte]] =
    CompactScalaCodec(t.BloomFilter)
  implicit val minHashCodec: Injection[t.MinHashSig, Array[Byte]] =
    CompactScalaCodec(t.MinHashSig)
  implicit val decayedValueCodec: Injection[t.DecayedValue, Array[Byte]] =
    CompactScalaCodec(t.DecayedValue)
  implicit val averagedValueCodec: Injection[t.AveragedValue, Array[Byte]] =
    CompactScalaCodec(t.AveragedValue)
  implicit val qtreeCodec: Injection[t.QTree, Array[Byte]] =
    CompactScalaCodec(t.QTree)

  implicit val tuple2Codec: Injection[t.Tuple2, Array[Byte]] = CompactScalaCodec(t.Tuple2)
  implicit val tuple3Codec: Injection[t.Tuple3, Array[Byte]] = CompactScalaCodec(t.Tuple3)
  implicit val tuple4Codec: Injection[t.Tuple4, Array[Byte]] = CompactScalaCodec(t.Tuple4)
  implicit val tuple5Codec: Injection[t.Tuple5, Array[Byte]] = CompactScalaCodec(t.Tuple5)
  implicit val tuple6Codec: Injection[t.Tuple6, Array[Byte]] = CompactScalaCodec(t.Tuple6)
  implicit val tuple7Codec: Injection[t.Tuple7, Array[Byte]] = CompactScalaCodec(t.Tuple7)
  implicit val tuple8Codec: Injection[t.Tuple8, Array[Byte]] = CompactScalaCodec(t.Tuple8)
  implicit val tuple9Codec: Injection[t.Tuple9, Array[Byte]] = CompactScalaCodec(t.Tuple9)
  implicit val tuple10Codec: Injection[t.Tuple10, Array[Byte]] = CompactScalaCodec(t.Tuple10)
  implicit val tuple11Codec: Injection[t.Tuple11, Array[Byte]] = CompactScalaCodec(t.Tuple11)
  implicit val tuple12Codec: Injection[t.Tuple12, Array[Byte]] = CompactScalaCodec(t.Tuple12)
  implicit val tuple13Codec: Injection[t.Tuple13, Array[Byte]] = CompactScalaCodec(t.Tuple13)
  implicit val tuple14Codec: Injection[t.Tuple14, Array[Byte]] = CompactScalaCodec(t.Tuple14)
  implicit val tuple15Codec: Injection[t.Tuple15, Array[Byte]] = CompactScalaCodec(t.Tuple15)
  implicit val tuple16Codec: Injection[t.Tuple16, Array[Byte]] = CompactScalaCodec(t.Tuple16)
  implicit val tuple17Codec: Injection[t.Tuple17, Array[Byte]] = CompactScalaCodec(t.Tuple17)
  implicit val tuple18Codec: Injection[t.Tuple18, Array[Byte]] = CompactScalaCodec(t.Tuple18)
  implicit val tuple19Codec: Injection[t.Tuple19, Array[Byte]] = CompactScalaCodec(t.Tuple19)
  implicit val tuple20Codec: Injection[t.Tuple20, Array[Byte]] = CompactScalaCodec(t.Tuple20)
  implicit val tuple21Codec: Injection[t.Tuple21, Array[Byte]] = CompactScalaCodec(t.Tuple21)
  implicit val tuple22Codec: Injection[t.Tuple22, Array[Byte]] = CompactScalaCodec(t.Tuple22)
}

package com.twitter.algebird_internal.bijection

import com.twitter.algebird_internal.thriftscala._

import com.twitter.bijection.{Bijection, Conversion, Injection}
import com.twitter.algebird.{AdaptiveVector, SparseVector, DenseVector, Monoid}

import Conversion.asMethod

/**
 * Serialize Algebird AdaptiveVector[T], where T is not a Vector, to Thrift AdaptiveVector.
 *
 * @author Wen-Hao Lue (wlue@twitter.com)
 */
class AdaptiveVectorBijection[T](implicit inj: Injection[T, TsmObject], monoid: Monoid[T])
    extends Bijection[AdaptiveVector[T], TsmAdaptiveVector] {

  private def sparseValue: T = monoid.zero

  override def apply(vector: AdaptiveVector[T]): TsmAdaptiveVector = vector match {
    case SparseVector(map, _, size) =>
      val convertedMap = map.map { case (key, value) => (key -> value.as[TsmObject]) }
      TsmAdaptiveVector(size, Some(convertedMap), None)
    case DenseVector(vector, _, size) =>
      TsmAdaptiveVector(size, None, Some(vector.map(_.as[TsmObject])))
  }

  override def invert(vector: TsmAdaptiveVector): AdaptiveVector[T] = vector match {
    case TsmAdaptiveVector(size, Some(mutableMap), None) =>
      val convertedMap = mutableMap.toMap.map {
        case (key, value) => (key -> value.as[Option[T]].get)
      }
      AdaptiveVector.fromMap[T](convertedMap, sparseValue, size)
    case TsmAdaptiveVector(size, None, Some(vector)) =>
      val convertedVector = vector
        .map { obj =>
          obj.as[Option[T]].get
        }
        .as[Vector[T]]
      AdaptiveVector.fromVector(convertedVector, sparseValue)
    case _ => sys.error("Invalid serialization.")
  }
}

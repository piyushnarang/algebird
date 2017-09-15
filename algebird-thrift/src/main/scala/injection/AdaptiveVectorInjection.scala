package com.twitter.algebird_internal.injection

import com.twitter.algebird_internal.{thriftscala => t}

import com.twitter.bijection.{Conversion, Injection}
import com.twitter.algebird.{AdaptiveVector, SparseVector, DenseVector, Monoid}

import Conversion.asMethod
import scala.util.Try

/**
 * Serialize Algebird AdaptiveVector[T], where T is not a Vector, to Thrift AdaptiveVector.
 *
 * @author Sam Ritchie
 */
class AdaptiveVectorInjection[T](implicit inj: Injection[T, t.Item], monoid: Monoid[T])
    extends Injection[AdaptiveVector[T], t.AdaptiveVector] {
  import t.VectorContents.{Sparse, Dense}

  private def sparseValue: T = monoid.zero

  override def apply(vector: AdaptiveVector[T]) = vector match {
    case SparseVector(map, _, size) =>
      val convertedMap = map.map { case (key, value) => (key -> value.as[t.Item]) }
      t.AdaptiveVector(size, Sparse(convertedMap))
    case DenseVector(vector, _, size) =>
      t.AdaptiveVector(size, Dense(vector.map(_.as[t.Item])))
  }

  override def invert(vector: t.AdaptiveVector) = Try {
    vector match {
      case t.AdaptiveVector(size, Sparse(mutableMap)) =>
        val convertedMap = mutableMap.toMap.map {
          case (key, value) => (key -> value.as[Option[T]].get)
        }
        AdaptiveVector.fromMap[T](convertedMap, sparseValue, size)
      case t.AdaptiveVector(size, Dense(vector)) =>
        val convertedVector = vector
          .map { obj =>
            obj.as[Option[T]].get
          }
          .as[Vector[T]]
        AdaptiveVector.fromVector(convertedVector, sparseValue)
    }
  }
}

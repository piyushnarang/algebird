package com.twitter.algebird_internal.injection

import com.twitter.algebird_internal.{thriftscala => t}

import com.twitter.algebird.{Monoid, SketchMap, SketchMapMonoid}
import com.twitter.algebird.matrix.AdaptiveMatrix
import com.twitter.bijection.{Conversion, Injection}

import Conversion.asMethod
import scala.util.Try

/**
 * Converts from Algebird SketchMap to Thrift SketchMap. Requires a monoid to
 * determine configuration.
 */
class SketchMapInjection[K, V](monoid: SketchMapMonoid[K, V])(
  implicit keyInj: Injection[K, t.Item],
  valInj: Injection[V, t.Item],
  ordering: Ordering[V],
  valMonoid: Monoid[V]
) extends Injection[SketchMap[K, V], t.SketchMap] {

  implicit val matrixBij: Injection[AdaptiveMatrix[V], t.AdaptiveMatrix] =
    new AdaptiveMatrixInjection[V]()(valInj, valMonoid)

  override def apply(sketchMap: SketchMap[K, V]) = {
    val heavyHitters = monoid.heavyHitters(sketchMap).map {
      case (key, value) =>
        t.HeavyHitter(key.as[t.Item], value.as[t.Item])
    }

    t.SketchMap(
      matrixBij.apply(sketchMap.valuesTable),
      heavyHitters,
      sketchMap.totalValue.as[t.Item]
    )
  }

  override def invert(thrift: t.SketchMap) = Try {
    thrift match {
      case t.SketchMap(valuesTable, heavyHitters, totalValue) => {
        val heavyHitterKeys = heavyHitters.toList.map { hh =>
          hh.key.as[Try[K]].get
        }

        SketchMap[K, V](
          matrixBij.invert(valuesTable).get,
          heavyHitterKeys,
          totalValue.as[Try[V]].get
        )
      }
    }
  }
}

package com.twitter.algebird_internal.bijection

import com.twitter.algebird_internal.thriftscala._

import com.twitter.algebird.{Monoid, SketchMap, SketchMapMonoid}
import com.twitter.algebird.matrix.AdaptiveMatrix
import com.twitter.bijection.{Bijection, Conversion, Injection}

import Conversion.asMethod

/**
 * Converts from Algebird SketchMap to Thrift SketchMap. Requires a monoid to
 * determine configuration.
 */
class SketchMapBijection[K, V](monoid: SketchMapMonoid[K, V])(
  implicit keyInj: Injection[K, TsmObject],
  valInj: Injection[V, TsmObject],
  ordering: Ordering[V],
  valMonoid: Monoid[V]
) extends Bijection[SketchMap[K, V], TsmSketchMap] {

  implicit val matrixBij: Bijection[AdaptiveMatrix[V], TsmAdaptiveMatrix] =
    new AdaptiveMatrixBijection[V]()(valInj, valMonoid)

  override def apply(sketchMap: SketchMap[K, V]): TsmSketchMap = {
    val heavyHitters = monoid.heavyHitters(sketchMap).map {
      case (key, value) =>
        TsmHeavyHitter(key.as[TsmObject], value.as[TsmObject])
    }

    TsmSketchMap(
      matrixBij.apply(sketchMap.valuesTable),
      heavyHitters,
      sketchMap.totalValue.as[TsmObject]
    )
  }

  override def invert(thrift: TsmSketchMap): SketchMap[K, V] = thrift match {
    case TsmSketchMap(valuesTable, heavyHitters, totalValue) => {
      val heavyHitterKeys = heavyHitters.toList.map { hh =>
        hh.key.as[Option[K]].get
      }
      SketchMap[K, V](
        matrixBij.invert(valuesTable),
        heavyHitterKeys,
        totalValue.as[Option[V]].get
      )
    }
  }
}

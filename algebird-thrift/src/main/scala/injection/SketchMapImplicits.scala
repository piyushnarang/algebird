package com.twitter.algebird_internal.injection

import com.twitter.algebird._
import com.twitter.algebird_internal.{thriftscala => thrift}
import com.twitter.bijection._

trait SketchMapImplicits {
  import ThriftImplicits._

  /**
   * Factory that creates SketchMap monoids based on SketchMap
   * configuration case classes.
   */
  implicit def sketchMapMonoid$[K, V](
    implicit params: SketchMapParams[K],
    ordering: Ordering[V],
    monoid: Monoid[V]
  ): SketchMapMonoid[K, V] =
    SketchMap.monoid[K, V](params)(ordering, monoid)

  implicit def sketchMapInjection$[K, V](
    implicit sketchMapMonoid: SketchMapMonoid[K, V],
    valueMonoid: Monoid[V],
    keyInj: Injection[K, thrift.Item],
    valInj: Injection[V, thrift.Item],
    ordering: Ordering[V]
  ): Injection[SketchMap[K, V], Array[Byte]] =
    new SketchMapInjection[K, V](sketchMapMonoid).andThen {
      Injection.connect[thrift.SketchMap, Array[Byte]]
    }
}

object SketchMapImplicits extends SketchMapImplicits

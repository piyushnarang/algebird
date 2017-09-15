package com.twitter.algebird_internal.injection

import com.twitter.algebird._
import com.twitter.algebird_internal.thriftscala.Item
import com.twitter.bijection.{Bijection, Codec, Conversion, Injection}
import org.scalacheck.Arbitrary
import org.scalacheck.Prop._
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers
import scala.collection.mutable.WrappedArray
import org.junit.runner.RunWith

import Conversion.asMethod

/**
 * Helper classes and objects exist below this class.
 */
@RunWith(classOf[JUnitRunner])
class HllSpec extends FlatSpec with Checkers {
  import AlgebirdImplicits._
  def rt[T: Codec](t: T): Option[T] = t.as[Array[Byte]].as[Option[T]]

  /**
   * Returns true if round tripping maintains equality AND that
   * deserialization doesn't fail, false otherwise.
   */
  def canRT[T: Codec: Equiv](t: T): Boolean = {
    val roundTripped = rt(t)
    roundTripped.isDefined && Equiv[T].equiv(t, roundTripped.get)
  }

  /**
   * Tests for HyperLogLog
   */
  "AlgebirdImplicits" should "allow for HLL custom config" in {
    assert(CustomScope.hllMonoid.bits == 24)
  }

  it should "allow custom sketch map config" in {
    assert {
      CustomScope.sketchParams.heavyHittersCount ==
        CustomScope.sketchMapMonoid.params.heavyHittersCount
    }
  }

  it should "round trip HLL instances" in {
    val monoid = CustomScope.hllMonoid
    check { i: Int =>
      canRT(Create.hll(monoid)(i))
    }
    check { i: Int =>
      canRT(Create.hll(monoid)(i))
    }
  }

  it should "round trip SketchMap instances" in {
    check { xs: List[(Int, Long)] =>
      /**
       * This implicit monoid MUST be in scope to create the proper
       * injection that gets passed implicitly to canRT.
       */
      implicit val monoid = CustomScope.sketchMapMonoid
      canRT(monoid.create(xs))
    }
  }

  it should "rt SketchMap instances with custom value types" in {
    check { xs: List[(Int, StringWrapper)] =>
      import CustomScope._ // Brings in the implicit configs
      implicit val monoid = SketchMap.monoid[Int, StringWrapper](sketchParams)
      val inj = implicitly[Injection[SketchMap[Int, StringWrapper], Array[Byte]]]
      val init = monoid.create(xs)
      val roundTripped = rt(init)

      roundTripped.isDefined && {
        val RT = roundTripped.get
        RT == init &&
        (init.valuesTable == RT.valuesTable)
        (init.heavyHitterKeys == RT.heavyHitterKeys)
        (init.totalValue == RT.totalValue)
      }
    }
  }

  /**
   * Tests for BloomFilter
   */
  it should "rt bloom filter item amd bloom filter zero" in {
    implicit val monoid = CustomScope.bloomFilterMonoid
    check { item: String =>
      canRT(monoid.create(item))
    }
    assert(canRT(monoid.zero))
  }

  it should "rt sparse bloomFilter" in {
    implicit val monoid = CustomScope.bloomFilterMonoid
    check { items: List[String] =>
      items.length > 1 ==> {
        val bf = monoid.create(items: _*)
        canRT(bf)
      }
    }
  }

  it should "rt dense bloomFilter" in {
    implicit val monoid = CustomScope.bloomFilterMonoid
    check { items: List[String] =>
      (items.length > 1) ==> { // Try to create BFSparse
        val bf = monoid.create(items: _*)
        val denseBF: BF[String] = bf match {
          case b: BFSparse[String] => b.dense
          case b: BFInstance[String] => b
          case _ => throw new IllegalStateException("Unexpected BloomFilter state")
        }
        canRT(denseBF)
      }
    }
  }

  /**
   * Tests for MinHasher
   */
  it should "rt MinHasher" in {
    implicit val hasher: MinHasher[AnyRef] = CustomScope.minHasherMonoid
    check { items: List[String] =>
      (items.length > 0) ==> {
        val sig = items
          .map { l =>
            hasher.init(l)
          }
          .reduce { (a, b) =>
            hasher.plus(a, b)
          }
        val rtSig = rt[MinHashSignature](sig).get
        (sig.bytes: WrappedArray[Byte]) == (rtSig.bytes: WrappedArray[Byte])
      }
    }
  }

  /**
   * Tests for AveragedValue
   */
  it should "round trip AveragedValue instances" in {
    check { (count: Int, value: Int) =>
      canRT(AveragedValue(count, value))
    }
  }

  /**
   * Tests for DecayedValue
   */
  it should "get customized DecayedValueMonoid with custom config" in {
    assert(CustomScope.decayedValueMonoid.eps == CustomScope.decayedValueConfig.eps)
  }

  it should "round trip decayed value" in {
    check { (value: Double, time: Double, halfLife: Double) =>
      (halfLife != 0) ==> canRT(DecayedValue.build[Double](value, time, halfLife))
    }
  }
}

object Create {
  def hll[T: Codec](monoid: HyperLogLogMonoid)(t: T): HLL =
    monoid.create(Injection[T, Array[Byte]](t))
}

object CustomScope extends AlgebirdImplicits {
  implicit def inj(i: Int) = i.as[Array[Byte]]
  // Custom implicits configuration parameters!
  implicit val params = HyperLogLogBits(24)
  implicit val sketchParams =
    SketchMapParams[Int](seed = 10, eps = 0.05, delta = 0.1, heavyHittersCount = 40)
  implicit val decayedValueConfig = DecayedValueConfig(0.12)
  implicit val bloomFilterConfig = BloomFilterConfig(100, 0.01)
  implicit val minHasherConfig = MinHasherConfig(0.4, 2048, 16)
  val hllMonoid = implicitly[HyperLogLogMonoid]
  val sketchMapMonoid = SketchMap.monoid[Int, Long](sketchParams)
  val decayedValueMonoid = implicitly[DecayedValueMonoid]
  val bloomFilterMonoid = implicitly[BloomFilterMonoid[String]]
  val minHasherMonoid = implicitly[MinHasher[AnyRef]]
}

case class StringWrapper(s: String)

object StringWrapper extends ThriftImplicits {
  implicit lazy val wrapperToString: Bijection[StringWrapper, String] =
    Bijection.build[StringWrapper, String](_.s)(StringWrapper(_))

  implicit lazy val wrapperToItem: Injection[StringWrapper, Item] =
    Injection.connect[StringWrapper, String, Item]

  implicit val stringWrapperGen: Arbitrary[StringWrapper] =
    Arbitrary {
      for {
        s <- Arbitrary.arbitrary[String]
      } yield StringWrapper(s)
    }

  implicit val ord: Ordering[StringWrapper] =
    Ordering.by(_.s)

  implicit val monoid: Monoid[StringWrapper] =
    Monoid.from(StringWrapper("")) { (l, r) =>
      StringWrapper(l.s + r.s)
    }
}

package com.twitter.algebird_internal.injection

import com.twitter.algebird._
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class QTreeInjectionSpec extends FlatSpec with Checkers {

  import ThriftImplicits._

  def randomList(n: Long) = (1L to n).map(i => math.random)

  def buildQTree(k: Int, list: Seq[Double]) = {
    val qtSemigroup = new QTreeSemigroup[Double](k)
    list.map(QTree(_)).reduce(qtSemigroup.plus(_, _))
  }

  for (k <- (1 to 6))
    ("QTree with sizeHint 2^" + k) should "properly go to and from thrift representations" in {
      val list = randomList(10000)
      val qt = buildQTree(k, list)
      val qti = new QTreeInjection[Double]()
      val tqt = qti.apply(qt)
      val qt1 = qti.invert(tqt).get
      assert(qt == qt1)
    }
}

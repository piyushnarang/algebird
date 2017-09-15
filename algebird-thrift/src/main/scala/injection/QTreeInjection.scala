package com.twitter.algebird_internal.injection

import com.twitter.algebird.{Monoid, QTree}
import com.twitter.algebird_internal.{thriftscala => t}
import com.twitter.bijection.{Conversion, Injection}
import scala.collection.mutable.ArrayBuffer
import scala.util.{Success, Try}

/**
 * Serialize Algebird QTree[T] to Thrift QTree.
 *
 * The implicit monoid is required to instantiate QTree[T] instances during invert.
 */
class QTreeInjection[T](implicit inj: Injection[T, t.Item], monoid: Monoid[T])
    extends Injection[QTree[T], t.QTree] {

  import Conversion.asMethod

  override def apply(root: QTree[T]): t.QTree = {
    t.QTree(flatten(root, 0))
  }

  override def invert(qtree: t.QTree): Try[QTree[T]] =
    inflate(qtree.nodes.toIndexedSeq, Some(qtree.nodes.length - 1)).map(_.get)

  def flatten(qtree: QTree[T], start: Int): Seq[t.QTreeNode] = {
    val ab = new ArrayBuffer[t.QTreeNode]
    flattenRecurse(qtree, start, ab)
    ab
  }

  /**
   * Uses mutability to be faster and produce less garbage.
   */
  private[this] def flattenRecurse(qtree: QTree[T], start: Int, ab: ArrayBuffer[t.QTreeNode]): Int = {
    var lowersLength = 0
    var lowersLengthOffseted: Option[Int] = None
    if (qtree.lowerChild.isDefined) {
      lowersLength = flattenRecurse(qtree.lowerChild.get, start, ab)
      lowersLengthOffseted = Some(lowersLength - 1 + start)
    }

    val start1 = start + lowersLength

    var uppersLength = 0
    var uppersLengthOffseted: Option[Int] = None
    if (qtree.upperChild.isDefined) {
      uppersLength = flattenRecurse(qtree.upperChild.get, start1, ab)
      uppersLengthOffseted = Some(uppersLength - 1 + start1)
    }

    ab += t.QTreeNode(
      offset = qtree.offset,
      level = qtree.level,
      count = qtree.count,
      sum = qtree.sum.as[t.Item],
      lowerChild = lowersLengthOffseted,
      upperChild = uppersLengthOffseted
    )
    lowersLength + uppersLength + 1
  }

  def inflate(nodes: IndexedSeq[t.QTreeNode], iOpt: Option[Int]): Try[Option[QTree[T]]] =
    iOpt match {
      case None => Success(None)
      case Some(i) => {
        val node = nodes(i)
        for {
          sum <- node.sum.as[Try[T]]
          lowerChild <- inflate(nodes, node.lowerChild)
          upperChild <- inflate(nodes, node.upperChild)
        } yield {
          Some(
            QTree[T](
              offset = node.offset,
              level = node.level,
              count = node.count,
              sum = sum,
              lowerChild = lowerChild,
              upperChild = upperChild
            )
          )
        }
      }
    }
}

trait QTreeImplicits {
  import ThriftImplicits._

  implicit def qtreeToThriftInjection[T](
    implicit inj: Injection[T, t.Item],
    monoid: Monoid[T]
  ): Injection[QTree[T], t.QTree] =
    new QTreeInjection[T]()

  implicit def qtreeToBytesInjection[T](
    implicit inj: Injection[T, t.Item],
    monoid: Monoid[T]
  ): Injection[QTree[T], Array[Byte]] =
    Injection.connect[QTree[T], t.QTree, Array[Byte]]
}

object QTreeImplicits extends QTreeImplicits

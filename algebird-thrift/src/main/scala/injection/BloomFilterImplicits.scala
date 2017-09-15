package com.twitter.algebird_internal.injection

import com.twitter.bijection.{Bijection, Injection}

import com.twitter.algebird._
import com.twitter.algebird_internal.thriftscala.{BloomFilter => ThriftBF, Item => ThriftItem}
import com.twitter.algebird_internal.bloom_filter.thriftscala.{
  DenseBitSetContent => TDenseBitSet,
  SparseBitSetContent => TSparseBitSet,
  BloomFilterContent => TContent,
  ZeroContent => TZeroContent,
  ItemContent => TItemContent
}
import com.googlecode.javaewah.{EWAHCompressedBitmap => CBitSet}
import scala.collection.JavaConverters._
import scala.collection.immutable.BitSet
import com.twitter.algebird.BloomFilterMonoid
import com.twitter.algebird.BFZero
import com.twitter.algebird.BFItem
import com.twitter.algebird.BFSparse
import scala.util.Try

case class BloomFilterConfig(numOfEntries: Int, fpProb: Double)

object SparseBitSetToThriftBijection extends Bijection[CBitSet, TSparseBitSet] {
  override def apply(bits: CBitSet): TSparseBitSet = {

    val indexesWithDeltaEncoding = bits.asScala
      .map(_.intValue())
      .toSeq
      .sorted
      .foldLeft(List[Int](), 0) {
        case ((acc, lastVal), currentVal) =>
          val delta = currentVal - lastVal
          (acc :+ delta, currentVal)
      }
      ._1
    TSparseBitSet(indexesWithDeltaEncoding)
  }

  override def invert(tbits: TSparseBitSet): CBitSet = {
    val bits: CBitSet = new CBitSet()
    tbits.bitSetSparseDelta
      .foldLeft(List[Int](), 0) {
        case ((acc, lastVal), delta) =>
          val newVal = delta + lastVal
          (acc :+ newVal, newVal)
      }
      ._1
      .foreach { index: Int =>
        bits.set(index)
      }
    bits
  }
}

object DenseBitSetToThriftBijection extends Bijection[BitSet, TDenseBitSet] {
  override def apply(bits: BitSet): TDenseBitSet = {

    /**
     * BitSet1 BitSet2 BitSetN follow a weird naming convention in scala 2.9.3
     * In the constructor, they each takes elems as parameter. Notice elems here is not the indexes.
     * It's the bitmap backing the bitset, which is Array[Long], or just Long in the case of BitSet1
     * Do not get confused by BitSetN.elems and BitSetN.elements. Only the latter is the indexes
     **/
    val bitmap: Array[Long] = bits match {
      case b: BitSet.BitSet1 => Array(b.elems)

      /**
       * In scala 2.9.3 BitSet2's constructor looks like  class BitSet2(val elems0: Long, elems1: Long)
       * There is no way to get the elems1 from it due to the missing of "val" in front of elems1
       * Following code uses a trick to get the backing bitmap:
       * Add it with BitSet(128) and slice only first 128 bits from it
       * BitSet(128) is backed by 3 Long values, since setting it needs 129 bits, Adding it with any BitSet2
       * will get a result of type BitSetN, from which elems(the bitmap) can be extracted
       **/
      case b: BitSet.BitSet2 => (b ++ BitSet(128)).asInstanceOf[BitSet.BitSetN].elems.slice(0, 2)
      case b: BitSet.BitSetN => b.elems
    }
    TDenseBitSet(bitmap)
  }

  override def invert(tbits: TDenseBitSet): BitSet = {
    BitSet.fromBitMask(tbits.bitSetDense.toArray)
  }
}

class BloomFilterToThriftInjection(implicit monoid: BloomFilterMonoid[String])
    extends Injection[BF[String], ThriftBF] {

  override def apply(bf: BF[String]) = {
    val content = bf match {
      case BFSparse(hashes, cbitmap, width) =>
        TContent.SparseBitsContent(SparseBitSetToThriftBijection(cbitmap))
      case BFZero(hashes, width) => TContent.ZeroContent(TZeroContent())
      case BFItem(item, hashes, width) =>
        TContent.ItemContent(TItemContent(ThriftItem.StringValue(item)))
      case BFInstance(hashes, bits, width) =>
        TContent.DenseBitsContent(DenseBitSetToThriftBijection(bits))
    }
    ThriftBF(content, monoid.numHashes, monoid.width)
  }

  override def invert(tbf: ThriftBF) = Try {
    tbf match {
      case ThriftBF(content, numOfHashes, width) => {
        content match {
          case TContent.SparseBitsContent(bitsContent) => {
            val bits: CBitSet = SparseBitSetToThriftBijection.invert(bitsContent)
            BFSparse(monoid.hashes, bits, monoid.width)
          }
          case TContent.DenseBitsContent(bitsContent) =>
            new BFInstance(
              monoid.hashes,
              DenseBitSetToThriftBijection.invert(bitsContent),
              monoid.width
            )
          case TContent.ZeroContent(zeroContent) => BFZero(monoid.hashes, monoid.width)
          case TContent.ItemContent(thriftItem) => {
            thriftItem.item match {
              case item: ThriftItem.StringValue =>
                BFItem(item.string_value, monoid.hashes, monoid.width)
              case _ =>
                throw new RuntimeException(
                  "currently BloomFilter in algebird only support String value"
                )
            }
          }
          case TContent.UnknownUnionField(f) => {
            throw new RuntimeException("Got an unknown union field: " + f)
          }
        }
      }
    }
  }
}

trait BloomFilterImplicits {
  import ThriftImplicits._

  implicit def bfMonoid(implicit bfConfig: BloomFilterConfig): BloomFilterMonoid[String] =
    BloomFilter(bfConfig.numOfEntries, bfConfig.fpProb)
  implicit def BFtoBytes(implicit monoid: BloomFilterMonoid[String]) = {
    new BloomFilterToThriftInjection()(monoid) andThen Injection.connect[ThriftBF, Array[Byte]]
  }
}

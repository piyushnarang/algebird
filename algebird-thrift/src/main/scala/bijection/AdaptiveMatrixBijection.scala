package com.twitter.algebird_internal.bijection

import com.twitter.algebird_internal.thriftscala._

import com.twitter.bijection.{Bijection, Conversion, Injection}
import com.twitter.algebird.{AdaptiveVector, Monoid}
import com.twitter.algebird.matrix.{AdaptiveMatrix, DenseMatrix, SparseColumnMatrix}

import Conversion.asMethod

/**
 * Adaptive Matrix/Vector Thrift Bijections
 * TODO: Implement and represent Thrift AdaptiveVector recursively.
 */
/**
 * Serialize Algebird AdaptiveMatrix to Thrift AdaptiveMatrix.
 */
class AdaptiveMatrixBijection[T](implicit inj: Injection[T, TsmObject], monoid: Monoid[T])
    extends Bijection[AdaptiveMatrix[T], TsmAdaptiveMatrix] {

  private implicit val vectorBijection: Bijection[AdaptiveVector[T], TsmAdaptiveVector] =
    new AdaptiveVectorBijection[T]()(inj, monoid)

  override def apply(matrix: AdaptiveMatrix[T]): TsmAdaptiveMatrix = matrix match {
    // A sparse column matrix is an IndexedSeq[AdaptiveVector]
    case SparseColumnMatrix(rowsByColumns) =>
      TsmAdaptiveMatrix(
        matrix.rows,
        matrix.cols,
        None,
        Some(rowsByColumns.map(_.as[TsmAdaptiveVector]))
      )

    case DenseMatrix(_, _, rowsByColumnsFlat) =>
      TsmAdaptiveMatrix(
        matrix.rows,
        matrix.cols,
        None,
        None,
        Some(rowsByColumnsFlat.map(_.as[TsmObject]))
      )
  }

  private def sparseVector(size: Int): AdaptiveVector[T] = AdaptiveVector.fill(size)(monoid.zero)

  override def invert(matrix: TsmAdaptiveMatrix): AdaptiveMatrix[T] = matrix match {
    case TsmAdaptiveMatrix(rows, cols, Some(mutableMap), None, None) =>
      val convertedMap = mutableMap.toMap.map {
        case (key, value) => (key -> value.as[AdaptiveVector[T]])
      }
      val sparseData = (0 until rows).map { row =>
        convertedMap.getOrElse(row, sparseVector(cols))
      }.toIndexedSeq
      SparseColumnMatrix(sparseData)

    case TsmAdaptiveMatrix(rows, cols, None, Some(vector), None) =>
      SparseColumnMatrix(vector.map(_.as[AdaptiveVector[T]]).toIndexedSeq)

    case TsmAdaptiveMatrix(rows, cols, None, None, Some(data)) =>
      DenseMatrix(rows, cols, data.map { obj =>
        obj.as[Option[T]].get
      }.toIndexedSeq)

    case _ => sys.error("Invalid serialization.")
  }
}

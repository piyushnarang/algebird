package com.twitter.algebird_internal.injection

import com.twitter.algebird_internal.{thriftscala => t}

import com.twitter.bijection.{Conversion, Injection}
import com.twitter.algebird.{AdaptiveVector, Monoid}
import com.twitter.algebird.matrix.{AdaptiveMatrix, DenseMatrix, SparseColumnMatrix}

import Conversion.asMethod
import scala.util.Try

/**
 * Convert Algebird AdaptiveMatrix to Thrift AdaptiveMatrix.
 */
class AdaptiveMatrixInjection[T](implicit inj: Injection[T, t.Item], monoid: Monoid[T])
    extends Injection[AdaptiveMatrix[T], t.AdaptiveMatrix] {
  import t.MatrixContents.{Sparse, Dense, FullDense}

  private implicit val vectorInjection: Injection[AdaptiveVector[T], t.AdaptiveVector] =
    new AdaptiveVectorInjection[T]

  private def sparseVector(size: Int): AdaptiveVector[T] =
    AdaptiveVector.fill(size)(monoid.zero)

  override def apply(matrix: AdaptiveMatrix[T]) =
    matrix match {
      case SparseColumnMatrix(rowsByColumns) =>
        t.AdaptiveMatrix(matrix.rows, matrix.cols, Dense(rowsByColumns.map(_.as[t.AdaptiveVector])))
      case DenseMatrix(_, _, rowsByColumnsFlat) =>
        t.AdaptiveMatrix(matrix.rows, matrix.cols, FullDense(rowsByColumnsFlat.map(_.as[t.Item])))
    }

  override def invert(matrix: t.AdaptiveMatrix) =
    Try {
      matrix match {
        case t.AdaptiveMatrix(rows, cols, Sparse(mutableMap)) =>
          val convertedMap = mutableMap.toMap.map {
            case (key, value) => (key -> value.as[Try[AdaptiveVector[T]]].get)
          }
          val sparseData = (0 until rows).map { row =>
            convertedMap.getOrElse(row, sparseVector(cols))
          }.toIndexedSeq
          SparseColumnMatrix(sparseData)

        case t.AdaptiveMatrix(rows, cols, Dense(vector)) =>
          SparseColumnMatrix(
            vector.map(_.as[Try[AdaptiveVector[T]]].get).as[Vector[AdaptiveVector[T]]]
          )

        case t.AdaptiveMatrix(rows, cols, FullDense(data)) =>
          DenseMatrix(rows, cols, data.map { obj =>
            obj.as[Option[T]].get
          }.toIndexedSeq)
      }
    }
}

package com.twitter.algebird_internal.tests

import com.twitter.algebird_internal.thriftscala._

import com.twitter.algebird.{Monoid, AdaptiveVector, DenseVector, SparseVector}
import com.twitter.algebird.matrix.{AdaptiveMatrix, SparseColumnMatrix}
import com.twitter.algebird_internal.bijection.{
  ThriftImplicits,
  AdaptiveMatrixBijection,
  AdaptiveVectorBijection
}
import com.twitter.bijection.{Bijection, Conversion}

import org.specs._
import scala.util.Random

import Conversion.asMethod

class TsmAdaptiveMatrixSpec extends SpecificationWithJUnit {
  import ThriftImplicits._

  implicit val vectorBij: Bijection[AdaptiveVector[Long], TsmAdaptiveVector] =
    new AdaptiveVectorBijection[Long]

  implicit val matrixBij: Bijection[AdaptiveMatrix[Long], TsmAdaptiveMatrix] =
    new AdaptiveMatrixBijection[Long]

  "TsmAdaptiveVector" should {
    "Convert from Algebird AdaptiveVector and back" in {
      "Sparse Empty" in {
        val vector: AdaptiveVector[Long] = AdaptiveVector.fill(3)(0L)
        val thrift = vector.as[TsmAdaptiveVector]

        thrift mustEqual TsmAdaptiveVector(3, Some(Map.empty[Int, TsmObject]), None)

        val back = thrift.as[AdaptiveVector[Long]]
        back mustEqual vector
      }

      "Sparse" in {
        val vector: AdaptiveVector[Long] = AdaptiveVector.fromVector(Vector(1L, 0L, 0L, 0L, 0L), 0L)
        val thrift = vector.as[TsmAdaptiveVector]

        thrift mustEqual TsmAdaptiveVector(5, Some(Map(0 -> TsmObject.LongValue(1L))), None)

        val back = thrift.as[AdaptiveVector[Long]]
        back mustEqual vector
      }

      "Dense" in {
        val vector: AdaptiveVector[Long] = AdaptiveVector.fromVector(Vector.fill(3)(1L), 0L)
        val thrift = vector.as[TsmAdaptiveVector]

        thrift mustEqual TsmAdaptiveVector(
          3,
          None,
          Some(
            Seq(
              TsmObject.LongValue(1L),
              TsmObject.LongValue(1L),
              TsmObject.LongValue(1L)
            )
          )
        )

        val back = thrift.as[AdaptiveVector[Long]]
        back mustEqual vector
      }
    }
  }

  "TsmAdaptiveMatrix" should {
    "Convert from Algebird AdaptiveMatrix and back" in {

      "Dense" in {
        // 2x2 Matrix of 1L's
        val sparseVector: AdaptiveVector[Long] = AdaptiveVector.fromVector(Vector.fill(2)(0L), 0L)
        val vector: AdaptiveVector[Long] = AdaptiveVector.fromVector(Vector.fill(2)(1L), 0L)
        val vector2: IndexedSeq[AdaptiveVector[Long]] = Vector.fill(2)(vector)
        val matrix: AdaptiveMatrix[Long] = SparseColumnMatrix(vector2)
        val thrift = matrix.as[TsmAdaptiveMatrix]

        thrift mustEqual TsmAdaptiveMatrix(
          2,
          2,
          None,
          Some(
            Seq(
              TsmAdaptiveVector(
                2,
                None,
                Some(Seq(TsmObject.LongValue(1L), TsmObject.LongValue(1L)))
              ),
              TsmAdaptiveVector(
                2,
                None,
                Some(Seq(TsmObject.LongValue(1L), TsmObject.LongValue(1L)))
              )
            )
          )
        )

        val back = thrift.as[AdaptiveMatrix[Long]]
        back mustEqual matrix
      }
    }
  }
}

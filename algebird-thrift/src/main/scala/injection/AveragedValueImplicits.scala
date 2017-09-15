package com.twitter.algebird_internal.injection
import com.twitter.bijection.{Injection, Bijection}
import com.twitter.algebird._
import com.twitter.algebird_internal.thriftscala.{AveragedValue => ThriftAV}

trait AveragedValueImplicits {
  import ThriftImplicits._
  implicit val avgValueToBytes: Injection[AveragedValue, Array[Byte]] =
    Bijection.build[AveragedValue, ThriftAV] { avg =>
      ThriftAV(avg.count, avg.value)
    } { t =>
      AveragedValue(t.count, t.value)
    } andThen Injection.connect[ThriftAV, Array[Byte]]
}

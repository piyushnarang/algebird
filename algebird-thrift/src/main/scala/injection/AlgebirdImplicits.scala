package com.twitter.algebird_internal.injection

/**
 * Trait that stacks up all of the interesting hyperloglog and
 * sketchmap traits.
 */
trait AlgebirdImplicits
    extends ThriftImplicits
    with SketchMapImplicits
    with HyperLogLogImplicits
    with BloomFilterImplicits
    with MinHashImplicits
    with DecayedValueImplicits
    with AveragedValueImplicits
    with QTreeImplicits
object AlgebirdImplicits extends AlgebirdImplicits

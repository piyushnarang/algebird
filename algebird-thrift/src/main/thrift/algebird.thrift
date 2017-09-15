namespace java com.twitter.algebird_internal.thrift
#@namespace scala com.twitter.algebird_internal.thriftscala

include "algebird_common.thrift"
include "bloomfilter.thrift"

/**
 * AdaptiveVector representation.
 */
union VectorContents {
  1: map<i32, algebird_common.Item> sparse
  2: list<algebird_common.Item> dense
}

struct AdaptiveVector {
  1: i32 size
  2: VectorContents vector;
}


/**
 * Adaptive Matrix
 */
union MatrixContents {
  1: map<i32, AdaptiveVector> sparse
  2: list<AdaptiveVector> dense
  3: list<algebird_common.Item> fullDense
}

struct AdaptiveMatrix {
  1: i32 rows
  2: i32 columns
  3: MatrixContents matrix
}

/**
 * Heavy Hitter
 */
struct HeavyHitter {
  1: algebird_common.Item key
  2: algebird_common.Item value
}

/**
 * SketchMap with multiple values. This can represent any SketchMap<K, V> by
 * inspecting total_count
 */
struct SketchMap {
  /**
   * 2D Vector that holds value approximations.
   */
  1: AdaptiveMatrix values_table

  /**
   * Sorted set of heavy hitters.
   * We store the associated value so we don't have to deserialize just to see
   * heavy hitter values.
   */
  2: list<HeavyHitter> heavy_hitters

  /**
   * Sum of all values seen in the data stream.
   */
  3: algebird_common.Item total_value
}

/**
 * HyperLogLog wrapper in Thrift.
 * Used for easy case matching/bijection.
 */
struct Hll {
  1: binary bytes
}

/**
 * BloomFilter wrapper in Thrift.
 */
struct BloomFilter {
   1: bloomfilter.BloomFilterContent content
   2: i32 num_of_hashes
   3: i32 width
 }

/**
 * MinHash wrapper in Thrift
 */
 struct MinHashSig {
   1: binary bytes
   2: i32 num_of_hashes
   3: i32 num_of_bands
   4: i32 hash_size
 }

/**
 * DecayedValue wrapper in Thrift
 */
struct DecayedValue {
  1: double value
  2: double scaled_time
}(persisted='true')

/**
 * AveragedValue wrapper in Thrift
 */
struct AveragedValue {
  1: i64 count
  2: double value
}

/**
 * QTree representation.
 * Flatten the tree into a list and use indexes to refer to the children.
 */
struct QTreeNode {
  1: required i64 offset
  2: required i32 level
  3: required i64 count,
  4: required algebird_common.Item sum
  5: optional i32 lower_child
  6: optional i32 upper_child
}

struct QTree {
  1: required list<QTreeNode> nodes
}

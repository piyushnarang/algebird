namespace java com.twitter.algebird_internal.thrift
#@namespace scala com.twitter.algebird_internal.thriftscala

//
// Sketch Map
//

/**
 * Arbitrary field that can contain any Sketch Map serializable type. Shared for
 * both key and value types.
 */
union TsmObject {
  1: i64 long_value
  2: string string_value
  3: binary bytes_value
}

/**
 * AdaptiveVector representation.
 */
struct TsmAdaptiveVector {
  1: i32 size
  2: optional map<i32, TsmObject> sparse
  3: optional list<TsmObject> dense
}

/** Adaptive Matrix **/
struct TsmAdaptiveMatrix {
  1: i32 rows
  2: i32 columns
  3: optional map<i32, TsmAdaptiveVector> sparse
  4: optional list<TsmAdaptiveVector> dense
  5: optional list<TsmObject> fullDense
}

/** Heavy Hitter **/
struct TsmHeavyHitter {
  1: TsmObject key
  2: TsmObject value
}

/**
 * SketchMap with multiple values. This can represent any SketchMap<K, V> by
 * inspecting total_count
 */
struct TsmSketchMap {
  /** 2D Vector that holds value approximations. */
  1: TsmAdaptiveMatrix values_table

  /**
   * Sorted set of heavy hitters.
   * We store the associated value so we don't have to deserialize just to see
   * heavy hitter values.
   */
  2: list<TsmHeavyHitter> heavy_hitters

  /** Total value seen in the data stream. **/
  3: TsmObject total_value
}

/**
 * HyperLogLog wrapper in Thrift.
 * Used for easy case matching/bijection.
 */
struct ThriftHll {
  1: binary bytes
}

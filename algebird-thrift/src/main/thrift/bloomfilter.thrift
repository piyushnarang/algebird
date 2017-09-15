namespace java com.twitter.algebird_internal.bloom_filter.thrift
#@namespace scala com.twitter.algebird_internal.bloom_filter.thriftscala

include "algebird_common.thrift"

 /**
  * Thrift struct for BitSet in BFSparse
  * It stores delta encoded indexes in BitSet
  **/
struct SparseBitSetContent {
  1: list<i32> bit_set_sparse_delta
}

/**
 * Thrift struct for EWAHCompressedBitmap used in BFInstance
 * bit_set_dense is the bitmap backing the EWAHCompressedBitmap, not indexes
 **/
struct DenseBitSetContent {
  1: list<i64> bit_set_dense
}

/**
 * Thrift struct for String item used in BFItem
 * current Algebird only support String
 * But since Item is used as type here, it can support other
 * primitive types defined in Item in the future(as long as BloomFilter in algebird supports them)
 **/
struct ItemContent {
  1: algebird_common.Item item
}

/**
 * Thrift struct for BFZero
 **/
struct ZeroContent {

}

/**
 * Thrift struct for the content in BF(in algebird)
 **/
union BloomFilterContent {
  1: SparseBitSetContent sparse_bits_content
  2: DenseBitSetContent dense_bits_content
  3: ItemContent item_content
  4: ZeroContent zero_content
}

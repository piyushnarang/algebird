namespace java com.twitter.algebird_internal.thrift
#@namespace scala com.twitter.algebird_internal.thriftscala

/**
 * The thrift analog of Scala's Unit
 */
struct Empty {}

/**
 * Arbitrary field that can contain any Sketch Map serializable type. Shared for
 * both key and value types.
 */
union Item {
  1: i16 short_value
  2: i32 int_value
  3: i64 long_value
  4: double double_value
  5: string string_value
  6: binary bytes_value
  7: Empty empty_value
}



/**
 * Now, the tuples.
 */
struct Tuple2 {
  1: Item a;
  2: Item b;
}

struct Tuple3 {
  1: Item a;
  2: Item b;
  3: Item c;
}

struct Tuple4 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
}

struct Tuple5 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
}

struct Tuple6 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
}

struct Tuple7 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
}

struct Tuple8 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
  8: Item h;
}

struct Tuple9 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
  8: Item h;
  9: Item i;
}

struct Tuple10 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
  8: Item h;
  9: Item i;
  10: Item j;
}

struct Tuple11 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
  8: Item h;
  9: Item i;
  10: Item j;
  11: Item k;
}

struct Tuple12 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
  8: Item h;
  9: Item i;
  10: Item j;
  11: Item k;
  12: Item l;
}

struct Tuple13 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
  8: Item h;
  9: Item i;
  10: Item j;
  11: Item k;
  12: Item l;
  13: Item m;
}

struct Tuple14 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
  8: Item h;
  9: Item i;
  10: Item j;
  11: Item k;
  12: Item l;
  13: Item m;
  14: Item n;
}

struct Tuple15 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
  8: Item h;
  9: Item i;
  10: Item j;
  11: Item k;
  12: Item l;
  13: Item m;
  14: Item n;
  15: Item o;
}

struct Tuple16 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
  8: Item h;
  9: Item i;
  10: Item j;
  11: Item k;
  12: Item l;
  13: Item m;
  14: Item n;
  15: Item o;
  16: Item p;
}

struct Tuple17 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
  8: Item h;
  9: Item i;
  10: Item j;
  11: Item k;
  12: Item l;
  13: Item m;
  14: Item n;
  15: Item o;
  16: Item p;
  17: Item q;
}

struct Tuple18 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
  8: Item h;
  9: Item i;
  10: Item j;
  11: Item k;
  12: Item l;
  13: Item m;
  14: Item n;
  15: Item o;
  16: Item p;
  17: Item q;
  18: Item r;
}

struct Tuple19 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
  8: Item h;
  9: Item i;
  10: Item j;
  11: Item k;
  12: Item l;
  13: Item m;
  14: Item n;
  15: Item o;
  16: Item p;
  17: Item q;
  18: Item r;
  19: Item s;
}

struct Tuple20 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
  8: Item h;
  9: Item i;
  10: Item j;
  11: Item k;
  12: Item l;
  13: Item m;
  14: Item n;
  15: Item o;
  16: Item p;
  17: Item q;
  18: Item r;
  19: Item s;
  20: Item t;
}

struct Tuple21 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
  8: Item h;
  9: Item i;
  10: Item j;
  11: Item k;
  12: Item l;
  13: Item m;
  14: Item n;
  15: Item o;
  16: Item p;
  17: Item q;
  18: Item r;
  19: Item s;
  20: Item t;
  21: Item u;
}

struct Tuple22 {
  1: Item a;
  2: Item b;
  3: Item c;
  4: Item d;
  5: Item e;
  6: Item f;
  7: Item g;
  8: Item h;
  9: Item i;
  10: Item j;
  11: Item k;
  12: Item l;
  13: Item m;
  14: Item n;
  15: Item o;
  16: Item p;
  17: Item q;
  18: Item r;
  19: Item s;
  20: Item t;
  21: Item u;
  22: Item v;
}

package com.pangrel.pakaimasker

data class Summary(val totalScanned: Int?, val totalMasked: Int?, val totalUnmasked: Int?, val lastUpdate: String?) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}

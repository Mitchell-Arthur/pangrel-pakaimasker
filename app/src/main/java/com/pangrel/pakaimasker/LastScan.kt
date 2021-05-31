package com.pangrel.pakaimasker

data class LastScan(val datetime: String, val masked: Boolean) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}

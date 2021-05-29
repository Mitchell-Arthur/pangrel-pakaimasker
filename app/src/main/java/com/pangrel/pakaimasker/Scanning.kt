package com.pangrel.pakaimasker

data class Scanning(val time: String, val result: ClassificationResult, val passed: Boolean) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}

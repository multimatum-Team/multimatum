package com.github.multimatum_team.multimatum.util

fun <T, K, V> List<T>.associateNotNull(transform: (T) -> Pair<K, V?>): Map<K, V> =
    this.mapNotNull {
        val (k, v) = transform(it)
        v?.let { k to v }
    }
    .toMap()
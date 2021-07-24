package com.ilab.yougetmobiledl.base

import java.util.concurrent.ConcurrentHashMap

fun <T> ConcurrentHashMap<*, T>.mapValues(): MutableList<T> {
    return values.toMutableList()
}
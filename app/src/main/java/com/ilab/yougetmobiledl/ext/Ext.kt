package com.ilab.yougetmobiledl.ext

import java.util.concurrent.ConcurrentHashMap

fun <T> ConcurrentHashMap<*, T>.mapValues(): MutableList<T> {
    return values.toMutableList()
}
package org.lmd.project

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

inline fun <T> networkBoundResource(
    crossinline fetch: suspend () -> T
): Flow<Result<T>> = flow {
    val data = fetch()
    emit(Result.success(data))
}.catch { e ->
    emit(Result.failure(e))
}
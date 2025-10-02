package org.example.project.myPool.domian.model


sealed class RelativeTime {
    data object JustNow : RelativeTime()
    data class MinutesAgo(val minutes: Int) : RelativeTime()
    data class HoursAgo(val hours: Int) : RelativeTime()
    data class DaysAgo(val days: Int) : RelativeTime()
    data object Unknown : RelativeTime()
}
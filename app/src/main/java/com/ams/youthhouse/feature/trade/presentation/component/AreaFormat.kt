package com.ams.youthhouse.feature.trade.presentation.component

/** `59.58` → `59.58㎡`, `85.0` → `85㎡` */
internal fun Double.formatArea(): String {
    val text = if (this % 1.0 == 0.0) toInt().toString() else toString()
    return "$text㎡"
}

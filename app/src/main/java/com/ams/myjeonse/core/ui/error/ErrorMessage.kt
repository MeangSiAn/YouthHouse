package com.ams.myjeonse.core.ui.error

import androidx.annotation.StringRes
import com.ams.myjeonse.R
import com.ams.myjeonse.core.common.error.AppException

/**
 * 실패를 사용자에게 보여줄 문구로 옮긴다.
 *
 * [AppException]은 문구를 담지 않는다(지역화는 presentation 책임). 그 매핑이 여기다.
 * 화면마다 복사본을 두지 않도록 core에 둔다.
 */
@StringRes
fun Throwable.toUserMessageRes(): Int = when (this) {
    is AppException.Network -> R.string.error_network
    is AppException.Unauthorized -> R.string.error_service_key
    is AppException.Server -> R.string.error_server
    is AppException.Parse -> R.string.error_parse
    else -> R.string.error_unknown
}

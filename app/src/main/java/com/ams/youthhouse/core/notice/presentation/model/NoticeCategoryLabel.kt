package com.ams.youthhouse.core.notice.presentation.model

import androidx.annotation.StringRes
import com.ams.youthhouse.R
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory

/** 분야의 표시 문구. 도메인 enum이 문자열 리소스를 알지 않도록 여기서 잇는다. */
@get:StringRes
val NoticeCategory.labelRes: Int
    get() = when (this) {
        NoticeCategory.RENTAL -> R.string.notice_category_rental
        NoticeCategory.SALE -> R.string.notice_category_sale
    }

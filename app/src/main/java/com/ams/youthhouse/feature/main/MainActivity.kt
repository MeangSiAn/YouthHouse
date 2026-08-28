package com.ams.youthhouse.feature.main

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ams.youthhouse.R
import com.ams.youthhouse.core.ui.base.BaseActivity
import com.ams.youthhouse.feature.main.intro.IntroScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 런처 테마는 첫 창의 흰 깜빡임을 없애려고 배경만 코발트로 둔 것이라
        // 창이 뜬 뒤에는 앱 테마로 되돌린다.
        setTheme(R.style.Theme_YouthHouse)
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun ActivityContent() {
        // 인트로는 홈 위에 덮어 둔다. 아래에서 홈이 인트로와 동시에 데이터를 받기 시작하므로
        // 인트로가 걷힐 때쯤이면 대부분 목록이 준비돼 있다.
        // rememberSaveable이라 화면 회전이나 프로세스 복원 뒤에는 다시 재생되지 않는다.
        var isIntroFinished by rememberSaveable { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            MainRoute()

            if (!isIntroFinished) {
                IntroScreen(onFinished = { isIntroFinished = true })
            }
        }
    }
}

package com.ams.youthhouse.core.backup.domain.repository

import com.ams.youthhouse.core.backup.domain.model.BackupSummary

/**
 * 백업 파일 쓰기·읽기.
 *
 * 파일 위치는 문자열 URI로 받는다 — 어디에 쓸지는 사용자가 시스템 파일 선택기에서
 * 고르고, 도메인은 안드로이드 `Uri`를 모른다.
 */
interface BackupRepository {

    /** 기기의 찜·관심 단지·임장노트·설정을 [targetUri]에 쓴다. */
    suspend fun export(targetUri: String): BackupSummary

    /**
     * [sourceUri]를 읽어 기기에 합친다. 같은 단지의 노트는 덮어쓰고, 찜은 없는 것만 더한다.
     * 지우지는 않는다 — 불러오기가 기존 기록을 없애면 안 된다.
     *
     * @throws BackupFormatException 이 앱의 백업 파일이 아닐 때
     */
    suspend fun import(sourceUri: String): BackupSummary
}

class BackupFormatException(message: String) : RuntimeException(message)

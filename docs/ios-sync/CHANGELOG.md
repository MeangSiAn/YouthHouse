# Android → iOS 변경 기록

최신 항목이 위. 읽는 법·쓰는 법은 [README.md](README.md).

<!-- 새 항목은 이 줄 바로 아래에 추가한다. -->

## 2026-09-06 · (해시 미정) · chore: ignore the whole .idea directory

iOS 영향 없음 (Android Studio 설정 파일 무시).

---

## 기준선 · `e66e26f` · 2026-09-06

**커밋 `e66e26f`(feature: fetch notices from our backend instead of data.go.kr)까지는 iOS에 이미 반영됐다.**
이 항목 아래에는 기록이 없다. iOS는 여기서부터 위로 읽는다.

기준선 시점의 Android 구성 요약 (참고용):

- 탭 5개: 홈 / 공고 / 내 일정 / 매매 / 마이
- 공고: 자체 백엔드 `GET notice?category&sido&status&limit&offset`, `GET notice/{noticeId}`. 찜은 `noticeId` 키, 스냅숏 저장(notice.db v3)
- 매매: 단지 검색·상세(실거래 차트, K-apt 단지 정보)·관심 단지·최근 본 단지·임장노트(단지당 1개, 채광/소음/주차/단지 관리 1~5점, 역 도보, 엘리베이터, 누수·곰팡이, 메모)·임장 단지 비교(최대 3곳). trade.db v3
- 홈: 마감 임박 카드, 오늘의 새 공고, 공고 현황, 내 일정 2건, 관심 단지 2곳, 임장기록 블록(예정 카드 + 완료 행 + 비교 버튼), 곧 열릴 공고(접수중 0건일 때), 알아두면 좋은 것(가이드 3편)
- 마이: 개인정보 미수집 안내, 관심 지역·공고 분야 설정, 개인정보처리방침 링크, 앱 버전
- 공고 목록 필터 칩: 지역(드롭다운) · 분야 · 접수 상태(접수중/접수 예정/마감 포함)

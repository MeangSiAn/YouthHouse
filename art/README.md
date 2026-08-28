# 스토어·아이콘 원본

앱에 들어가는 런처 아이콘은 `app/src/main/res/`의 벡터가 단일 소스다. 여기 있는 SVG는
**Play Console 등록정보처럼 앱 밖에서 필요한 이미지**를 만들 때 쓰는 원본이다.

| 파일 | 용도 |
|---|---|
| `ic_launcher_play_512.svg` | Play Console 앱 아이콘 512×512 PNG의 원본 |
| `ic_launcher_foreground.svg` | 밀도별 비트맵을 다시 만들고 싶을 때 Image Asset Studio에 넣는 전경 |

## 512×512 PNG 만들기

Play Console은 **512×512, 32비트 PNG(알파 포함), 1MB 이하**를 요구한다.
로컬에 SVG 렌더링 도구가 없으므로 아래 중 하나를 쓴다.

- **Android Studio** — `ic_launcher_play_512.svg`를 열고 내보내기, 또는 Image Asset Studio에
  `ic_launcher_foreground.svg`를 전경으로, 배경색 `#1E3FA0`으로 넣고 생성
- **브라우저** — SVG를 열어 512×512로 스크린샷 후 크기 맞춤
- **CLI** — `rsvg-convert -w 512 -h 512 ic_launcher_play_512.svg -o ic_launcher_512.png`
  (`brew install librsvg`)

## 좌표 규칙

심볼 좌표는 `core/designsystem/component/BrandMark.kt`의 **78 뷰포트** 기준이다.
다른 캔버스로 옮길 때는 배율과 이동만 바꾸고 좌표 자체는 건드리지 않는다.
그래야 인트로 화면·런처 아이콘·스토어 아이콘의 심볼이 갈라지지 않는다.

- 게시판: `(12,14)` 크기 `54×52`, 모서리 `9`, 선 굵기 `3.4`
- 공고 줄: `(24,31)-(50,31)`, `(24,40)-(54,40)`, `(24,49)-(42,49)`
- 알림 점: 중심 `(60,18)`, 반지름 `9`, `#FF7A6E`
- 심볼 중심 `(39.65, 38.35)`, 중심에서 가장 먼 지점까지 `37.8` (알림 점 바깥쪽)

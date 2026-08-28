# R8 keep 규칙.
#
# 원칙: 추측으로 규칙을 넣지 않는다. 규칙을 넓게 깔면 R8이 할 일이 없어져 축소를 켠 의미가
# 사라지고, 나중에 진짜 필요한 규칙이 무엇이었는지도 알 수 없게 된다.
# 릴리즈 빌드를 실기기에서 돌려 실제로 깨지는 지점이 나올 때만, 그 이유를 적고 추가한다.
#
# 이 프로젝트가 쓰는 라이브러리(Retrofit, kotlinx.serialization, Paging3, Hilt, OkHttp)는
# 모두 자기 keep 규칙을 AAR/JAR에 담아 배포하므로 대부분 자동으로 지켜진다.

# 크래시 리포트에서 줄 번호를 읽을 수 있게 한다. 소스 파일명은 가린다.
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

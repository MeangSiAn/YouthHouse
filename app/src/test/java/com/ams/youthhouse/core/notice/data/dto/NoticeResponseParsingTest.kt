package com.ams.youthhouse.core.notice.data.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 실제 API 응답을 기준으로 DTO 역직렬화를 고정한다.
 *
 * 여기 쓰는 [Json] 설정은 `core/network/di/NetworkModule.provideJson()`과 동일해야 한다.
 * 설정이 갈라지면 앱에서만 파싱이 깨지고 테스트는 통과하는 상황이 생긴다.
 */
class NoticeResponseParsingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Test
    fun `정상 응답에서 item 목록을 파싱한다`() {
        val response = json.decodeFromString<NoticeListResponseDto>(NORMAL_RESPONSE).response

        assertEquals("00", response.header.resultCode)
        assertEquals("383", response.body?.totalCount)
        assertEquals(2, response.body?.item?.size)

        val first = response.body?.item?.first()
        assertEquals("20955", first?.pblancId)
        assertEquals(0, first?.houseSn)
        assertEquals("매입임대", first?.suplyTyNm)
        assertEquals("울산광역시", first?.brtcNm)
        assertEquals(3, first?.sumSuplyCo)
    }

    @Test
    fun `item이 1건이어도 배열로 파싱된다`() {
        val response = json.decodeFromString<NoticeListResponseDto>(SINGLE_ITEM_RESPONSE).response

        assertEquals(1, response.body?.item?.size)
    }

    /** 결과가 0건이면 body 키 자체가 내려오지 않는다. 여기서 크래시가 나면 목록 화면이 통째로 죽는다. */
    @Test
    fun `body 키가 없는 NODATA 응답에서 body는 null이다`() {
        val response = json.decodeFromString<NoticeListResponseDto>(NO_DATA_RESPONSE).response

        assertEquals("03", response.header.resultCode)
        assertEquals("NODATA_ERROR", response.header.resultMsg)
        assertNull(response.body)
    }

    @Test
    fun `페이지 범위를 넘으면 item이 빈 배열이다`() {
        val response = json.decodeFromString<NoticeListResponseDto>(OUT_OF_RANGE_RESPONSE).response

        assertEquals("00", response.header.resultCode)
        assertTrue(response.body?.item?.isEmpty() == true)
    }

    @Test
    fun `모르는 필드가 추가되어도 파싱에 실패하지 않는다`() {
        val response = json.decodeFromString<NoticeListResponseDto>(UNKNOWN_FIELD_RESPONSE).response

        assertEquals("20955", response.body?.item?.first()?.pblancId)
    }

    /** 값 없음을 빈 문자열로 주는 API 특성. null 정규화는 매퍼의 책임이므로 DTO는 원문을 유지한다. */
    @Test
    fun `빈 문자열 필드가 그대로 유지된다`() {
        val response = json.decodeFromString<NoticeListResponseDto>(NORMAL_RESPONSE).response
        val first = response.body?.item?.first()

        assertEquals("", first?.hsmpNm)
        assertEquals("", first?.fullAdres)
        assertEquals("", first?.beforePblancId)
    }

    /**
     * 회귀 테스트: `totHshldCo`가 383건 중 82건에서 빈 문자열로 온다(페이지 2부터 등장).
     * 이걸 `Int`로 그냥 받으면 해당 페이지 전체가 역직렬화에 실패해 목록이 멈춘다.
     */
    @Test
    fun `숫자 필드가 빈 문자열로 와도 파싱된다`() {
        val response = json.decodeFromString<NoticeListResponseDto>(MIXED_NUMBER_TYPE_RESPONSE)
            .response
        val items = response.body?.item.orEmpty()

        assertEquals(3, items.size)
        assertEquals(0, items[0].totHshldCo)
        assertEquals(120, items[1].totHshldCo)
        assertEquals(37, items[2].totHshldCo)
    }

    @Test
    fun `숫자 필드가 따옴표 붙은 숫자로 와도 파싱된다`() {
        val response = json.decodeFromString<NoticeListResponseDto>(MIXED_NUMBER_TYPE_RESPONSE)
            .response
        val items = response.body?.item.orEmpty()

        assertEquals(3, items[1].sumSuplyCo)
        assertEquals(12_000_000, items[1].rentGtn)
    }

    /**
     * 분양 응답에는 임대 전용 키(rentGtn/mtRntchrg/suplyTyNm/totHshldCo/suplyHoCo)가
     * **아예 없다.** DTO를 공유하기로 한 결정 전체가 이 테스트에 달려 있다.
     */
    @Test
    fun `임대 전용 키가 없는 분양 응답도 파싱된다`() {
        val response = json.decodeFromString<NoticeListResponseDto>(SALE_RESPONSE).response
        val item = response.body?.item?.first()

        assertEquals("1462", item?.pblancId)
        assertEquals("아파트", item?.houseTyNm)
        // 없는 키는 DTO 기본값으로 흡수된다.
        assertEquals(0, item?.rentGtn)
        assertEquals(0, item?.mtRntchrg)
        assertEquals("", item?.suplyTyNm)
        assertEquals(0, item?.totHshldCo)
    }

    private companion object {
        const val NORMAL_RESPONSE = """
        {
          "response": {
            "header": { "resultCode": "00", "resultMsg": "NORMAL SERVICE" },
            "body": {
              "totalCount": "383",
              "numOfRows": "2",
              "pageNo": "1",
              "item": [
                {
                  "pblancId": "20955", "houseSn": 0, "sttusNm": "일반공고",
                  "pblancNm": "[울산권] 2026년 기존주택등 매입임대주택 입주자 모집 공고",
                  "suplyInsttNm": "LH", "houseTyNm": "다가구주택", "suplyTyNm": "매입임대",
                  "beforePblancId": "", "rcritPblancDe": "20260709", "przwnerPresnatnDe": "20261002",
                  "suplyHoCo": "", "refrnc": "LH 콜센터 : 1600-1004",
                  "url": "https://apply.lh.or.kr/x", "pcUrl": "https://www.myhome.go.kr/x",
                  "mobileUrl": "https://m.myhome.go.kr/x",
                  "hsmpNm": "", "brtcNm": "울산광역시", "signguNm": "중구",
                  "fullAdres": "", "rnCodeNm": "", "refrnLegaldongNm": "", "pnu": "", "heatMthdNm": "",
                  "totHshldCo": 0, "sumSuplyCo": 3, "rentGtn": 0, "enty": 0, "prtpay": 0,
                  "surlus": 0, "mtRntchrg": 0, "beginDe": "20260810", "endDe": "20260811"
                },
                {
                  "pblancId": "20955", "houseSn": 0, "sttusNm": "일반공고",
                  "pblancNm": "[울산권] 2026년 기존주택등 매입임대주택 입주자 모집 공고",
                  "suplyInsttNm": "LH", "houseTyNm": "다가구주택", "suplyTyNm": "매입임대",
                  "beforePblancId": "", "rcritPblancDe": "20260709", "przwnerPresnatnDe": "20261002",
                  "suplyHoCo": "", "refrnc": "LH 콜센터 : 1600-1004",
                  "url": "https://apply.lh.or.kr/x", "pcUrl": "https://www.myhome.go.kr/x",
                  "mobileUrl": "https://m.myhome.go.kr/x",
                  "hsmpNm": "", "brtcNm": "울산광역시", "signguNm": "남구",
                  "fullAdres": "", "rnCodeNm": "", "refrnLegaldongNm": "", "pnu": "", "heatMthdNm": "",
                  "totHshldCo": 0, "sumSuplyCo": 2, "rentGtn": 0, "enty": 0, "prtpay": 0,
                  "surlus": 0, "mtRntchrg": 0, "beginDe": "20260810", "endDe": "20260811"
                }
              ]
            }
          }
        }
        """

        const val SINGLE_ITEM_RESPONSE = """
        {
          "response": {
            "header": { "resultCode": "00", "resultMsg": "NORMAL SERVICE" },
            "body": {
              "totalCount": "1", "numOfRows": "20", "pageNo": "1",
              "item": [ { "pblancId": "1", "houseSn": 0, "pblancNm": "단건" } ]
            }
          }
        }
        """

        const val NO_DATA_RESPONSE = """
        {
          "response": {
            "header": { "resultCode": "03", "resultMsg": "NODATA_ERROR" }
          }
        }
        """

        const val OUT_OF_RANGE_RESPONSE = """
        {
          "response": {
            "header": { "resultCode": "00", "resultMsg": "NORMAL SERVICE" },
            "body": { "totalCount": "383", "numOfRows": "10", "pageNo": "9999", "item": [] }
          }
        }
        """

        /** 실제 응답에서 관측된 타입 혼재: 빈 문자열 / 따옴표 숫자 / 순수 숫자. */
        const val MIXED_NUMBER_TYPE_RESPONSE = """
        {
          "response": {
            "header": { "resultCode": "00", "resultMsg": "NORMAL SERVICE" },
            "body": {
              "totalCount": "3", "numOfRows": "20", "pageNo": "1",
              "item": [
                { "pblancId": "1", "pblancNm": "빈 문자열", "totHshldCo": "", "sumSuplyCo": "" },
                { "pblancId": "2", "pblancNm": "따옴표 숫자", "totHshldCo": "120",
                  "sumSuplyCo": "3", "rentGtn": "12000000" },
                { "pblancId": "3", "pblancNm": "순수 숫자", "totHshldCo": 37, "sumSuplyCo": 37 }
              ]
            }
          }
        }
        """

        /** 실측 분양 응답 형태 — 27개 키만 있고 임대 전용 5개가 없다. */
        const val SALE_RESPONSE = """
        {
          "response": {
            "header": { "resultCode": "00", "resultMsg": "NORMAL SERVICE" },
            "body": {
              "totalCount": "63", "numOfRows": "20", "pageNo": "1",
              "item": [
                {
                  "pblancId": "1462", "houseSn": 1, "sttusNm": "일반공고",
                  "pblancNm": "성남복정2 A1블록 신혼희망타운(공공분양) 입주자모집공고",
                  "suplyInsttNm": "LH", "houseTyNm": "아파트",
                  "beforePblancId": "", "rcritPblancDe": "20260810",
                  "przwnerPresnatnDe": "20260917", "refrnc": "LH 콜센터 : 1600-1004",
                  "url": "https://apply.lh.or.kr/x",
                  "pcUrl": "https://www.myhome.go.kr/hws/portal/sch/selectLttotHouseDetailView.do?pblancId=1462&houseSn=1",
                  "mobileUrl": "https://m.myhome.go.kr/hws/portal/sch/selectLttotHouseDetailView.do?pblancId=1462&houseSn=1",
                  "hsmpNm": "성남복정2 A1블록", "brtcNm": "경기도", "signguNm": "성남시 수정구",
                  "fullAdres": "경기도 성남시 수정구 신흥동 81-4 ", "rnCodeNm": "",
                  "refrnLegaldongNm": "신흥동", "pnu": "4113110100100810004",
                  "heatMthdNm": "개별난방", "sumSuplyCo": 594,
                  "enty": 43287000, "prtpay": 0, "surlus": 304680000,
                  "beginDe": "20260824", "endDe": "20260826"
                }
              ]
            }
          }
        }
        """

        const val UNKNOWN_FIELD_RESPONSE = """
        {
          "response": {
            "header": { "resultCode": "00", "resultMsg": "NORMAL SERVICE", "newHeaderField": 1 },
            "body": {
              "totalCount": "1", "numOfRows": "20", "pageNo": "1", "brandNewField": "x",
              "item": [ { "pblancId": "20955", "houseSn": 0, "someFutureField": true } ]
            }
          }
        }
        """
    }
}

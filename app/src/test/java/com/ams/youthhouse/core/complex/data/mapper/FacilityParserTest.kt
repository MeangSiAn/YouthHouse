package com.ams.youthhouse.core.complex.data.mapper

import com.ams.youthhouse.core.complex.domain.model.FacilityGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 입력은 전부 K-apt 실측값이다(2026-09-09 data.mosstis.com에서 받은 그대로). */
class FacilityParserTest {

    @Test
    fun `괄호 안 공백과 전화번호가 있어도 분류 단위로 끊는다`() {
        // 동탄역푸르지오. 공백으로 자르면 "[초등학교(치동초교] [031-374-2961)]"가 됐다.
        val groups = parseFacilityGroups(
            "초등학교(치동초교 031-374-2961) 중학교(치동중) 고등학교(치동고 2024.3월 개교예)",
        )

        assertEquals(
            listOf(
                FacilityGroup("초등학교", listOf("치동초교")),
                FacilityGroup("중학교", listOf("치동중")),
                FacilityGroup("고등학교", listOf("치동고 2024.3월 개교예")),
            ),
            groups,
        )
    }

    @Test
    fun `빈 괄호는 항목을 통째로 버린다`() {
        val groups = parseFacilityGroups(
            "관공서(동탄5동사무소 031-5189-4951) 병원(한림대 병원 1522-2500) 대형상가() 공원() 기타(동탄역 1800-1472)",
        )

        assertEquals(listOf("관공서", "병원", "기타"), groups.map { it.category })
        assertEquals(listOf("동탄5동사무소"), groups[0].names)
        assertEquals(listOf("한림대 병원"), groups[1].names)
        assertEquals(listOf("동탄역"), groups[2].names)
    }

    @Test
    fun `괄호 안 쉼표는 같은 분류의 여러 이름이다`() {
        // 봉천두산1,2단지. 이름 사이 공백이 들쭉날쭉하다.
        val groups = parseFacilityGroups(
            "초등학교(구암, 신봉, 은천초등학교) 대학교(서울대학교, 숭실대학교, 중앙대학교)",
        )

        assertEquals(listOf("구암", "신봉", "은천초등학교"), groups[0].names)
        assertEquals(listOf("서울대학교", "숭실대학교", "중앙대학교"), groups[1].names)
    }

    @Test
    fun `중첩 괄호 안의 전화번호도 지운다`() {
        // 각화힐스테이트
        val groups = parseFacilityGroups(
            "관공서(문화동주민쎈타 (062-410-8735)) 병원(광주병원(062-260-7000)) 대형상가(삼섬홈플러스)",
        )

        assertEquals(
            listOf(
                FacilityGroup("관공서", listOf("문화동주민쎈타")),
                FacilityGroup("병원", listOf("광주병원")),
                FacilityGroup("대형상가", listOf("삼섬홈플러스")),
            ),
            groups,
        )
    }

    @Test
    fun `이름 안의 공백은 유지한다`() {
        val groups = parseFacilityGroups("대형상가(LG하이프라자, 하이마트, 삼성전자 등) 대형상가(단지내 상가)")

        // 같은 분류가 두 번 오면 하나로 합친다.
        assertEquals(1, groups.size)
        assertEquals(listOf("LG하이프라자", "하이마트", "삼성전자 등", "단지내 상가"), groups[0].names)
    }

    @Test
    fun `빈 문자열과 빈 괄호만 있는 값은 빈 목록이다`() {
        assertTrue(parseFacilityGroups(null).isEmpty())
        assertTrue(parseFacilityGroups("").isEmpty())
        assertTrue(parseFacilityGroups("대형상가()").isEmpty())
    }

    @Test
    fun `단지 내 시설은 쉼표로 나누고 기타는 버린다`() {
        val items = parseWelfareFacilities("관리사무소, 노인정, 보육시설, 문고, 주민공동시설, 기타")

        assertEquals(listOf("관리사무소", "노인정", "보육시설", "문고", "주민공동시설"), items)
    }
}

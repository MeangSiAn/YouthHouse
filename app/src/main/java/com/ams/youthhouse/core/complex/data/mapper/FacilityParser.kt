package com.ams.youthhouse.core.complex.data.mapper

import com.ams.youthhouse.core.complex.domain.model.FacilityGroup

/**
 * K-apt 주변시설 문자열을 분류별 항목으로 쪼갠다.
 *
 * 원문은 "분류(내용)"이 공백으로 이어 붙은 한 줄이다:
 * `관공서(동탄5동사무소 031-5189-4951) 병원(한림대 병원 1522-2500) 대형상가() 공원()`
 *
 * 괄호 안에 공백·쉼표·전화번호·중첩 괄호가 다 들어오므로 공백으로 자르면
 * `[초등학교(치동초교] [031-374-2961)]`처럼 깨진다. 괄호 깊이를 세면서 **최상위에서만** 끊는다.
 *
 * - 빈 괄호(`공원()`)는 버린다. 분류명만 남는 건 정보가 아니라 잡음이다.
 * - 전화번호는 지운다. 여기서 전화를 걸 수 있는 것도 아니고 이름을 가릴 뿐이다.
 * - 괄호 안의 쉼표는 같은 분류의 여러 이름이다 (`초등학교(구암, 신봉, 은천초등학교)`).
 */
internal fun parseFacilityGroups(raw: String?): List<FacilityGroup> {
    val text = raw?.trim().orEmpty()
    if (text.isEmpty()) return emptyList()

    val merged = LinkedHashMap<String, MutableList<String>>()
    splitTopLevel(text)
        .mapNotNull { segment -> segment.toFacilityGroup() }
        .forEach { group ->
            merged.getOrPut(group.category) { mutableListOf() }
                .addAll(group.names.filter { it !in merged[group.category].orEmpty() })
        }
    return merged.map { (category, names) -> FacilityGroup(category = category, names = names) }
}

/** 단지 내 시설은 분류 없이 쉼표로만 온다: `관리사무소, 노인정, 기타`. */
internal fun parseWelfareFacilities(raw: String?): List<String> =
    raw.orEmpty()
        .split(',')
        .map { it.trim() }
        // "기타"는 무엇이 있다는 말이 아니다. 칩 한 칸을 차지할 정보가 아니다.
        .filter { it.isNotEmpty() && it != OTHER }
        .distinct()

/** 괄호 밖의 공백에서만 자른다. 괄호가 닫히지 않은 채 끝나도 마지막 조각은 살린다. */
private fun splitTopLevel(text: String): List<String> {
    val segments = mutableListOf<String>()
    val current = StringBuilder()
    var depth = 0
    for (ch in text) {
        when {
            ch == '(' -> {
                depth++
                current.append(ch)
            }

            ch == ')' -> {
                depth = (depth - 1).coerceAtLeast(0)
                current.append(ch)
            }

            ch.isWhitespace() && depth == 0 -> {
                if (current.isNotBlank()) segments += current.toString()
                current.clear()
            }

            else -> current.append(ch)
        }
    }
    if (current.isNotBlank()) segments += current.toString()
    return segments
}

private fun String.toFacilityGroup(): FacilityGroup? {
    val open = indexOf('(')
    val category = (if (open < 0) this else substring(0, open)).trim()
    if (category.isEmpty()) return null

    // 첫 여는 괄호 뒤부터 끝까지. 바깥 닫는 괄호 하나만 벗겨 중첩 괄호는 내용에 남긴다.
    val content = if (open < 0) "" else substring(open + 1).trim().removeSuffix(")")
    val names = content.toNames()
    return if (names.isEmpty()) null else FacilityGroup(category = category, names = names)
}

private fun String.toNames(): List<String> =
    replace(PHONE_NUMBER, " ")
        .replace(EMPTY_PARENS, " ")
        .split(NAME_SEPARATOR)
        .map { it.replace(WHITESPACE, " ").trim() }
        .filter { it.isNotEmpty() }

/** `031-5189-4951`, `1522-2500`, 그리고 그것을 감싼 괄호까지. */
private val PHONE_NUMBER = Regex("""\(?\s*(?:\d{2,4}-\d{3,4}-\d{4}|\d{4}-\d{4})\s*\)?""")
private val EMPTY_PARENS = Regex("""\(\s*\)""")
private val NAME_SEPARATOR = Regex("""[,，/]""")
private val WHITESPACE = Regex("""\s+""")
private const val OTHER = "기타"

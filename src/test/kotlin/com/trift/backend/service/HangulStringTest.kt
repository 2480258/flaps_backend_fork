package com.trift.backend.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

class HangulStringTest {
    @Test
    fun `한글로만 이루어진 문자열에서 초성 추출`() {
        val hangulString = HangulString("각난닫랄맘밥사")
        assertEquals("ㄱㄴㄷㄹㅁㅂㅅ", hangulString.flatAsInitialWithoutSpace())
    }

    @Test
    fun `한글과 영어로 이루어진 문자열에서 초성 추출`() {
        val hangulString = HangulString("abcd맘밥사")
        assertEquals("abcdㅁㅂㅅ", hangulString.flatAsInitialWithoutSpace())
    }

    @Test
    fun `중성만 있는 문자열에서 초성 추출`() {
        val hangulString = HangulString("aaaㅏ")
        assertEquals(null, hangulString.flatAsInitialWithoutSpace())
    }

    @Test
    fun `초성 추출 시 문자열에서 공백 제거`() {
        val hangulString = HangulString("가나다라 마")
        assertEquals("ㄱㄴㄷㄹㅁ", hangulString.flatAsInitialWithoutSpace())
    }

    @Test
    fun `한글 분리`() {
        val hangulString = HangulString("가나다라ㅁ")
        assertEquals("ㄱㅏㄴㅏㄷㅏㄹㅏㅁ", hangulString.explodeWithoutSpace())
    }

    @Test
    fun `종성이 있는 한글 분리`() {
        val hangulString = HangulString("가나다람")
        assertEquals("ㄱㅏㄴㅏㄷㅏㄹㅏㅁ", hangulString.explodeWithoutSpace())
    }

    @Test
    fun `된소리 한글 분리`() {
        val hangulString = HangulString("뽣뛟")
        assertEquals("ㅃㅗㅏㄷㄸㅜㅔㄹㅂ", hangulString.explodeWithoutSpace())
    }

    @Test
    fun `영어가 섞인 한글 분리`() {
        val hangulString = HangulString("뽣aa뛟")
        assertEquals("ㅃㅗㅏㄷaaㄸㅜㅔㄹㅂ", hangulString.explodeWithoutSpace())
    }

    @Test
    fun `공백이 섞인 한글 분리`() {
        val hangulString = HangulString("뽣a a뛟")
        assertEquals("ㅃㅗㅏㄷaaㄸㅜㅔㄹㅂ", hangulString.explodeWithoutSpace())
    }
}
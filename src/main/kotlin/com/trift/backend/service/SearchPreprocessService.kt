package com.trift.backend.service

import org.springframework.stereotype.Component



data class HangulString(val original: String) {

    private val originalWithoutSpace = original.split(' ').fold("") { x, y -> x + y }

    data class Hangul constructor(val code: Char)
    {
        companion object {
            private val hangulStartIndex = 0xAC00
            private val hangulEndIndex = 0xD7A4


            val orderOfChosung = listOf('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')
            val orderOfJungSung = listOf('ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ')
            val orderOfJongSung = listOf(null, 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')

            val MIXED = mapOf(
                'ㄲ' to listOf('ㄱ', 'ㄱ'),
                'ㄳ' to listOf('ㄱ', 'ㅅ'),
                'ㄵ' to listOf('ㄴ', 'ㅈ'),
                'ㄶ' to listOf('ㄴ', 'ㅎ'),
                'ㄺ' to listOf('ㄹ', 'ㄱ'),
                'ㄻ' to listOf('ㄹ', 'ㅁ'),
                'ㄼ' to listOf('ㄹ', 'ㅂ'),
                'ㄽ' to listOf('ㄹ', 'ㅅ'),
                'ㄾ' to listOf('ㄹ', 'ㅌ'),
                'ㄿ' to listOf('ㄹ', 'ㅍ'),
                'ㅀ' to listOf('ㄹ', 'ㅎ'),
                'ㅄ' to listOf('ㅂ', 'ㅅ'),
                'ㅆ' to listOf('ㅅ', 'ㅅ'),
                'ㅘ' to listOf('ㅗ', 'ㅏ'),
                'ㅙ' to listOf('ㅗ', 'ㅐ'),
                'ㅚ' to listOf('ㅗ', 'ㅣ'),
                'ㅝ' to listOf('ㅜ', 'ㅓ'),
                'ㅞ' to listOf('ㅜ', 'ㅔ'),
                'ㅟ' to listOf('ㅜ', 'ㅣ'),
                'ㅢ' to listOf('ㅡ', 'ㅣ'),
            )

            fun checkIfHangul(code: Char): Boolean {
                return extract(code) != null
            }

            private fun extract(code: Char): Triple<Char?, Char?, Char?>? {
                var chosung: Char?
                var jungsung: Char?
                var jongsung: Char?


                if(orderOfChosung.contains(code)) {
                    chosung = code
                    jungsung = null
                    jongsung = null
                } else if(orderOfJungSung.contains(code)) {
                    chosung = null
                    jungsung = code
                    jongsung = null
                } else if(orderOfJongSung.contains(code)) {
                    chosung = null
                    jungsung = null
                    jongsung = code
                } else { // 중성과 종성 추가
                    if((code.code < hangulStartIndex) or (code.code > hangulEndIndex)) {
                        return null
                    }

                    chosung = orderOfChosung[(code.code - hangulStartIndex) / (orderOfJungSung.size * orderOfJongSung.size)]
                    jungsung = orderOfJungSung[(code.code - hangulStartIndex) / orderOfJongSung.size % orderOfJungSung.size]
                    jongsung = orderOfJongSung[(code.code - hangulStartIndex) % orderOfJongSung.size]
                }

                return Triple(chosung, jungsung, jongsung)
            }
        }

        private fun explodeMixed(char: Char?) : String? {
            if(char != null)
                return MIXED.getOrDefault(char, listOf(char)).fold("") {x, y -> x + y}
            else
                return null
        }

        val chosung: Char?

        val chosungAsExplode: String?
            get() = explodeMixed(chosung)

        val jungsung: Char?
        val jungsungAsExplode: String?
            get() = explodeMixed(jungsung)
        val jongsung: Char?

        val jongsungAsExplode: String?
            get() = explodeMixed(jongsung)

        init {
            val result = extract(code)

            chosung = result?.first
            jungsung = result?.second
            jongsung = result?.third
        }

        fun explodeJongsungIfMixed() {

        }
    }

    private data class HChar (val code: Char) {

        val hangul: Hangul?

        val etc: Char?

        init {
            if(Hangul.checkIfHangul(code)) {
                hangul = Hangul(code)
                etc = null
            } else {
                hangul = null
                etc = code
            }
        }
    }

    private val hangulListWithoutSpace: List<HChar> = originalWithoutSpace.map {
        HChar(it)
    }

    fun flatAsInitialWithoutSpace(): String? {
        val ret = hangulListWithoutSpace.map {
            if(it.hangul != null) it.hangul.chosung else it.etc
        }

        if(ret.contains(null)) {
            return null
        }

        return ret.fold("") { x, y -> x + y }
    }

    fun explodeWithoutSpace() : String {
        val ret = hangulListWithoutSpace.map {
            if(it.hangul != null) {
                val chosung = it.hangul.chosungAsExplode ?: ""
                val jungsung = it.hangul.jungsungAsExplode ?: ""
                val jongsung = it.hangul.jongsungAsExplode ?: ""

                return@map "$chosung$jungsung$jongsung"
            } else {
                return@map it.etc.toString()
            }
        }.fold("") { x, y -> x + y }

        return ret
    }
}

interface SearchPreprocessService {
    fun getHString(string: String): HangulString
}

@Component
class SearchPreprocessServiceImpl : SearchPreprocessService {
    override fun getHString(string: String): HangulString {
        return HangulString(string)
    }
}

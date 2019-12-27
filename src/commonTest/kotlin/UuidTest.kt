package com.benasher44.uuid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class UuidTest {

    private val uuidChars = HashSet(Uuid.uuidChars)

    private fun isValidUuidChar(char: Char) = uuidChars.contains(char)

    @Test
    fun generates_a_UUID() {
        val uuid = uuid4()
        val uuidString = uuid.toString()
        assertEquals(uuidString.length, UUID_STRING_LENGTH)
        assertNull(Uuid.hyphenIndices.find { uuidString[it] != '-' })
        for (range in Uuid.uuidCharRanges) {
            assertNull(range.find { !isValidUuidChar(uuidString[it]) })
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun deprecated_parses_a_UUID_from_a_string() {
        val uuid = uuid4()
        val uuidFromStr = Uuid.parse(uuid.toString())
        assertEquals(uuid, uuidFromStr)
        // double check hashcode equality, while we're here
        assertEquals(uuid.hashCode(), uuidFromStr.hashCode())
    }

    @Test
    fun parses_a_UUID_from_a_string() {
        val uuid = uuid4()
        val uuidFromStr = Uuid.fromString(uuid.toString())
        assertEquals(uuid, uuidFromStr)
        // double check hashcode equality, while we're here
        assertEquals(uuid.hashCode(), uuidFromStr.hashCode())
    }

    @Test
    fun throws_when_passed_invalid_number_of_bytes() {
        assertFails { Uuid(ByteArray(17)) }
        assertFails { Uuid(ByteArray(15)) }
    }

    @Test
    fun parsing_throws_when_passed_invalid_length_of_string() {
        assertFailsWith<IllegalArgumentException>(
            message = "Uuid string has invalid length: c480d6ab-cb0c-427b-a9a6",
            block = { Uuid.fromString("c480d6ab-cb0c-427b-a9a6") }
        )
    }

    @Test
    fun parsing_throws_when_passed_invalid_format_of_string() {
        assertFailsWith<IllegalArgumentException>(
            message = "Uuid string has invalid format: c480d6abcb0c427ba9a619c5f8a146bd",
            block = { Uuid.fromString("c480d6abcb0c427ba9a619c5f8a146bd") }
        )
    }

    @Test
    fun parsing_throws_when_passed_invalid_characters_in_string() {
        assertFailsWith<IllegalArgumentException>(
            message = "Uuid string has invalid characters ghijklmn-opqr-stuv-wxyz-GHIJKLMNOPQR",
            block = { Uuid.fromString("ghijklmn-opqr-stuv-wxyz-GHIJKLMNOPQR") }
        )
    }

    @Test
    @Suppress("DEPRECATION")
    fun deprecated_parse_provides_higher_and_lower_bits() {
        val uuid = Uuid.parse("c480d6ab-cb0c-427b-a9a6-19c5f8a146bd")!!
        assertEquals(-6222257497095190851, uuid.leastSignificantBits)
        assertEquals(-4287190811922382213, uuid.mostSignificantBits)
    }

    @Test
    fun provides_higher_and_lower_bits() {
        val uuid = Uuid.fromString("c480d6ab-cb0c-427b-a9a6-19c5f8a146bd")
        assertEquals(-6222257497095190851, uuid.leastSignificantBits)
        assertEquals(-4287190811922382213, uuid.mostSignificantBits)
    }

    @Test
    fun uuid4_generation() {
        val uuid = uuid4()

        assertEquals(4, uuid.version)
        assertEquals(2, uuid.variant)
    }

    @Test
    fun uuid_construction_from_msb_and_lsb() {
        assertEquals("00000000-0000-0000-0000-000000000000", Uuid(0, 0).toString(), "min")
        assertEquals("00000000-0000-0000-ffff-ffffffffffff", Uuid(0, -1).toString(), "lsb")
        assertEquals("ffffffff-ffff-ffff-0000-000000000000", Uuid(-1, 0).toString(), "msb")
        assertEquals("ffffffff-ffff-ffff-ffff-ffffffffffff", Uuid(-1, -1).toString(), "max")
    }

    @Test
    @Suppress("DEPRECATION")
    fun parse_variants_from_deprecated_parse() {
        assertEquals(0, Uuid.parse("00000000-0000-0000-0000-000000000000")!!.variant, "Nil or NCS")
        assertEquals(2, Uuid.parse("00000000-0000-0000-8000-000000000000")!!.variant, "RFC 4122")
        assertEquals(6, Uuid.parse("00000000-0000-0000-c000-000000000000")!!.variant, "Microsoft")
        assertEquals(7, Uuid.parse("00000000-0000-0000-e000-000000000000")!!.variant, "Future")
    }

    @Test
    fun variants() {
        assertEquals(0, Uuid.fromString("00000000-0000-0000-0000-000000000000").variant, "Nil or NCS")
        assertEquals(2, Uuid.fromString("00000000-0000-0000-8000-000000000000").variant, "RFC 4122")
        assertEquals(6, Uuid.fromString("00000000-0000-0000-c000-000000000000").variant, "Microsoft")
        assertEquals(7, Uuid.fromString("00000000-0000-0000-e000-000000000000").variant, "Future")
    }

    @Test
    @Suppress("DEPRECATION")
    fun versions_from_deprecated_parse() {
        assertEquals(0, Uuid.parse("00000000-0000-0000-0000-000000000000")!!.version, "Nil")
        assertEquals(1, Uuid.parse("00000000-0000-1000-0000-000000000000")!!.version, "time-based")
        assertEquals(2, Uuid.parse("00000000-0000-2000-0000-000000000000")!!.version, "DCE security")
        assertEquals(3, Uuid.parse("00000000-0000-3000-0000-000000000000")!!.version, "name-based using MD5 hashing")
        assertEquals(4, Uuid.parse("00000000-0000-4000-0000-000000000000")!!.version, "random or pseudo-random")
        assertEquals(5, Uuid.parse("00000000-0000-5000-0000-000000000000")!!.version, "name-based using SHA-1 hashing")

        assertEquals(6, Uuid.parse("00000000-0000-6000-0000-000000000000")!!.version, "future #1")
        assertEquals(7, Uuid.parse("00000000-0000-7000-0000-000000000000")!!.version, "future #2")
        assertEquals(8, Uuid.parse("00000000-0000-8000-0000-000000000000")!!.version, "future #3")
        assertEquals(9, Uuid.parse("00000000-0000-9000-0000-000000000000")!!.version, "future #4")
        assertEquals(10, Uuid.parse("00000000-0000-a000-0000-000000000000")!!.version, "future #5")
        assertEquals(11, Uuid.parse("00000000-0000-b000-0000-000000000000")!!.version, "future #6")
        assertEquals(12, Uuid.parse("00000000-0000-c000-0000-000000000000")!!.version, "future #7")
        assertEquals(13, Uuid.parse("00000000-0000-d000-0000-000000000000")!!.version, "future #8")
        assertEquals(14, Uuid.parse("00000000-0000-e000-0000-000000000000")!!.version, "future #9")
        assertEquals(15, Uuid.parse("00000000-0000-f000-0000-000000000000")!!.version, "future #10")
    }

    @Test
    fun versions() {
        assertEquals(0, Uuid.fromString("00000000-0000-0000-0000-000000000000").version, "Nil")
        assertEquals(1, Uuid.fromString("00000000-0000-1000-0000-000000000000").version, "time-based")
        assertEquals(2, Uuid.fromString("00000000-0000-2000-0000-000000000000").version, "DCE security")
        assertEquals(3, Uuid.fromString("00000000-0000-3000-0000-000000000000").version, "name-based using MD5 hashing")
        assertEquals(4, Uuid.fromString("00000000-0000-4000-0000-000000000000").version, "random or pseudo-random")
        assertEquals(5, Uuid.fromString("00000000-0000-5000-0000-000000000000").version, "name-based using SHA-1 hashing")

        assertEquals(6, Uuid.fromString("00000000-0000-6000-0000-000000000000").version, "future #1")
        assertEquals(7, Uuid.fromString("00000000-0000-7000-0000-000000000000").version, "future #2")
        assertEquals(8, Uuid.fromString("00000000-0000-8000-0000-000000000000").version, "future #3")
        assertEquals(9, Uuid.fromString("00000000-0000-9000-0000-000000000000").version, "future #4")
        assertEquals(10, Uuid.fromString("00000000-0000-a000-0000-000000000000").version, "future #5")
        assertEquals(11, Uuid.fromString("00000000-0000-b000-0000-000000000000").version, "future #6")
        assertEquals(12, Uuid.fromString("00000000-0000-c000-0000-000000000000").version, "future #7")
        assertEquals(13, Uuid.fromString("00000000-0000-d000-0000-000000000000").version, "future #8")
        assertEquals(14, Uuid.fromString("00000000-0000-e000-0000-000000000000").version, "future #9")
        assertEquals(15, Uuid.fromString("00000000-0000-f000-0000-000000000000").version, "future #10")
    }
}
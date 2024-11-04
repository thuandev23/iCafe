package com.pro.shopfee.zalopay.Helper.HMac
object HexStringUtil {
    private val HEX_CHAR_TABLE = byteArrayOf(
        '0'.toInt().toByte(), '1'.toInt().toByte(), '2'.toInt().toByte(), '3'.toInt().toByte(),
        '4'.toInt().toByte(), '5'.toInt().toByte(), '6'.toInt().toByte(), '7'.toInt().toByte(),
        '8'.toInt().toByte(), '9'.toInt().toByte(), 'a'.toInt().toByte(), 'b'.toInt().toByte(),
        'c'.toInt().toByte(), 'd'.toInt().toByte(), 'e'.toInt().toByte(), 'f'.toInt().toByte()
    )

    // @formatter:on
    /**
     * Convert a byte array to a hexadecimal string
     *
     * @param raw
     * A raw byte array
     *
     * @return Hexadecimal string
     */
    fun byteArrayToHexString(raw: ByteArray): String {
        val hex = ByteArray(2 * raw.size)
        var index = 0

        for (b in raw) {
            val v = b.toInt() and 0xFF
            hex[index++] = HEX_CHAR_TABLE[v ushr 4]
            hex[index++] = HEX_CHAR_TABLE[v and 0xF]
        }
        return String(hex)
    }

    /**
     * Convert a hexadecimal string to a byte array
     *
     * @param hex
     * A hexadecimal string
     *
     * @return The byte array
     */
    fun hexStringToByteArray(hex: String): ByteArray {
        val hexstandard = hex.toLowerCase()
        val sz = hexstandard.length / 2
        val bytesResult = ByteArray(sz)

        var idx = 0
        for (i in 0 until sz) {
            bytesResult[i] = hexstandard[idx].toInt().toByte()
            ++idx
            var tmp = hexstandard[idx].toInt().toByte()
            ++idx

            if (bytesResult[i] > HEX_CHAR_TABLE[9]) {
                bytesResult[i] = (bytesResult[i] - ('a'.toInt().toByte() - 10)).toByte()
            } else {
                bytesResult[i] = (bytesResult[i] - '0'.toInt().toByte()).toByte()
            }
            tmp = if (tmp > HEX_CHAR_TABLE[9]) {
                (tmp - ('a'.toInt().toByte() - 10)).toByte()
            } else {
                (tmp - '0'.toInt().toByte()).toByte()
            }

            bytesResult[i] = (bytesResult[i] * 16 + tmp).toByte()
        }
        return bytesResult
    }
}
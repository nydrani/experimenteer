package xyz.velvetmilk.testingtool.models

data class CipherResult(val iv: ByteArray, val result: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as CipherResult
        if (!iv.contentEquals(other.iv)) return false
        if (!result.contentEquals(other.result)) return false

        return true
    }

    override fun hashCode(): Int {
        return (iv + result).contentHashCode()
    }
}

package com.tim.vpnprotocols.extension

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.parcelableCreator

const val LEFT_BRACKET_CHAR_CODE: Int = 91
const val RIGHT_BRACKET_CHAR_CODE: Int = 93
/**
 * Convert [Parcelable] to [String] with [ByteArray]
 *
 * @return [String] like "[1, 99, 24, 0, 12]"
 */
fun Parcelable.convertToString(): String {
    val originalParcel = Parcel.obtain()
    this.writeToParcel(originalParcel, 0)
    val byteArray = originalParcel.marshall()
    originalParcel.recycle()
    return byteArray.contentToString()
}

/**
 * Convert encoded [String] with [ByteArray] to [Parcelable]
 * filter '[' and ']'
 *
 * @return [Parcelable] object as [T]
 */
inline fun <reified T : Parcelable> String.convertToObject(): T {
    val filtered = this.dropWhile { it == Char(LEFT_BRACKET_CHAR_CODE) || it == Char(RIGHT_BRACKET_CHAR_CODE) }
    val bytes = filtered.split(",")
    val result = ByteArray(bytes.size) {
        bytes[it].trim().toByteOrNull() ?: -1
    }
    val resultParcel = Parcel.obtain()
    resultParcel.unmarshall(result, 0, result.size)
    resultParcel.setDataPosition(0)
    val userDtoFromParcel = parcelableCreator<T>().createFromParcel(resultParcel)
    resultParcel.recycle()
    return userDtoFromParcel
}

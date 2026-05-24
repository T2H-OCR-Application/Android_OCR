package com.t2h.ocr.domain.ocr

import org.opencv.core.Mat

/**
 * Extension function to ensure that [Mat] and its subclasses are released
 * after use, similar to [AutoCloseable.use].
 */
inline fun <T : Mat, R> T.use(block: (T) -> R): R {
    try {
        return block(this)
    } finally {
        this.release()
    }
}

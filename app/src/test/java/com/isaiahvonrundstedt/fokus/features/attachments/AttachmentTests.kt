package com.isaiahvonrundstedt.fokus.features.attachments

import org.junit.Test
import org.junit.Assert.*

class AttachmentTests {

    @Test
    fun fileType_isImage() {
        val type = "/storage/emulated/0/Android/data/com.isaiahvonrundstedt.fokus/files/image.jpg"

        assertTrue(type, Attachment.isImage(type))
    }

}
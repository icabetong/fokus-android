package com.isaiahvonrundstedt.fokus.features.attachments

import org.junit.Test
import org.junit.Assert.*

class AttachmentTests {

    private val path = "/storage/emulated/0/Android/data/com.isaiahvonrundstedt.fokus/files/image.jpg"

    @Test
    fun `Get attachment file type and check if it's an image`() {
        assertTrue(Attachment.isImage(path))
    }

}
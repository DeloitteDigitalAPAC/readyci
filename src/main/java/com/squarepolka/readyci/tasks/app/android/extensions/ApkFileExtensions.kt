package com.squarepolka.readyci.tasks.app.android.extensions

import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkSignStatus
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

val ApkFile.isSigned: Boolean
    get() = verifyApk() == ApkSignStatus.signed

val ApkFile.isDebuggable: Boolean
    get() {
        val inputSource = InputSource(StringReader(manifestXml))
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource)
        val debuggable = (doc.getElementsByTagName("application").item(0) as Element).getAttribute("android:debuggable")

        return debuggable == null || !debuggable.toBoolean()
    }
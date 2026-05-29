package com.koner.typst

import android.content.res.Resources
import com.rk.file.FileType
import com.rk.icons.Icon

class TypstLanguage(resources: Resources) : FileType {
    override val extensions = listOf("typ")
    override val textmateScope = "source.typst"
    override val name = "typst"
    override val title = "Typst"
    override val icon = Icon.ExternalResourceIcon(R.drawable.typst, resources)
}
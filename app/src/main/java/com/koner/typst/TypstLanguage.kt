package com.koner.typst

import com.rk.file.FileType
import com.rk.icons.Icon

class TypstLanguage(override val icon: Icon) : FileType {
    override val extensions = listOf("typ")
    override val textmateScope = "source.typst"
    override val name = "typst"
    override val title = "Typst"
}
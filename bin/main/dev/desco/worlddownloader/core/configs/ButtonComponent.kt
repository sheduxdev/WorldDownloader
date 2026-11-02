package dev.desco.worlddownloader.core.configs

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color

class ButtonComponent(name: String, private val action: () -> Unit): UIContainer() {
    private var hovered = false

    private val background = UIBlock(Color(0, 0, 0, 150)).constrain {
        this.x = 0.pixels
        this.y = 0.pixels
        this.width = FillConstraint()
        this.height = 16.pixels
    } effect OutlineEffect(Color.WHITE, 1f, drawInsideChildren = true) childOf this

    private val text = UIText(name, shadow = false).constrain {
        this.x = CenterConstraint()
        this.y = CenterConstraint()
        this.textScale = 1.0.pixels
    } childOf background

    init {
        onMouseEnter {
            hovered = true
            text.setText(name.uppercase())
        }
        onMouseLeave {
            hovered = false
            text.setText(name)
        }
        onLeftClick {
            action()
        }
    }
}

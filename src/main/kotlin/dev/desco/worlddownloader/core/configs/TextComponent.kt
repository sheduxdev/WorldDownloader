package dev.desco.worlddownloader.core.configs

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.UKeyboard
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color
import kotlin.reflect.KProperty

class TextComponent(
    name: String,
    default: String,
    placeholder: String = "",
    private val onChange: (String) -> Unit = {},
): UIContainer() {

    private var value = default
    private var ready = false

    private val label = UIText(name, shadow = false).constrain {
        this.x = 0.pixels
        this.y = 0.pixels
        this.textScale = 1.1.pixels
    } childOf this

    private val box = UIBlock(Color(0, 0, 0, 130)).constrain {
        this.x = 0.pixels
        this.y = SiblingConstraint(4f)
        this.width = FillConstraint()
        this.height = 16.pixels
    } effect OutlineEffect(Color.WHITE, 1f, drawInsideChildren = true) childOf this

    private val input = UITextInput(placeholder, shadow = false).constrain {
        this.x = 4.pixels
        this.y = CenterConstraint()
        this.width = FillConstraint() - 8.pixels
        this.height = 10.pixels
        this.textScale = 1.0.pixels
    }.apply {
        onLeftClick { event ->
            event.stopPropagation()
            grabWindowFocus()
        }
        onUpdate {
            value = it
            onChange(it)
        }
        onFocusLost {
            value = (this as UITextInput).getText()
            onChange(value)
        }
        onKeyType { _, keyCode ->
            if (keyCode == UKeyboard.KEY_ENTER || keyCode == UKeyboard.KEY_ESCAPE) {
                value = (this as UITextInput).getText()
                onChange(value)
                releaseWindowFocus()
                setActive(false)
            }
        }
    } childOf box

    init {
        if (default.isNotEmpty()) {
            runCatching { input.setText(default) }
        }
        ready = true
    }

    operator fun getValue(thisRef: Configuration, prop: KProperty<*>) = value
    operator fun setValue(thisRef: Configuration, prop: KProperty<*>, value: String) {
        this.value = value
        if (ready) {
            runCatching { input.setText(value) }
        }
        onChange(value)
    }
}

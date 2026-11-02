package dev.desco.worlddownloader.core.configs

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color
import kotlin.reflect.KProperty

class PresetSelectorComponent(
    name: String,
    default: String,
    private val presets: () -> List<String>,
    private val onChange: (String) -> Unit = {},
): UIContainer() {
    private var items = presets().ifEmpty { listOf(default) }
    private var value = default.takeIf { it.isNotBlank() } ?: items.first()

    private val label = UIText(name, shadow = false).constrain {
        this.x = 0.pixels
        this.y = 0.pixels
        this.textScale = 1.1.pixels
    } childOf this

    private val row = UIContainer().constrain {
        this.x = 0.pixels
        this.y = SiblingConstraint(4f)
        this.width = FillConstraint()
        this.height = 18.pixels
    } childOf this

    private val previous = smallButton("<") { move(-1) } childOf row
    private val selected = UIBlock(Color(0, 0, 0, 130)).constrain {
        this.x = SiblingConstraint(4f)
        this.y = 0.pixels
        this.width = FillConstraint() - 84.pixels
        this.height = 18.pixels
    } effect OutlineEffect(Color.WHITE, 1f, drawInsideChildren = true) childOf row
    private val selectedText = UIText(value, shadow = false).constrain {
        this.x = CenterConstraint()
        this.y = 5.pixels
        this.textScale = 1.0.pixels
    } childOf selected
    private val next = smallButton(">") { move(1) }.constrain {
        this.x = SiblingConstraint(4f)
    } childOf row
    private val refresh = smallButton("R") {
        refresh()
    }.constrain {
        this.x = SiblingConstraint(4f)
    } childOf row

    operator fun getValue(thisRef: Configuration, prop: KProperty<*>) = value
    operator fun setValue(thisRef: Configuration, prop: KProperty<*>, value: String) {
        this.value = value
        selectedText.setText(value)
        onChange(value)
    }

    fun refresh() {
        items = presets().ifEmpty { listOf(value.ifBlank { "default" }) }
        if (value !in items) {
            value = items.first()
            onChange(value)
        }
        selectedText.setText(value)
    }

    private fun move(delta: Int) {
        refresh()
        val index = items.indexOf(value).takeIf { it >= 0 } ?: 0
        value = items[(index + delta + items.size) % items.size]
        selectedText.setText(value)
        onChange(value)
    }

    private fun smallButton(text: String, action: () -> Unit) = (UIBlock(Color(0, 0, 0, 150)).constrain {
        this.x = 0.pixels
        this.y = 0.pixels
        this.width = 24.pixels
        this.height = 18.pixels
    } effect OutlineEffect(Color.WHITE, 1f, drawInsideChildren = true)).apply {
        UIText(text, shadow = false).constrain {
            this.x = CenterConstraint()
            this.y = 5.pixels
            this.textScale = 1.0.pixels
        } childOf this
        onLeftClick {
            action()
        }
    }
}

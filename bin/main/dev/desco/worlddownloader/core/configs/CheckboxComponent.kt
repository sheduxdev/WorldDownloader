package dev.desco.worlddownloader.core.configs

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.state.state
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color
import kotlin.reflect.KProperty

class CheckboxComponent(name: String, default: Boolean): UIContainer() {

    private val scale = 1.25

    private var value by state(default)

    private val track = UIBlock(if (default) Color(75, 150, 105) else Color(45, 45, 45)).constrain {
        this.x = 0.pixels
        this.y = 0.pixels
        this.width = (28 * scale).pixels
        this.height = (13 * scale).pixels
    } effect OutlineEffect(Color.WHITE, 1f, drawInsideChildren = true) childOf this

    private val knob = UIBlock(Color.WHITE).constrain {
        this.x = (if (default) 16 else 2).pixels
        this.y = 2.pixels
        this.width = (9 * scale).pixels
        this.height = (9 * scale).pixels
    } childOf track

    private val name = UIText(name, shadow = false).constrain {
        this.x = SiblingConstraint(6f)
        this.y = 2.pixels
        this.textScale = scale.pixels
    } childOf this

    operator fun getValue(thisRef: Configuration, prop: KProperty<*>) = value
    operator fun setValue(thisRef: Configuration, prop: KProperty<*>, value: Boolean) {
        this.value = value
    }

    init {
        constrain {
            this.width = FillConstraint()
            this.height = (14 * scale).pixels
        }
        onLeftClick {
            value = !value
            animateSwitch()
        }
    }

    private fun animateSwitch() {
        knob.animate {
            setXAnimation(Animations.OUT_SIN, 0.12f, (if (value) 16 else 2).pixels)
        }
        track.animate {
            setColorAnimation(
                Animations.OUT_SIN,
                0.12f,
                (if (value) Color(75, 150, 105) else Color(45, 45, 45)).toConstraint()
            )
        }
    }
}

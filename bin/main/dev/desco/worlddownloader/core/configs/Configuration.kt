package dev.desco.worlddownloader.core.configs

import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import java.awt.Color

abstract class Configuration: UIContainer() {

    private val actualContainer = ScrollComponent().constrain {
        this.x = 0.pixels
        this.y = 0.pixels
        this.width = 100.percent
        this.height = 100.percent
    } childOf this

    init {
        constrain {
            this.x = 0.pixels
            this.y = 0.pixels
            this.width = 100.percent
            this.height = 100.percent
        }
    }

    fun Configuration.setting(name: String, default: Boolean) = CheckboxComponent(name, default).constrain {
        this.x = 5.percent
        this.y = SiblingConstraint(5f).coerceAtLeast(3.percent)
        this.width = 90.percent
        this.height = 18.pixels
    } childOf actualContainer

    fun Configuration.textSetting(
        name: String,
        default: String = "",
        placeholder: String = "",
        onChange: (String) -> Unit = {},
    ) = TextComponent(name, default, placeholder, onChange).constrain {
            this.x = 5.percent
            this.y = SiblingConstraint(8f).coerceAtLeast(3.percent)
            this.width = 90.percent
            this.height = 32.pixels
        } childOf actualContainer

    fun Configuration.actionButton(name: String, action: () -> Unit) = ButtonComponent(name, action).constrain {
        this.x = 5.percent
        this.y = SiblingConstraint(6f).coerceAtLeast(3.percent)
        this.width = 42.percent
        this.height = 16.pixels
    } childOf actualContainer

    fun Configuration.presetSelector(
        name: String,
        default: String,
        presets: () -> List<String>,
        onChange: (String) -> Unit = {},
    ) = PresetSelectorComponent(name, default, presets, onChange).constrain {
        this.x = 5.percent
        this.y = SiblingConstraint(8f).coerceAtLeast(3.percent)
        this.width = 90.percent
        this.height = 34.pixels
    } childOf actualContainer

    fun Configuration.category(name: String) = UIContainer().constrain {
        this.x = 3.percent
        this.y = SiblingConstraint(10f).coerceAtLeast(3.percent)
        this.width = ChildBasedSizeConstraint()
        this.height = 10.pixels
    }.apply {
        val text = UIText(name).constrain {
            this.x = CenterConstraint()
            this.y = CenterConstraint()
        } childOf this

        UIBlock(Color.WHITE).constrain {
            this.x = SiblingConstraint(3f, alignOpposite = true)
            this.y = CenterConstraint()
            this.width = 10.pixels
            this.height = 1.pixels
        } childOf this

        UIBlock(Color.WHITE).constrain {
            this.x = SiblingConstraint(3f).boundTo(text)
            this.y = CenterConstraint()
            this.width = 10.pixels
            this.height = 1.pixels
        } childOf this
    } childOf actualContainer
}

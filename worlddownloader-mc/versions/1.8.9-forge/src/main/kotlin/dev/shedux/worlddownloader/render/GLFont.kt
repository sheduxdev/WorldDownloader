package dev.shedux.worlddownloader.render

import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage

class GLFont(resourcePath: String, private val basePx: Float) {

    private class Glyph(val tex: Int, val w: Int, val h: Int, val advance: Int)

    private val font: Font
    private val fm: java.awt.FontMetrics
    private val ascent: Int
    private val lineHeight: Int
    private val glyphs = HashMap<Char, Glyph>()

    init {
        GLFont::class.java.getResourceAsStream(resourcePath).use { stream ->
            requireNotNull(stream) { "font not found: $resourcePath" }
            font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(basePx)
        }
        val g = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics()
        g.font = font
        fm = g.fontMetrics
        ascent = fm.ascent
        lineHeight = fm.height
        g.dispose()
    }

    private fun rasterize(c: Char): Glyph {
        val pad = 2
        val advance = maxOf(1, fm.charWidth(c))
        val w = advance + pad * 2
        val h = lineHeight + pad * 2
        val img = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        g.font = font
        g.color = Color.WHITE
        g.drawString(c.toString(), pad.toFloat(), (pad + ascent).toFloat())
        g.dispose()

        val buf = BufferUtils.createByteBuffer(w * h * 4)
        for (y in 0 until h) for (x in 0 until w) {
            val a = (img.getRGB(x, y) ushr 24) and 0xFF
            buf.put(255.toByte()).put(255.toByte()).put(255.toByte()).put(a.toByte())
        }
        buf.flip()

        val tex = GL11.glGenTextures()
        GlStateManager.bindTexture(tex)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf)
        return Glyph(tex, w, h, advance)
    }

    private fun glyph(c: Char) = glyphs.getOrPut(c) { rasterize(c) }

    fun width(text: String, px: Float): Float {
        val scale = px / basePx
        var w = 0f
        for (c in text) w += glyph(c).advance * scale
        return w
    }

    fun height(px: Float): Float = lineHeight * (px / basePx)

    fun draw(text: String, x: Float, y: Float, color: Int, px: Float) {
        val scale = px / basePx
        val a = (color ushr 24 and 0xFF) / 255f
        val r = (color ushr 16 and 0xFF) / 255f
        val g = (color ushr 8 and 0xFF) / 255f
        val b = (color and 0xFF) / 255f
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.enableTexture2D()
        GlStateManager.color(r, g, b, a)
        var penX = x
        for (c in text) {
            val gl = glyph(c)
            val w = gl.w * scale
            val h = gl.h * scale
            GlStateManager.bindTexture(gl.tex)
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glTexCoord2f(0f, 0f); GL11.glVertex2f(penX, y)
            GL11.glTexCoord2f(0f, 1f); GL11.glVertex2f(penX, y + h)
            GL11.glTexCoord2f(1f, 1f); GL11.glVertex2f(penX + w, y + h)
            GL11.glTexCoord2f(1f, 0f); GL11.glVertex2f(penX + w, y)
            GL11.glEnd()
            penX += gl.advance * scale
        }
        GlStateManager.color(1f, 1f, 1f, 1f)
    }
}

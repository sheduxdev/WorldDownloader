package dev.shedux.worlddownloader.render

import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object Draw {

    private fun color(c: Int) {
        val a = (c ushr 24 and 0xFF) / 255f
        val r = (c ushr 16 and 0xFF) / 255f
        val g = (c ushr 8 and 0xFF) / 255f
        val b = (c and 0xFF) / 255f
        GlStateManager.color(r, g, b, a)
    }

    private inline fun shape(c: Int, body: () -> Unit) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        color(c)
        body()
        GlStateManager.enableTexture2D()
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    fun rect(x: Float, y: Float, w: Float, h: Float, c: Int) = shape(c) {
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glVertex2f(x, y + h); GL11.glVertex2f(x + w, y + h)
        GL11.glVertex2f(x + w, y); GL11.glVertex2f(x, y)
        GL11.glEnd()
    }

    private fun corner(cx: Float, cy: Float, r: Float, from: Double) {
        GL11.glBegin(GL11.GL_TRIANGLE_FAN)
        GL11.glVertex2f(cx, cy)
        val seg = 12
        for (i in 0..seg) {
            val a = from + (PI / 2) * (i.toDouble() / seg)
            GL11.glVertex2f(cx + cos(a).toFloat() * r, cy + sin(a).toFloat() * r)
        }
        GL11.glEnd()
    }

    fun roundedRect(x: Float, y: Float, w: Float, h: Float, r: Float, c: Int) = shape(c) {
        val rr = minOf(r, w / 2, h / 2)
        GL11.glBegin(GL11.GL_QUADS)
        // center cross: horizontal band + vertical band
        GL11.glVertex2f(x + rr, y + h); GL11.glVertex2f(x + w - rr, y + h)
        GL11.glVertex2f(x + w - rr, y); GL11.glVertex2f(x + rr, y)
        GL11.glVertex2f(x, y + h - rr); GL11.glVertex2f(x + rr, y + h - rr)
        GL11.glVertex2f(x + rr, y + rr); GL11.glVertex2f(x, y + rr)
        GL11.glVertex2f(x + w - rr, y + h - rr); GL11.glVertex2f(x + w, y + h - rr)
        GL11.glVertex2f(x + w, y + rr); GL11.glVertex2f(x + w - rr, y + rr)
        GL11.glEnd()
        corner(x + rr, y + rr, rr, PI)
        corner(x + w - rr, y + rr, rr, PI * 1.5)
        corner(x + w - rr, y + h - rr, rr, 0.0)
        corner(x + rr, y + h - rr, rr, PI * 0.5)
    }

    // 1px-ish outline by drawing the rounded rect slightly larger behind, then punching the fill on top.
    fun outline(x: Float, y: Float, w: Float, h: Float, r: Float, t: Float, c: Int, fill: Int) {
        roundedRect(x, y, w, h, r, c)
        roundedRect(x + t, y + t, w - t * 2, h - t * 2, maxOf(0f, r - t), fill)
    }
}

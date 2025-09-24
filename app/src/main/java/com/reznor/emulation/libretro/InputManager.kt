package com.reznor.emulation.libretro

import android.view.KeyEvent
import android.view.MotionEvent

class InputManager {

    // RetroPad button mappings
    enum class RetroPadButton(val retroId: Int) {
        B(0),
        Y(1),
        SELECT(2),
        START(3),
        UP(4),
        DOWN(5),
        LEFT(6),
        RIGHT(7),
        A(8),
        X(9),
        L(10),
        R(11),
        L2(12),
        R2(13),
        L3(14),
        R3(15)
    }

    private val buttonStates = BooleanArray(16) { false }

    // Touch controls for virtual gamepad
    data class TouchControl(
        val x: Float,
        val y: Float,
        val radius: Float,
        val button: RetroPadButton
    )

    private val touchControls = mutableListOf<TouchControl>()

    fun initializeTouchControls(screenWidth: Int, screenHeight: Int) {
        touchControls.clear()

        val buttonSize = 80f
        val margin = 20f

        // D-pad (left side)
        val dpadCenterX = screenWidth * 0.15f
        val dpadCenterY = screenHeight * 0.75f

        touchControls.add(TouchControl(dpadCenterX, dpadCenterY - buttonSize, buttonSize, RetroPadButton.UP))
        touchControls.add(TouchControl(dpadCenterX, dpadCenterY + buttonSize, buttonSize, RetroPadButton.DOWN))
        touchControls.add(TouchControl(dpadCenterX - buttonSize, dpadCenterY, buttonSize, RetroPadButton.LEFT))
        touchControls.add(TouchControl(dpadCenterX + buttonSize, dpadCenterY, buttonSize, RetroPadButton.RIGHT))

        // Action buttons (right side)
        val actionCenterX = screenWidth * 0.85f
        val actionCenterY = screenHeight * 0.75f

        touchControls.add(TouchControl(actionCenterX - buttonSize, actionCenterY - buttonSize, buttonSize, RetroPadButton.Y))
        touchControls.add(TouchControl(actionCenterX + buttonSize, actionCenterY - buttonSize, buttonSize, RetroPadButton.X))
        touchControls.add(TouchControl(actionCenterX - buttonSize, actionCenterY + buttonSize, buttonSize, RetroPadButton.A))
        touchControls.add(TouchControl(actionCenterX + buttonSize, actionCenterY + buttonSize, buttonSize, RetroPadButton.B))

        // Shoulder buttons
        touchControls.add(TouchControl(margin + buttonSize, margin + buttonSize, buttonSize, RetroPadButton.L))
        touchControls.add(TouchControl(screenWidth - margin - buttonSize, margin + buttonSize, buttonSize, RetroPadButton.R))
    }

    fun handleKeyEvent(keyCode: Int, action: Int): Boolean {
        val pressed = action == KeyEvent.ACTION_DOWN
        val button = mapKeyToButton(keyCode) ?: return false

        buttonStates[button.retroId] = pressed
        return true
    }

    fun handleTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val pointerIndex = event.actionIndex
        val x = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)

        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                // Check if touch is on a control
                touchControls.forEach { control ->
                    if (isPointInCircle(x, y, control.x, control.y, control.radius)) {
                        buttonStates[control.button.retroId] = true
                        return true
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                // Release all buttons (simplified - should track individual pointers)
                buttonStates.fill(false)
                return true
            }
        }

        return false
    }

    private fun mapKeyToButton(keyCode: Int): RetroPadButton? {
        return when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> RetroPadButton.A
            KeyEvent.KEYCODE_BUTTON_B -> RetroPadButton.B
            KeyEvent.KEYCODE_BUTTON_X -> RetroPadButton.X
            KeyEvent.KEYCODE_BUTTON_Y -> RetroPadButton.Y
            KeyEvent.KEYCODE_BUTTON_L1 -> RetroPadButton.L
            KeyEvent.KEYCODE_BUTTON_R1 -> RetroPadButton.R
            KeyEvent.KEYCODE_BUTTON_SELECT -> RetroPadButton.SELECT
            KeyEvent.KEYCODE_BUTTON_START -> RetroPadButton.START
            KeyEvent.KEYCODE_DPAD_UP -> RetroPadButton.UP
            KeyEvent.KEYCODE_DPAD_DOWN -> RetroPadButton.DOWN
            KeyEvent.KEYCODE_DPAD_LEFT -> RetroPadButton.LEFT
            KeyEvent.KEYCODE_DPAD_RIGHT -> RetroPadButton.RIGHT
            else -> null
        }
    }

    private fun isPointInCircle(px: Float, py: Float, cx: Float, cy: Float, radius: Float): Boolean {
        val dx = px - cx
        val dy = py - cy
        return dx * dx + dy * dy <= radius * radius
    }

    fun getButtonState(port: Int, device: Int, index: Int, id: Int): Int {
        // Simplified - only handle RetroPad device
        if (device == 1 && port == 0 && index == 0 && id < buttonStates.size) {
            return if (buttonStates[id]) 1 else 0
        }
        return 0
    }

    fun pollInput() {
        // Called by libretro to poll input state
        // In a real implementation, this would update input states
    }
}
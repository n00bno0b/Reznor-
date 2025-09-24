package com.reznor.emulation.libretro

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class LibretroVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private val renderer = VideoRenderer()
    var inputManager: InputManager? = null

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        // Enable touch and key events
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
    }

    fun updateVideoFrame(data: ByteBuffer, width: Int, height: Int, pitch: Int) {
        renderer.updateFrame(data, width, height, pitch)
        requestRender()
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        inputManager?.handleTouchEvent(event)
        return true
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent): Boolean {
        inputManager?.handleKeyEvent(keyCode, android.view.KeyEvent.ACTION_DOWN)
        return true
    }

    override fun onKeyUp(keyCode: Int, event: android.view.KeyEvent): Boolean {
        inputManager?.handleKeyEvent(keyCode, android.view.KeyEvent.ACTION_UP)
        return true
    }

    private class VideoRenderer : Renderer {
        private var textureId = 0
        private var program = 0
        private var positionHandle = 0
        private var textureHandle = 0
        private var mvpHandle = 0

        private lateinit var vertexBuffer: FloatBuffer
        private lateinit var textureBuffer: FloatBuffer

        private var frameData: ByteBuffer? = null
        private var frameWidth = 0
        private var frameHeight = 0
        private var framePitch = 0

        private val vertexShaderCode = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            uniform mat4 uMVP;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = uMVP * aPosition;
                vTexCoord = aTexCoord;
            }
        """.trimIndent()

        private val fragmentShaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            varying vec2 vTexCoord;
            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
            }
        """.trimIndent()

        init {
            // Vertex coordinates for a full-screen quad
            val vertices = floatArrayOf(
                -1.0f,  1.0f, 0.0f,  // top left
                -1.0f, -1.0f, 0.0f,  // bottom left
                 1.0f, -1.0f, 0.0f,  // bottom right
                 1.0f,  1.0f, 0.0f   // top right
            )

            // Texture coordinates
            val texCoords = floatArrayOf(
                0.0f, 0.0f,  // top left
                0.0f, 1.0f,  // bottom left
                1.0f, 1.0f,  // bottom right
                1.0f, 0.0f   // top right
            )

            vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices)
            vertexBuffer.position(0)

            textureBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(texCoords)
            textureBuffer.position(0)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            Log.i("VideoRenderer", "Surface created")

            // Create shaders
            val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

            // Create program
            program = GLES20.glCreateProgram()
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)

            // Get handles
            positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
            textureHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
            mvpHandle = GLES20.glGetUniformLocation(program, "uMVP")

            // Create texture
            val textures = IntArray(1)
            GLES20.glGenTextures(1, textures, 0)
            textureId = textures[0]

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            Log.i("VideoRenderer", "Surface changed: ${width}x$height")
            GLES20.glViewport(0, 0, width, height)
        }

        override fun onDrawFrame(gl: GL10?) {
            frameData?.let { data ->
                // Update texture with frame data
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
                GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    frameWidth, frameHeight, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data
                )

                // Clear screen
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

                // Use program
                GLES20.glUseProgram(program)

                // Set vertex attributes
                GLES20.glEnableVertexAttribArray(positionHandle)
                GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

                GLES20.glEnableVertexAttribArray(textureHandle)
                GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)

                // Set MVP matrix (identity for now)
                val mvp = FloatArray(16)
                android.opengl.Matrix.setIdentityM(mvp, 0)
                GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvp, 0)

                // Draw
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

                // Disable attributes
                GLES20.glDisableVertexAttribArray(positionHandle)
                GLES20.glDisableVertexAttribArray(textureHandle)
            }
        }

        fun updateFrame(data: ByteBuffer, width: Int, height: Int, pitch: Int) {
            frameData = data
            frameWidth = width
            frameHeight = height
            framePitch = pitch
        }

        private fun loadShader(type: Int, shaderCode: String): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            return shader
        }
    }
}
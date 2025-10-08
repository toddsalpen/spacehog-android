package com.spacehog.ui.splash

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.media.MediaPlayer
import android.os.Process
import android.util.Log
import android.view.SurfaceHolder
import com.spacehog.R
import com.spacehog.data.DrawablesManager
import com.spacehog.logic.StarfieldManager
import com.spacehog.model.Sprite
import com.spacehog.model.StarField

class SplashScreenRenderer(
    context: Context,
    private val holder: SurfaceHolder,
    private val starfieldManager: StarfieldManager
) : Runnable {
    private val TAG = "StarfieldDebug"
    private var thread: Thread? = null
    @Volatile private var isRunning = false

    private val screenSize: Point
    private val logo: Sprite
    private val paint = Paint()
    private val mediaPlayer: MediaPlayer?

    companion object {
        private const val MAX_FPS = 60
        private const val FRAME_PERIOD = (1000 / MAX_FPS).toLong()
    }

    init {
        // Get screen size from the surface holder
        val screenRect = holder.surfaceFrame
        screenSize = Point(screenRect.width(), screenRect.height())

        val drawablesManager = DrawablesManager(context)
        val logoBitmap = drawablesManager.getBitmap(R.drawable.space_hog)
            ?: throw IllegalStateException("Splash screen logo 'space_hog' not found")

        // Calculate logo size to fit screen while maintaining aspect ratio
        val logoWidth: Float
        val logoHeight: Float
        val imageRatio = logoBitmap.width.toFloat() / logoBitmap.height
        val screenRatio = screenSize.x.toFloat() / screenSize.y

        if (imageRatio <= screenRatio) {
            logoHeight = screenSize.y.toFloat()
            logoWidth = logoHeight * imageRatio
        } else {
            logoWidth = screenSize.x.toFloat()
            logoHeight = logoWidth / imageRatio
        }

        val logoX = (screenSize.x - logoWidth) * 0.5f
        val logoY = (screenSize.y - logoHeight) * 0.5f

        logo = Sprite(logoBitmap, logoWidth, logoHeight, logoX, logoY)
        logo.addFrameFromMaster(0, 0, logo.masterBitmap.width, logo.masterBitmap.height)

        mediaPlayer = MediaPlayer.create(context, R.raw.splash_screen_music)
    }

    override fun run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
        mediaPlayer?.start()
        mediaPlayer?.isLooping = true

        while (isRunning) {
            if (!holder.surface.isValid) continue

            val beginTime = System.currentTimeMillis()

            val canvas = holder.lockCanvas()
            try {
                synchronized(holder) {
                    updateCanvas()
                    drawCanvas(canvas)
                }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }

            val timeDiff = System.currentTimeMillis() - beginTime
            val sleepTime = FRAME_PERIOD - timeDiff

            if (sleepTime > 0) {
                Thread.sleep(sleepTime)
            }
        }
    }

    private fun updateCanvas() {
    }

    private fun drawCanvas(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)

        paint.color = Color.WHITE
        for (star in starfieldManager.getStarStates()) {
            paint.alpha = (star.alpha * 255).toInt().coerceIn(0, 255)
            canvas.drawPoint(star.x, star.y, paint)
        }

        paint.alpha = 255
        logo.draw(canvas, paint)
    }

    fun resume() {
        if (thread == null || !thread!!.isAlive) {
            isRunning = true
            thread = Thread(this)
            thread?.start()
        }
    }

    fun pause() {
        isRunning = false
        mediaPlayer?.pause()
        try {
            thread?.join(200) // Wait a bit for the thread to finish
        } catch (e: InterruptedException) {
            //
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    // A new method for when the screen is fully destroyed
    fun stopAndRelease() {
        pause() // Ensure the thread is stopped
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}
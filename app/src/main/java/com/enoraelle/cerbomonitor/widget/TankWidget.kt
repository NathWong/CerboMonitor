package com.enoraelle.cerbomonitor.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.enoraelle.cerbomonitor.Constants
import com.enoraelle.cerbomonitor.ui.theme.CyanTank
import com.enoraelle.cerbomonitor.ui.theme.DarkBackground
import com.enoraelle.cerbomonitor.ui.theme.LightBackground
import com.enoraelle.cerbomonitor.ui.theme.OrangeWarning
import com.enoraelle.cerbomonitor.ui.theme.RedError
import com.enoraelle.cerbomonitor.worker.TankUpdateWorker

class TankWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val sharedPrefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val tankLevel = sharedPrefs.getFloat(Constants.KEY_TANK_LEVEL, -1f)
        val isOnline = sharedPrefs.getBoolean(Constants.KEY_IS_ONLINE, false)

        provideContent {
            val levelText = if (tankLevel >= 0f) "$tankLevel %" else "-- %"

            val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val isDarkTheme = uiMode == Configuration.UI_MODE_NIGHT_YES
            val widgetBackgroundColor = if (isDarkTheme) DarkBackground else LightBackground

            val gaugeColor = if (tankLevel > 20f) CyanTank else OrangeWarning
            val textColor = if (isOnline) Color.White else RedError

            val fillPercentage = if (tankLevel >= 0f) (tankLevel / 100f).coerceIn(0f, 1f) else 0f
            val gaugeBitmap =
                generateWaterBitmap(
                    400,
                    800,
                    fillPercentage,
                    gaugeColor,
                    widgetBackgroundColor
                )

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
            ) {
                Image(
                    provider = ImageProvider(gaugeBitmap),
                    contentDescription = "Level gauge",
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Water", // @todo receive name from config
                            style = TextStyle(
                                color = ColorProvider(Color.DarkGray, Color.White),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Image(
                            provider = ImageProvider(android.R.drawable.ic_popup_sync),
                            contentDescription = "Refresh",
                            modifier = GlanceModifier
                                .size(24.dp)
                                .clickable(actionRunCallback<RefreshAction>())
                        )
                    }

                    Text(
                        text = levelText,
                        style = TextStyle(
                            color = ColorProvider(textColor, textColor),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier.padding(top = 4.dp)
                    )

                    if (!isOnline) {
                        Text(
                            text = "Offline",
                            style = TextStyle(
                                color = ColorProvider(RedError, RedError),
                                fontSize = 12.sp
                            ),
                            modifier = GlanceModifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }

    private fun generateWaterBitmap(
        width: Int,
        height: Int,
        fillPercentage: Float,
        waterColor: Color,
        backgroundColor: Color
    ): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        canvas.drawColor(backgroundColor.toArgb())

        // Early return if empty
        if (fillPercentage <= 0f) return bitmap

        val paint = Paint().apply {
            color = waterColor.toArgb()
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val waterY = height - (height * fillPercentage)

        val path = Path()
        path.moveTo(0f, waterY)

        // Draw wave only if not full
        if (fillPercentage > 0.02f && fillPercentage < 0.98f) {
            val waveAmplitude = 15f // Wave height

            path.quadTo(width * 0.25f, waterY - waveAmplitude, width * 0.5f, waterY)
            path.quadTo(width * 0.75f, waterY + waveAmplitude, width.toFloat(), waterY)
        } else {
            path.lineTo(width.toFloat(), waterY)
        }

        path.lineTo(width.toFloat(), height.toFloat())
        path.lineTo(0f, height.toFloat())
        path.close()

        canvas.drawPath(path, paint)

        return bitmap
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val request = OneTimeWorkRequestBuilder<TankUpdateWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
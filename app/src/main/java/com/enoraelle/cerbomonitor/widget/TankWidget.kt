package com.enoraelle.cerbomonitor.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.enoraelle.cerbomonitor.Constants
import com.enoraelle.cerbomonitor.ui.theme.*
import com.enoraelle.cerbomonitor.worker.TankUpdateWorker

class TankWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val sharedPrefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val tankLevel = sharedPrefs.getFloat(Constants.KEY_TANK_LEVEL, -1f)
        val isOnline = sharedPrefs.getBoolean(Constants.KEY_IS_ONLINE, false)

        provideContent {
            val levelText = if (tankLevel >= 0f) "$tankLevel %" else "-- %"

            val gaugeColor = if (tankLevel > 20f) CyanTank else OrangeWarning
            val textColor = if (isOnline) Color.White else RedError

            val widgetHeight = LocalSize.current.height
            val fillPercentage = if (tankLevel >= 0f) (tankLevel / 100f).coerceIn(0f, 1f) else 0f
            val calculatedHeight = widgetHeight * fillPercentage

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(DarkBackground)
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(calculatedHeight)
                            .background(gaugeColor.copy(alpha = 0.25f))
                    ) {}
                }

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
                            text = "Eau Douce",
                            style = TextStyle(color = ColorProvider(Color.White, Color.White), fontSize = 14.sp)
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
                            text = "Injoignable",
                            style = TextStyle(color = ColorProvider(RedError, RedError), fontSize = 12.sp),
                            modifier = GlanceModifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val request = OneTimeWorkRequestBuilder<TankUpdateWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
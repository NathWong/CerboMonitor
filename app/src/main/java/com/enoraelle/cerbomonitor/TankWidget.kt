package com.enoraelle.cerbomonitor

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
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class TankWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val sharedPrefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val tankLevel = sharedPrefs.getFloat(Constants.KEY_TANK_LEVEL, -1f)
        val isOnline = sharedPrefs.getBoolean(Constants.KEY_IS_ONLINE, false)

        provideContent {
            val levelText = if (tankLevel >= 0f) "$tankLevel %" else "-- %"

            // Si la cuve est presque vide (< 20%), on la met en orange/rouge, sinon bleu cyan
            val gaugeColor = if (tankLevel > 20f) Color(0xFF00E5FF) else Color(0xFFFF9800)
            val textColor = if (isOnline) Color.White else Color(0xFFFF5252)

            // Calcul des proportions (0.0 à 1.0)
            val fillWeight = if (tankLevel >= 0f) (tankLevel / 100f).coerceIn(0f, 1f) else 0f
            val emptyWeight = 1f - fillWeight

            // Conteneur principal (Fond gris foncé)
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E))
            ) {
                // 1. LA JAUGE (En arrière-plan)
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    // La partie vide (transparente) qui pousse le liquide vers le bas
                    if (emptyWeight > 0f) {
                        Spacer(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .defaultWeight()
                        )
                    }
                    // La partie pleine (colorée)
                    if (fillWeight > 0f) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(0.dp) // Hauteur gérée par le weight
                                .background(gaugeColor.copy(alpha = 0.25f)) // Transparent à 25%
                                .defaultWeight() // Attention à l'erreur précédente : ici on utilise defaultWeight() propre à Glance
                        ) {}
                    }
                }

                // 2. LE TEXTE ET BOUTONS (Par-dessus la jauge)
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
                            style = TextStyle(color = ColorProvider(Color(0xFFFF5252), Color(0xFFFF5252)), fontSize = 12.sp),
                            modifier = GlanceModifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// L'action de rafraîchissement manuel
class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val request = OneTimeWorkRequestBuilder<TankUpdateWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
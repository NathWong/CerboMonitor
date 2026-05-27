package com.enoraelle.cerbomonitor

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class TankWidgetReceiver : GlanceAppWidgetReceiver() {
    // On indique à Android quel design utiliser pour ce widget
    override val glanceAppWidget = TankWidget()
}
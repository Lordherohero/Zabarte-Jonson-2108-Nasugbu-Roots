package com.mygroup.nasugburoots.ui

import android.app.Application
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.mygroup.nasugburoots.model.TouristSpot
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.internal.MapsforgeThemes
import java.io.File
import java.io.FileInputStream

private const val MAP_FILE_NAME = "planet_120.348_14.063_d073dc7d.map"

private fun addMarker(
    mapView: MapView,
    markers: MutableList<View>,
    latitude: Double,
    longitude: Double,
    onClick: () -> Unit
) {
    val marker = TextView(mapView.context).apply {
        text = "📍"
        textSize = 32f
        setPadding(8, 8, 8, 8)
        setOnClickListener {
            onClick()
        }
    }

    mapView.addView(
        marker,
        MapView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            LatLong(latitude, longitude),
            MapView.LayoutParams.Alignment.BOTTOM_CENTER
        )
    )

    markers.add(marker)
}

@Composable
fun NasugbuMapScreen(
    spots: List<TouristSpot> = emptyList(),
    onSpotClick: (TouristSpot) -> Unit = {}
) {
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val markers = remember { mutableListOf<View>() }
    val fitted = remember { booleanArrayOf(false) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            try {
                AndroidGraphicFactory.createInstance(
                    ctx.applicationContext as Application
                )

                val mapFile = File(ctx.cacheDir, MAP_FILE_NAME)
                if (!mapFile.exists()) {
                    ctx.assets.open(MAP_FILE_NAME).use { input ->
                        mapFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                val mapView = MapView(ctx)
                mapViewState.value = mapView

                val tileCache = AndroidUtil.createTileCache(
                    ctx,
                    "nasugbu-map-cache",
                    mapView.model.displayModel.tileSize,
                    1f,
                    mapView.model.frameBufferModel.overdrawFactor
                )

                val mapDataStore = MapFile(FileInputStream(mapFile))

                val mapLayer = TileRendererLayer(
                    tileCache,
                    mapDataStore,
                    mapView.model.mapViewPosition,
                    AndroidGraphicFactory.INSTANCE
                )
                mapLayer.setXmlRenderTheme(MapsforgeThemes.DEFAULT)
                mapView.layerManager.layers.add(mapLayer)

                mapView.setCenter(LatLong(14.07125, 120.62498))
                mapView.setZoomLevel(14)
                mapView.setBuiltInZoomControls(true)
                mapView.mapScaleBar.isVisible = true
                mapView
            } catch (e: Exception) {
                TextView(ctx).apply {
                    text = "Unable to load Nasugbu map: ${e.message}"
                }
            }
        },
        update = { view ->
            if (view is MapView) {
                markers.forEach { marker ->
                    view.removeView(marker)
                }
                markers.clear()

                if (spots.isEmpty()) {
                    addMarker(view, markers, 14.07125, 120.62498) {
                        Toast.makeText(
                            view.context,
                            "Nasugbu Landing Monument",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    spots.forEach { spot ->
                        addMarker(view, markers, spot.latitude, spot.longitude) {
                            onSpotClick(spot)
                        }
                    }

                    if (!fitted[0]) {
                        val centerLat = spots.map { it.latitude }.average()
                        val centerLon = spots.map { it.longitude }.average()
                        view.setCenter(LatLong(centerLat, centerLon))
                        view.setZoomLevel(12)
                        fitted[0] = true
                    }
                }
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            mapViewState.value?.destroyAll()
            AndroidGraphicFactory.clearResourceMemoryCache()
        }
    }
}

package com.mygroup.nasugburoots.ui

import android.app.Application
import android.graphics.Color
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
import com.mygroup.nasugburoots.data.sampleSpots
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
    title: String,
    isFocused: Boolean = false,
    onClick: () -> Unit
) {
    val marker = TextView(mapView.context).apply {
        text = if (isFocused) "📍 $title" else "📍 $title"
        textSize = if (isFocused) 15f else 12f
        if (isFocused) {
            setBackgroundColor(Color.argb(230, 25, 54, 45))
            setTextColor(Color.WHITE)
            setPadding(16, 10, 16, 10)
        } else {
            setBackgroundColor(Color.argb(210, 255, 255, 255))
            setTextColor(Color.rgb(25, 54, 45))
            setPadding(10, 6, 10, 6)
        }
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
    focusedSpot: TouristSpot? = null,
    onSpotClick: (TouristSpot) -> Unit = {}
) {
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val markers = remember { mutableListOf<View>() }
    val lastCenteredSpotId = remember { mutableStateOf<Int?>(null) }

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

                val initialLat = focusedSpot?.latitude ?: 14.07125
                val initialLon = focusedSpot?.longitude ?: 120.62498
                val initialZoom = if (focusedSpot != null) 16 else 13

                mapView.setCenter(LatLong(initialLat, initialLon))
                mapView.setZoomLevel(initialZoom.toByte())
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

                val displaySpots = if (spots.isEmpty()) sampleSpots else spots

                displaySpots.forEach { spot ->
                    val isFocused = focusedSpot != null && spot.placeId == focusedSpot.placeId
                    addMarker(
                        mapView = view,
                        markers = markers,
                        latitude = spot.latitude,
                        longitude = spot.longitude,
                        title = spot.placeName,
                        isFocused = isFocused
                    ) {
                        Toast.makeText(view.context, spot.placeName, Toast.LENGTH_SHORT).show()
                        onSpotClick(spot)
                    }
                }

                val target = focusedSpot
                if (target != null && lastCenteredSpotId.value != target.placeId) {
                    lastCenteredSpotId.value = target.placeId
                    view.setCenter(LatLong(target.latitude, target.longitude))
                    view.setZoomLevel(16.toByte())
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

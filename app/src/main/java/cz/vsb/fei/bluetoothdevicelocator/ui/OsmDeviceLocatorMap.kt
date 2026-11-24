package cz.vsb.fei.bluetoothdevicelocator.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceEntity
import cz.vsb.fei.bluetoothdevicelocator.data.LocationData
import cz.vsb.fei.bluetoothdevicelocator.data.LocationEntity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import cz.vsb.fei.bluetoothdevicelocator.R

/**
 * Initial zoom factor for the map.
 */
private const val INITIAL_MAP_ZOOM_FACTOR = 17.0

/**
 * Scale factor for zooming in on the markers.
 */
private const val ZOOM_IN_SCALE_FACTOR = 2f

/**
 * Color of the line between markers.
 */
private const val BETWEEN_MARKERS_LINE_COLOR = Color.RED

/**
 * Width of the line between markers.
 */
private const val BETWEEN_MARKERS_LINE_WIDTH = 7f

/**
 * Fill color of the device position accuracy circle.
 */
private val ACCURACY_CIRCLE_FILL_COLOR =  Color.argb(45, 30, 136, 229)

/**
 * Stroke color of the device position accuracy circle.
 */
private val ACCURACY_CIRCLE_STROKE_COLOR = Color.rgb(30, 136, 229)

/**
 * Stroke width of the device position accuracy circle.
 */
private const val ACCURACY_CIRCLE_STROKE_WIDTH = 3f

/**
 * Defines OpenStreetMap component with device and user location.
 * @param device Device to find.
 * @param deviceLocation Location of the device.
 * @param deviceLocationRssi Value of RSSI for the device at the location.
 * @param currentLocation Current location of the phone.
 * @param modifier Modifier to apply to the layout.
 */
@Composable
fun OsmDeviceLocatorMap(
    device: DeviceEntity,
    deviceLocation: LocationEntity,
    deviceLocationRssi: Float,
    currentLocation: LocationData?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // mandatory for osmdroid
    remember {
        Configuration.getInstance().userAgentValue = context.packageName
        true
    }

    /**
     * Point on the map with device location.
     */
    val devicePoint = remember(deviceLocation.latitude, deviceLocation.longitude) {
        GeoPoint(deviceLocation.latitude, deviceLocation.longitude)
    }

    /**
     * Flag whether both markers have been centered on the map.
     */
    var zoomedToMarkers by remember { mutableStateOf(false) }

    var centeredOnBothMarkersForFirstTime by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(INITIAL_MAP_ZOOM_FACTOR)
                controller.setCenter(devicePoint)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            val netAccuracy: Double = deviceLocation.calculateNetAccuracy(
                deviceLocation.accuracy, deviceLocationRssi)

            val userPoint: GeoPoint? = if (currentLocation != null) {
                GeoPoint(currentLocation.latitude, currentLocation.longitude)
            } else {
                null
            }

            drawAccuracyCircle(netAccuracy, devicePoint, mapView)
            drawDeviceAndUserMarkers(device, devicePoint, userPoint, context, mapView)

            // zoom to markers if first time or when requested via button
            if (!zoomedToMarkers || (!centeredOnBothMarkersForFirstTime && userPoint != null)) {
                zoomedToMarkers = true
                if (userPoint != null) centeredOnBothMarkersForFirstTime = true
                zoomToMarkers(devicePoint, userPoint, mapView)
            }

            mapView.invalidate()
        },
        onRelease = { mapView ->
            mapView.onDetach()
        }
    )

    FloatingActionButton(
        onClick = { zoomedToMarkers = false },
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Adjust,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
        )
    }
}

/**
 * Draws device location accuracy circle on the map.
 * @param netAccuracy Net accuracy of the device location in meters.
 * @param devicePoint Point on the map with device location.
 * @param mapView MapView to draw on.
 */
private fun drawAccuracyCircle(
    netAccuracy: Double,
    devicePoint: GeoPoint,
    mapView: MapView
) {
    if (netAccuracy > 0.0) {
        val accuracyCircle = Polygon(mapView).apply {
            points = Polygon.pointsAsCircle(
                devicePoint, netAccuracy)
            fillPaint.color = ACCURACY_CIRCLE_FILL_COLOR
            outlinePaint.color = ACCURACY_CIRCLE_STROKE_COLOR
            outlinePaint.strokeWidth = ACCURACY_CIRCLE_STROKE_WIDTH
            outlinePaint.style = Paint.Style.STROKE
        }
        mapView.overlays.add(accuracyCircle)
    }
}

/**
 * Draws device and user markers on the map.
 * @param device Device to find.
 * @param devicePoint Point on the map with device location.
 * @param userPoint Point on the map with user location.
 * @param context Context to use.
 * @param mapView MapView to draw on.
 */
private fun drawDeviceAndUserMarkers(
    device: DeviceEntity,
    devicePoint: GeoPoint,
    userPoint: GeoPoint?,
    context: Context,
    mapView: MapView
) {
    val deviceMarker = Marker(mapView).apply {
        position = devicePoint
        title = device.name
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    }

    val userMarker = if (userPoint != null) {
        Marker(mapView).apply {
            position = userPoint
            title = context.resources.getString(R.string.my_location_label)
            icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
    } else {
        null
    }

    drawLineBetweenMarkers(devicePoint, userPoint, mapView)
    mapView.overlays.add(deviceMarker)
    mapView.overlays.add(userMarker)
}

/**
 * Draws line between markers on the map.
 * @param devicePoint Point on the map with device location.
 * @param userPoint Point on the map with user location.
 * @param mapView MapView to draw on.
 */
private fun drawLineBetweenMarkers(
    devicePoint: GeoPoint,
    userPoint: GeoPoint?,
    mapView: MapView
) {
    if (userPoint == null) return

    val line = Polyline(mapView).apply {
        outlinePaint.color = BETWEEN_MARKERS_LINE_COLOR
        outlinePaint.strokeWidth = BETWEEN_MARKERS_LINE_WIDTH
        outlinePaint.style = Paint.Style.STROKE
        addPoint(devicePoint)
        addPoint(userPoint)
    }

    mapView.overlays.add(line)
}

/**
 * Zooms to markers on the map.
 * @param devicePoint Point on the map with device location.
 * @param userPoint Point on the map with user location.
 * @param mapView MapView to draw on.
 */
private fun zoomToMarkers(
    devicePoint: GeoPoint,
    userPoint: GeoPoint?,
    mapView: MapView
) {
    if (userPoint != null) {
        mapView.post {
            val box = BoundingBox.fromGeoPointsSafe(listOf(devicePoint, userPoint))
                .increaseByScale(ZOOM_IN_SCALE_FACTOR)
            mapView.zoomToBoundingBox(box, true, 120)
        }
    } else {
        mapView.post {
            mapView.controller.animateTo(devicePoint)
        }
    }
}

package dji.v5.ux.mapkit.maplibre.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import dji.v5.ux.mapkit.core.callback.MapScreenShotListener;
import dji.v5.ux.mapkit.core.callback.OnMapTypeLoadedListener;
import dji.v5.ux.mapkit.core.camera.DJICameraUpdate;
import dji.v5.ux.mapkit.core.maps.DJIBaseMap;
import dji.v5.ux.mapkit.core.maps.DJIMap;
import dji.v5.ux.mapkit.core.maps.DJIProjection;
import dji.v5.ux.mapkit.core.maps.DJIUiSettings;
import dji.v5.ux.mapkit.core.models.DJIBitmapDescriptor;
import dji.v5.ux.mapkit.core.models.DJICameraPosition;
import dji.v5.ux.mapkit.core.models.DJILatLng;
import dji.v5.ux.mapkit.core.models.annotations.DJICircle;
import dji.v5.ux.mapkit.core.models.annotations.DJICircleOptions;
import dji.v5.ux.mapkit.core.models.annotations.DJIGroupCircle;
import dji.v5.ux.mapkit.core.models.annotations.DJIGroupCircleOptions;
import dji.v5.ux.mapkit.core.models.annotations.DJIMarker;
import dji.v5.ux.mapkit.core.models.annotations.DJIMarkerOptions;
import dji.v5.ux.mapkit.core.models.annotations.DJIPolygon;
import dji.v5.ux.mapkit.core.models.annotations.DJIPolygonOptions;
import dji.v5.ux.mapkit.core.models.annotations.DJIPolyline;
import dji.v5.ux.mapkit.core.models.annotations.DJIPolylineOptions;
import dji.v5.ux.mapkit.core.utils.DJIMapkitLog;
import dji.v5.ux.mapkit.maplibre.annotations.MCircle;
import dji.v5.ux.mapkit.maplibre.annotations.MGroupCircle;
import dji.v5.ux.mapkit.maplibre.annotations.MLineLayerPolyline;
import dji.v5.ux.mapkit.maplibre.annotations.MMarkerCircle;
import dji.v5.ux.mapkit.maplibre.annotations.MPolygon;
import dji.v5.ux.mapkit.maplibre.annotations.MSymbolLayerMarker;
import dji.v5.ux.mapkit.maplibre.utils.MaplibreUtils;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MapboxMap????????????
 * Created by joeyang on 10/15/17.
 */
public class MaplibreMapDelegate extends DJIBaseMap implements DJIMap,
        MapboxMap.OnMarkerClickListener,
        MapboxMap.OnMapClickListener,
        MapboxMap.OnInfoWindowClickListener,
        Style.OnStyleLoaded,
        MapboxMap.OnCameraMoveListener,
        MapboxMap.OnMapLongClickListener,
        View.OnTouchListener {



    /**
     * zindex - layer???????????????????????????source id
     */
    private static final String HEAD_SOURCE_ID = "head-source-id";

    /**
     * zindex - layer???????????????????????????layer id
     */
    private static final String HEAD_LAYER_ID = "head-layer-id";

    /**
     * zindex - layer???????????????????????????source id
     */
    private static final String TAIL_SOURCE_ID = "tail-source-id";

    /**
     * zindex - layer???????????????????????????layer id
     */
    private static final String TAIL_LAYER_ID = "tail-layer-id";

    /**
     * marker source id?????????
     */
    private static final String MARKER_SOURCE_ID_PREFIX = "marker-source-";

    /**
     * marker layer id?????????
     */
    private static final String MARKER_LAYER_ID_PREFIX = "marker-layer-";

    /**
     * marker bitmap id?????????
     */
    private static final String MARKER_BITMAP_ID_PREFIX = "marker-bitmap-";

    /**
     * line source id?????????
     */
    private static final String LINE_SOURCE_ID_PREFIX = "line-source-";

    /**
     * line layer id?????????
     */
    private static final String LINE_LAYER_ID_PREFIX = "line-layer-";

    /**
     * SingleCircle source id?????????
     */
    private static final String SINGLE_CIRCLE_SOURCE_ID_PREFIX = "single-circle-source-";

    /**
     * SingleCircle layer id?????????
     */
    private static final String SINGLE_CIRCLE_LAYER_ID_PREFIX = "single-circle-layer-";

    /**
     * GroupCircle source id?????????
     */
    private static final String GROUP_CIRCLE_SOURCE_ID_PREFIX = "group-circle-source-";

    /**
     * GroupCircle layer id?????????
     */
    private static final String GROUP_CIRCLE_LAYER_ID_PREFIX = "group-circle-layer-";

    /**
     * MarkerCircle source id?????????
     */
    private static final String MARKER_CIRCLE_SOURCE_ID_PREFIX = "marker-circle-source-";

    /**
     * MarkerCircle layer id?????????
     */
    private static final String MARKER_CIRCLE_LAYER_ID_PREFIX = "marker-circle-layer-";

    /**
     * ????????????style??????????????????stop the world??????????????????????????????marker?????????????????????
     */
    private boolean stoppingWorld = false;

    private Context context;

    /**
     * ????????? MapboxMap ??????
     */
    private MapboxMap mapboxMap;

    /**
     * ??????marker?????????marker???map
     */
    private Map<Marker, DJIMarker> markers;

    /**
     * ??????????????????polyline??????
     */
    private Set<DJIPolyline> polylines;

    /**
     * ???????????????singleCircle??????
     */
    private Set<DJICircle> singleCircles;

    /**
     * ???????????????groupCircle??????
     */
    private Set<DJIGroupCircle> groupCircles;

    /**
     * ???????????????markerCircle??????
     */
    private Set<DJICircle> markerCircles;

    /**
     * ??????map???view
     */
   // private MapView mapView;

    /**
     * Source id ??? Set?????????marker?????????
     */
    // private Set<String> markerSources = new HashSet<>();

    /**
     * marker source?????????
     */
    private int markerSourcesCount = 0;

    /**
     * Layer id ??? Set?????????marker???layer
     */
   // private Set<String> markerLayers = new HashSet<>();

    /**
     * marker layer?????????
     */
    private int markerLayersCount = 0;

    /**
     * Bitmap ??? image id ??? Set?????????marker?????????
     */
    private Set<String> markerBitmaps = new HashSet<>();

    /**
     * marker bitmap?????????
     */
    private int markerBitmapsCount = 0;

    /**
     * line source?????????
     */
    private int lineSourceCount = 0;

    /**
     * line layer?????????
     */
    private int lineLayersCount = 0;

    /**
     * singleCircle source?????????
     */
    private int singleCircleSourceCount = 0;

    /**
     * singleCircle layer?????????
     */
    private int singleCircleLayersCount = 0;

    /**
     * groupCircle source?????????
     */
    private int groupCircleSourceCount = 0;

    /**
     * groupCircle layer?????????
     */
    private int groupCircleLayersCount = 0;

    /**
     * markerCircle layer?????????
     */
    private int markerCircleLayersCount = 0;


    /**
     * ??? ZIndex ????????????????????? layers ??????????????????????????????
     */
    private List<LayerZIndex> sortedLayersByZIndex;

    private Style style;
    private OnMapTypeLoadedListener onMapTypeLoadedListener;

    /**
     * ????????????layer???zindex
     */
    private class LayerZIndex {
        Layer layer;
        long zindex;

        LayerZIndex(Layer layer, long zindex) {
            this.layer = layer;
            this.zindex = zindex;
        }
    }

    /**
     * ZIndex - layer ??? List???????????????marker???ZIndex
     */
   // private LinkedList<Pair<Integer, ? extends Layer>> markerZIndexLayerList = new LinkedList<>();

    public MaplibreMapDelegate(MapboxMap mapboxMap, Context context, MapView view, Style style) {
        this.context = context;
        this.mapboxMap = mapboxMap;
        // this.mapView = view;
        this.style = style;
        view.setOnTouchListener(this);
        mapboxMap.setOnMarkerClickListener(this);
        mapboxMap.addOnMapClickListener(this);
        mapboxMap.setOnInfoWindowClickListener(this);
        mapboxMap.addOnMapLongClickListener(this);
        mapboxMap.addOnCameraMoveListener(this);

        markers = new HashMap<>();
        polylines = new HashSet<>();
        singleCircles = new HashSet<>();
        groupCircles = new HashSet<>();
        markerCircles = new HashSet<>();

        // ?????????marker???????????????
        sortedLayersByZIndex = new LinkedList<>();
        addDummySourcesAndLayers();
    }

    private void addDummySourcesAndLayers() {
        GeoJsonSource headSource = new GeoJsonSource(HEAD_SOURCE_ID);
        SymbolLayer headLayer = new SymbolLayer(HEAD_LAYER_ID, headSource.getId());

        GeoJsonSource tailSource = new GeoJsonSource(TAIL_SOURCE_ID);
        SymbolLayer tailLayer = new SymbolLayer(TAIL_LAYER_ID, tailSource.getId());

        sortedLayersByZIndex.add(new LayerZIndex(headLayer, Long.MIN_VALUE));
        sortedLayersByZIndex.add(new LayerZIndex(tailLayer, Long.MAX_VALUE));

        if (style.isFullyLoaded()) {
            style.addSource(headSource);
            style.addSource(tailSource);
            style.addLayer(headLayer);
            style.addLayerAbove(tailLayer, headLayer.getId());
        }

//        markerSources.add(HEAD_SOURCE_ID);
//        markerSources.add(TAIL_SOURCE_ID);
//        markerLayers.add(HEAD_LAYER_ID);
//        markerLayers.add(TAIL_LAYER_ID);
    }

    private void clearSourcesAndLayers() {
        DJIMapkitLog.d("ready to clearSourceAndLayers");
        if (style.isFullyLoaded()) {
            style.removeLayer(HEAD_LAYER_ID);
            style.removeLayer(TAIL_LAYER_ID);
            style.removeSource(HEAD_SOURCE_ID);
            style.removeSource(TAIL_SOURCE_ID);

            for (DJIMarker marker : markers.values()) {
                final MSymbolLayerMarker symbol = (MSymbolLayerMarker) marker;
                style.removeLayer(symbol.getLayerId());
                style.removeSource(symbol.getSourceId());
                symbol.setSymbolLayer(null);
                symbol.setSource(null);
            }

            for (DJIPolyline polyline : polylines) {
                final MLineLayerPolyline line = (MLineLayerPolyline) polyline;
                style.removeLayer(line.getLayerId());
                style.removeSource(line.getSourceId());
//              Log.d("joe-line-layer", "clearSourcesAndLayers layer id " + line.getLayerId() + ", source id " + line.getSourceId());
            }

            for (DJICircle singleCircle : singleCircles) {
                final MCircle mSingleCircle = (MCircle) singleCircle;
                style.removeLayer(mSingleCircle.getLayerId());
                style.removeSource(mSingleCircle.getSourceId());
            }

            for (DJIGroupCircle groupCircle : groupCircles) {
                final MGroupCircle mGroupCircle = (MGroupCircle) groupCircle;
                style.removeLayer(mGroupCircle.getLayerId());
                style.removeSource(mGroupCircle.getSourceId());
            }

            for (DJICircle markerCircle : markerCircles) {
                final MMarkerCircle mMarkerCircle = (MMarkerCircle) markerCircle;
                style.removeLayer(mMarkerCircle.getLayerId());
                style.removeSource(mMarkerCircle.getSourceID());
            }

//          for (String bitmap : markerBitmaps) {
//              mapboxMap.removeImage(bitmap);
//          }
        }

        sortedLayersByZIndex.clear();
//        markerSources.clear();
//        markerLayers.clear();
//        markerBitmaps.clear();
    }

    /**
     * ?????? <a>https://github.com/mapbox/mapbox-gl-native/issues/2450<a/> ????????????
     * ?????? MapBox ????????? marker ??? draggable ??????
     *
     * @param markerOptions
     */
    @NonNull
    @Override
    public DJIMarker addMarker(DJIMarkerOptions markerOptions) {
        DJILatLng latLng = markerOptions.getPosition();
        if (latLng == null) {
            throw new IllegalArgumentException("DJIMarkerOptions parameter must have position set");
        }

        // ??????shadowMarker???workaround symboLayer????????????infowindow?????????
        MarkerOptions options = new MarkerOptions();
        options.position(MaplibreUtils.fromDJILatLng(latLng));
        DJIBitmapDescriptor bitmapDescriptor = markerOptions.getIcon();
        if (bitmapDescriptor != null) {
            Icon i = MaplibreUtils.fromDJIBitmapDescriptor(context, bitmapDescriptor);
            Bitmap bitmap = i.getBitmap();
            Bitmap shadowBitmap = createTransparentBitmap(bitmap);
            Icon icon = IconFactory.getInstance(context).fromBitmap(shadowBitmap);
            options.icon(icon);
        }
        Marker shadowMarker = mapboxMap.addMarker(options);

        // ?????? SymbolLayer
        String markerLayerId = genMarkerLayerId();
        String markerSourceId = genMarkerSourceId();
        SymbolLayer markerSymbolLayer = new SymbolLayer(markerLayerId, markerSourceId);

        // ?????? GeoJsonSource???DJIMarker
        GeoJsonSource markerSource = new GeoJsonSource(markerSourceId);
        if (style.isFullyLoaded()) {
            style.addSource(markerSource);
        }
        DJIMarker djiMarker = new MSymbolLayerMarker(this, mapboxMap, markerSource, markerSymbolLayer, shadowMarker, context, markerOptions);
        djiMarker.setPosition(latLng);
        djiMarker.setRotation(markerOptions.getRotation());

        // ??????bitmapId????????????icon
        DJIBitmapDescriptor iconDescriptor = markerOptions.getIcon();
        if (iconDescriptor != null) {
            String markerBitmapId = genMarkerBitmapId();
            iconDescriptor.setId(markerBitmapId);
            djiMarker.setIcon(iconDescriptor);
            markerBitmaps.add(markerBitmapId);
        }

        // ??????title
        djiMarker.setTitle(markerOptions.getTitle());

        djiMarker.setDraggable(markerOptions.getDraggable());

        djiMarker.setAnchor(markerOptions.getAnchorU(), markerOptions.getAnchorV());

        // ??????zIndex????????????zIndex??????SymbolLayer
        addLayerByZIndex(markerOptions.getZIndex(), markerSymbolLayer);

        // ???shadowMarker????????????marker??????
        markers.put(shadowMarker, djiMarker);

        return djiMarker;
    }

    @Override
    public Object getMap() {
        return mapboxMap;
    }

    private Bitmap createTransparentBitmap(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap transparentBitmap = Bitmap.createBitmap(width, height, src.getConfig());
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                transparentBitmap.setPixel(x, y, Color.TRANSPARENT);
            }
        }
        return transparentBitmap;
    }

    /**
     * ??????zindex????????????layer
     *
     * @param zindex ????????????layer???zindex
     * @param layer  ????????????layer
     */
    public void addLayerByZIndex(final long zindex, final Layer layer) {

        LayerZIndex waitingLayerZIndex = new LayerZIndex(layer, zindex);
        if (0 == sortedLayersByZIndex.size()) {
            addDummySourcesAndLayers();
        }
        int size = sortedLayersByZIndex.size();

        for (int i = 0; i < size; i++) {
            final LayerZIndex layerZIndex = sortedLayersByZIndex.get(i);
            final LayerZIndex nextLayerZIndex = sortedLayersByZIndex.get(i + 1);
            if (zindex == layerZIndex.zindex
                    || (zindex > layerZIndex.zindex && zindex < nextLayerZIndex.zindex)) {
                Log.d("joe-line-layer", "layer id=" + layer.getId() + ", zindex=" + zindex + ", cur layer id=" + layerZIndex.layer.getId() + ", zindex=" + layerZIndex.zindex);
                if (style.isFullyLoaded()) {
                    style.addLayerAbove(layer, layerZIndex.layer.getId());
                }
                sortedLayersByZIndex.add(i + 1, waitingLayerZIndex);
                break;
            }
        }
    }

    /**
     * ??????zindex?????????layer
     *
     * @param zindex ??????zindex
     * @param layer  ????????????layer
     */
    public void updateLayerByZIndex(final int zindex, final Layer layer) {
        int size = sortedLayersByZIndex.size();

        Log.d("joe-layer", "updateLayerByZIndex cur layer id " + layer.getId());
        for (int i = 0; i < size; i++) {
            final LayerZIndex layerZIndex = sortedLayersByZIndex.get(i);
            Log.d("joe-layer", "updateLayerByZIndex layer id " + layerZIndex.layer.getId());
            if (layer == layerZIndex.layer) {
                if (style.isFullyLoaded()) {
                    style.removeLayer(layer);
                }
                sortedLayersByZIndex.remove(layerZIndex);
                break;
            }
        }
        addLayerByZIndex(zindex, layer);
    }

    private void removeZIndexById(String id) {
        for (LayerZIndex zIndex : sortedLayersByZIndex) {
            if (zIndex.layer.getId().equals(id)) {
                sortedLayersByZIndex.remove(zIndex);
                break;
            }
        }
    }

    /**
     * ???layer??????????????????????????????????????????Count??????
     *
     * @return
     */
    public String genMarkerLayerId() {
        String markerLayerId = MARKER_LAYER_ID_PREFIX + markerLayersCount;
        markerLayersCount++;
        return markerLayerId;
    }

    public String genMarkerSourceId() {
        String markerSourceId = MARKER_SOURCE_ID_PREFIX + markerSourcesCount;
        markerSourcesCount++;
        return markerSourceId;
    }

    public String genMarkerBitmapId() {
        String markerBitmapId = MARKER_BITMAP_ID_PREFIX + markerBitmapsCount;
        markerBitmapsCount++;
        return markerBitmapId;
    }

    public String genLineLayerId() {
        String lineLayerId = LINE_LAYER_ID_PREFIX + lineLayersCount;
        lineLayersCount++;
        return lineLayerId;
    }

    public String genLineSourceId() {
        String lineSourceId = LINE_SOURCE_ID_PREFIX + lineSourceCount;
        lineSourceCount++;
        return lineSourceId;
    }

    public String genSingleCircleLayerId() {
        String singleCircleLayerId = SINGLE_CIRCLE_LAYER_ID_PREFIX + singleCircleLayersCount;
        singleCircleLayersCount++;
        return singleCircleLayerId;
    }

    public String genSingleCircleSourceId() {
        String singleCircleSourceId = SINGLE_CIRCLE_SOURCE_ID_PREFIX + singleCircleSourceCount;
        singleCircleSourceCount++;
        return singleCircleSourceId;
    }

    public String genGroupCircleLayerId() {
        String groupCircleLayerId = GROUP_CIRCLE_LAYER_ID_PREFIX + groupCircleLayersCount;
        groupCircleLayersCount++;
        return groupCircleLayerId;
    }

    public String genGroupCircleSourceId() {
        String groupCircleSourceId = GROUP_CIRCLE_SOURCE_ID_PREFIX + groupCircleSourceCount;
        groupCircleSourceCount++;
        return groupCircleSourceId;
    }

    public String genMarkerCircleLayerId() {
        String markerCircleLayerId = MARKER_CIRCLE_LAYER_ID_PREFIX + markerCircleLayersCount;
        markerCircleLayersCount++;
        return markerCircleLayerId;
    }

    public String genMarkerCircleSourceId() {
        String markerCircleSourceId = MARKER_CIRCLE_SOURCE_ID_PREFIX + markerSourcesCount;
        markerSourcesCount++;
        return markerCircleSourceId;
    }

    @Override
    public DJICameraPosition getCameraPosition() {
        CameraPosition p = mapboxMap.getCameraPosition();
        return MaplibreUtils.fromCameraPosition(p);
    }

    @Override
    public void animateCamera(DJICameraUpdate cameraUpdate) {
        CameraUpdate update = MaplibreUtils.fromDJICameraUpdate(cameraUpdate);
        mapboxMap.animateCamera(update);
    }

    @Override
    public void onCameraMove() {
        final CameraPosition p = mapboxMap.getCameraPosition();
        DJICameraPosition cameraPosition = MaplibreUtils.fromCameraPosition(p);
        onCameraChange(cameraPosition);
    }

    @Override
    public void moveCamera(DJICameraUpdate cameraUpdate) {
        CameraUpdate update = MaplibreUtils.fromDJICameraUpdate(cameraUpdate);
        mapboxMap.moveCamera(update);
    }

    //  10/18/17 ?????????????????????
    @Override
    public void setInfoWindowAdapter(final InfoWindowAdapter adapter) {
        mapboxMap.setInfoWindowAdapter(marker -> {
            if (markers.containsKey(marker)) {
                final DJIMarker realMarker = markers.get(marker);
                return adapter.getInfoWindow(realMarker);
            }
            return null;
        });
    }

    //region for draggable marker
    //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

    private boolean isCatchTouch = false;
    private Marker currentSelectedMarker = null;

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        Projection projection = mapboxMap.getProjection();
        PointF longClickScreenPoint = projection.toScreenLocation(point);

        float minDistanceOfPixel = 60;
        float currentMinDistance = Float.MAX_VALUE;
        Marker candidateMarker = null;

        for ( Map.Entry<Marker, DJIMarker> entry : markers.entrySet()) {

            boolean isDraggable = markers.get(entry.getKey()).isDraggable();
            if (!isDraggable) {
                continue;
            }

            PointF markerPoint = projection.toScreenLocation(entry.getKey().getPosition());

            float xDis = Math.abs(markerPoint.x - longClickScreenPoint.x);
            float yDis = Math.abs(markerPoint.y - longClickScreenPoint.y);

            float distance = (float) Math.sqrt(xDis * xDis + yDis * yDis);

            if (distance < minDistanceOfPixel && distance < currentMinDistance) {
                candidateMarker = entry.getKey();
                isCatchTouch = true;
            }
        }
        if (isCatchTouch) {
            currentSelectedMarker = candidateMarker;
            onMarkerDragStart(markers.get(currentSelectedMarker));
            return true;
        }

        onMapLongClick(MaplibreUtils.fromLatLng(point));
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == MotionEvent.ACTION_UP) {

            if (isCatchTouch && currentSelectedMarker != null) {
                onMarkerDragEnd(markers.get(currentSelectedMarker));
            }

            isCatchTouch = false;
        }
        if (isCatchTouch && currentSelectedMarker != null) {
            if (action == MotionEvent.ACTION_MOVE) {
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                LatLng latLng = mapboxMap.getProjection().fromScreenLocation(new PointF(x, y));

                currentSelectedMarker.setPosition(latLng);
                DJIMarker djiMarker = markers.get(currentSelectedMarker);
                djiMarker.setPosition(MaplibreUtils.fromLatLng(latLng));
                onMarkerDrag(djiMarker);

            }
            return true;
        } else {
            return false;
        }
    }
    //endregion

    @Override
    public void setMapType(MapType type, OnMapTypeLoadedListener listener) {
        setMapType(type);
        onMapTypeLoadedListener = listener;
    }

    @Override
    public void setMapType(int type) {
        stoppingWorld = true;
        clearSourcesAndLayers();
        switch (type) {
            case DJIMap.MAP_TYPE_SATELLITE:
                mapboxMap.setStyle(Style.SATELLITE, this);
                break;
            case DJIMap.MAP_TYPE_HYBRID:
                mapboxMap.setStyle(Style.SATELLITE_STREETS, this);
                break;
            case DJIMap.MAP_TYPE_NORMAL:
            default:
                mapboxMap.setStyle(Style.MAPBOX_STREETS, this);
        }
    }

    @Override
    public void setMapType(MapType type) {
        stoppingWorld = true;
        clearSourcesAndLayers();
        switch (type) {
            case NORMAL:
                mapboxMap.setStyle(Style.MAPBOX_STREETS, this);
                break;
            case SATELLITE:
                mapboxMap.setStyle(Style.SATELLITE, this);
                break;
            case HYBRID:
                mapboxMap.setStyle(Style.SATELLITE_STREETS, this);
                break;
            default:
                mapboxMap.setStyle(Style.MAPBOX_STREETS, this);
        }
    }

    @Override
    public void onStyleLoaded(@NonNull Style style) {
        this.style = style;
        stoppingWorld = false;
        sortedLayersByZIndex.clear();
        updateState();
        if (onMapTypeLoadedListener != null) {
            onMapTypeLoadedListener.onMapTypeLoaded();
            onMapTypeLoadedListener = null;
        }
    }

    private void updateState() {
        addDummySourcesAndLayers();

        for (DJIMarker marker : markers.values()) {
            final MSymbolLayerMarker layer = (MSymbolLayerMarker) marker;
            layer.updateSourceLayer();
        }

        for (DJIPolyline polyline : polylines) {
            final MLineLayerPolyline layer = (MLineLayerPolyline) polyline;
            layer.updateSourceLayer();
        }
        for (DJICircle singleCircle : singleCircles) {
            final MCircle layer = (MCircle) singleCircle;
            layer.updateSourceLayer();
        }
        for (DJIGroupCircle groupCircle : groupCircles) {
            final MGroupCircle layer = (MGroupCircle) groupCircle;
            layer.updateSourceLayer();
        }
        for (DJICircle markerCircle : markerCircles) {
            final MMarkerCircle layer = (MMarkerCircle) markerCircle;
            layer.updateSourceLayer();
        }
    }

    @NonNull
    @Override
    public DJIPolyline addPolyline(DJIPolylineOptions options) {
        if (options.getPoints().size() == 1) {
            DJILatLng latLng = options.getPoints().get(0);
            options.getPoints().add(new DJILatLng(latLng.getLatitude(), latLng.getLongitude()));
        }
        String lineLayerId = genLineLayerId();
        String lineSourceId = genLineSourceId();
        LineLayer lineLayer = new LineLayer(lineLayerId, lineSourceId);
        GeoJsonSource lineSource = new GeoJsonSource(lineSourceId);
        if (style.isFullyLoaded()) {
            style.addSource(lineSource);
        }

        // MapBox ??????properties ?????????line???layer
        if (options.isDashed()) {
            lineLayer.setProperties(
                    PropertyFactory.lineDasharray(new Float[]{options.getDashLength(),
                            options.getDashLength()})
            );
        }

        DJIPolyline polyline = new MLineLayerPolyline(this, mapboxMap, lineLayer, lineSource, options);
        polyline.setPoints(options.getPoints());
        polyline.setColor(options.getColor());
        polyline.setWidth(options.getWidth());

        addLayerByZIndex((long) options.getZIndex(), lineLayer);
        polylines.add(polyline);

        return polyline;
    }

    /**
     * Because maxbox polygons can't set width, only one pixel. So drawing our own lines Witch can set width.
     *
     * @param options {@link DJIPolygonOptions}
     * @return
     */
    @Nullable
    @Override
    public DJIPolygon addPolygon(DJIPolygonOptions options) {
        Polygon polygon = mapboxMap.addPolygon(MaplibreUtils.fromDJIPolygonOptions(options));
        return new MPolygon(polygon, mapboxMap, options);
    }

    @Nullable
    @Override
    public DJICircle addMarkerCircle(DJICircleOptions options) {
        String markerCircleLayerId = genMarkerCircleLayerId();
        String markerCircleSourceId = genMarkerCircleSourceId();
        CircleLayer markerCircleLayer = new CircleLayer(markerCircleLayerId, markerCircleSourceId);
        GeoJsonSource markerCircleSource = new GeoJsonSource(markerCircleSourceId);
        if (style.isFullyLoaded()) {
            style.addSource(markerCircleSource);
        }

        DJICircle markerCircle = new MMarkerCircle(this, mapboxMap, markerCircleLayer, markerCircleSource, options);
        markerCircle.setVisible(true);
        markerCircle.setCircle(options.getCenter(), options.getRadius());
        markerCircle.setFillColor(options.getFillColor());
        markerCircle.setStrokeColor(options.getStrokeColor());


        addLayerByZIndex((long) options.getZIndex(), markerCircleLayer);
        markerCircles.add(markerCircle);

        return markerCircle;
    }

    @Nullable
    @Override
    public DJICircle addSingleCircle(DJICircleOptions options) {
        String singleCircleLayerId = genSingleCircleLayerId();
        String singleCircleSourceId = genSingleCircleSourceId();
        FillLayer singleCircleSymboLayer = new FillLayer(singleCircleLayerId, singleCircleSourceId);
        GeoJsonSource singleCircleSource = new GeoJsonSource(singleCircleSourceId);

        if (style.isFullyLoaded()) {
            style.addSource(singleCircleSource);
        }

        DJICircle singleCircle = new MCircle(this, mapboxMap, singleCircleSymboLayer, singleCircleSource, options);
        singleCircle.setVisible(true);
        singleCircle.setCircle(options.getCenter(), options.getRadius());
        singleCircle.setFillColor(options.getFillColor());
        singleCircle.setStrokeColor(options.getStrokeColor());


        addLayerByZIndex((long) options.getZIndex(), singleCircleSymboLayer);
        singleCircles.add(singleCircle);

        return singleCircle;
    }

    @Nullable
    @Override
    public DJIGroupCircle addGroupCircle(DJIGroupCircleOptions options) {
        if (options.getRadius().size() != options.getCenters().size() || options.getRadius().size() == 0) {
            return null;
        }
//        List<DJILatLng> centers = options.getCenters();
//        List<Double> radius = options.getRadius();
//        int size = centers.size();

        String groupCircleLayerId = genGroupCircleLayerId();
        String groupCircleSourceId = genGroupCircleSourceId();
        FillLayer groupCircleSymboLayer = new FillLayer(groupCircleLayerId, groupCircleSourceId);
        GeoJsonSource groupCircleSource = new GeoJsonSource(groupCircleSourceId);

        if (style.isFullyLoaded()) {
            style.addSource(groupCircleSource);
        }
        DJIGroupCircle groupCircle = new MGroupCircle(this, mapboxMap, groupCircleSymboLayer, groupCircleSource, options);
        groupCircle.setCircles(options.getCenters(), options.getRadius());
        groupCircle.setFillColor(options.getFillColor());
        groupCircle.setStrokeColor(options.getStrokeColor());
        groupCircle.setVisible(true);

        addLayerByZIndex((long) options.getZIndex(), groupCircleSymboLayer);
        groupCircles.add(groupCircle);

        return groupCircle;
    }


    @Override
    public DJIUiSettings getUiSettings() {
        return new MUiSettings(mapboxMap.getUiSettings());
    }

    @Override
    public void snapshot(final MapScreenShotListener callback) {
        mapboxMap.snapshot(snapshot -> callback.onMapScreenShot(snapshot));
    }

    @Override
    public DJIProjection getProjection() {
        return new MProjection(mapboxMap.getProjection());
    }

    @Override
    public void clear() {
        removeAllOnCameraChangeListeners();
        removeAllOnMarkerClickListener();
        mapboxMap.clear();
        mapboxMap.setInfoWindowAdapter(null);
    }

    @Override
    public boolean onInfoWindowClick(@NonNull Marker marker) {
        if (markers.containsKey(marker)) {
            final DJIMarker realMarker = markers.get(marker);
            onInfoWindowClick(realMarker);
        }
        return false;
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
//        DJILatLng latLng = new DJILatLng(point);
//        DJILatLng latLng = new DJILatLng(point.getLatitude(), point.getLongitude(), point.getAltitude());
        DJILatLng latLng = MaplibreUtils.fromLatLng(point);
        onMapClick(latLng);
        return true;
    }

    /**
     * ??????Marker???clicklistener
     *
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        if (markers.containsKey(marker)) {
            final DJIMarker realMarker = markers.get(marker);
            onMarkerClick(realMarker);
        }
        return true;
    }

    public void onMarkerRemove(Marker marker) {
        if (markers.containsKey(marker) && style.isFullyLoaded()) {
            final DJIMarker djiMarker = markers.get(marker);
            // ??????Marker?????????Layer???Source
            if (djiMarker instanceof MSymbolLayerMarker) {
                MSymbolLayerMarker symbolLayerMarker = (MSymbolLayerMarker) djiMarker;
                Layer removeLayer = style.getLayer(symbolLayerMarker.getLayerId());
                if (removeLayer != null) {
                    style.removeLayer(removeLayer);
                    removeZIndexById(removeLayer.getId());
                }
                style.removeSource(symbolLayerMarker.getSourceId());
                symbolLayerMarker.setSource(null);
                symbolLayerMarker.setSymbolLayer(null);
            }
            markers.remove(marker);
        }
    }

    public void onPolylineRemove(final DJIPolyline polyline) {
        if (polylines.contains(polyline) && style.isFullyLoaded()) {
            if (polyline instanceof MLineLayerPolyline) {
                MLineLayerPolyline lineLayerPolyline = (MLineLayerPolyline) polyline;
                style.removeSource(lineLayerPolyline.getSourceId());
                Layer removeLayer = style.getLayer(lineLayerPolyline.getLayerId());
                if (removeLayer != null) {
                    style.removeLayer(removeLayer);
                    removeZIndexById(removeLayer.getId());
                }
            }
            polylines.remove(polyline);
        }
    }

    public void onSingleCircleRemove(final DJICircle djiSingleCircle) {
        if (singleCircles.contains(djiSingleCircle) && style.isFullyLoaded()) {
            if (djiSingleCircle instanceof MCircle) {
                MCircle singleCircle = (MCircle) djiSingleCircle;
                style.removeSource(singleCircle.getSourceId());
                Layer removeLayer = style.getLayer(singleCircle.getLayerId());
                if (removeLayer != null) {
                    style.removeLayer(removeLayer);
                    removeZIndexById(removeLayer.getId());
                }
            }
            singleCircles.remove(djiSingleCircle);
        }
    }

    public void onGroupCircleRemove(final DJIGroupCircle djiGroupCircle) {
        if (groupCircles.contains(djiGroupCircle) && style.isFullyLoaded()) {
            if (djiGroupCircle instanceof MGroupCircle) {
                MGroupCircle groupCircle = (MGroupCircle) djiGroupCircle;
                style.removeSource(groupCircle.getSourceId());
                Layer removeLayer = style.getLayer(groupCircle.getLayerId());
                if (removeLayer != null) {
                    style.removeLayer(removeLayer);
                    removeZIndexById(removeLayer.getId());
                }
            }
            groupCircles.remove(djiGroupCircle);
        }
    }

    public void onMarkerCircleRemove(final DJICircle djiMarkerCircle) {
        if (markerCircles.contains(djiMarkerCircle) && style.isFullyLoaded()) {
            if (djiMarkerCircle instanceof MMarkerCircle) {
                MMarkerCircle markerCircle = (MMarkerCircle) djiMarkerCircle;
                style.removeSource(markerCircle.getSourceID());
                Layer removeLayer = style.getLayer(markerCircle.getLayerId());
                if (removeLayer != null) {
                    style.removeLayer(removeLayer);
                    removeZIndexById(removeLayer.getId());
                }
            }
            markerCircles.remove(djiMarkerCircle);
        }
    }

    public boolean isStoppingWorld() {
        return stoppingWorld;
    }

    //@Override
    //public void setOnCameraChangeListener(OnCameraChangeListener listener) {
    //    if (onCameraChangeListeners.contains(listener)) {
    //        return;
    //    }
    //    onCameraChangeListeners.add(listener);
    //    //mMap.addTransformListener(onTransformListener);
    //    // need confirm by Joe
    //}
}

package com.br.maputil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.br.commonutils.data.common.Location;
import com.br.commonutils.validator.Validator;
import com.br.maputil.data.MapMarker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class Mapper {

    private static Mapper mapper;
    private Context context;

    private Mapper(Context context) {
        this.context = context;
    }

    public static void init(@NonNull Context context) {
        mapper = new Mapper(context);
    }

    public static Mapper getInstance() throws IllegalAccessException {
        if (!Validator.isValid(mapper))
            throw new IllegalAccessException("Call init()");

        return mapper;
    }

    public MarkerOptions getNavigationMarker(@NonNull MapMarker mapMarker, boolean draggable) {
        Location location = mapMarker.getLocation();
        Bitmap bitmap = getFromView(R.drawable.ic_navigation);

        return new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title(mapMarker.getTitle()).snippet(mapMarker.getSnippet()).icon(BitmapDescriptorFactory.fromBitmap(bitmap)).draggable(draggable).anchor(0.5f, 0.5f).flat(true);
    }

    public MarkerOptions getMarker(@NonNull MapMarker mapMarker, boolean draggable) {
        Location location = mapMarker.getLocation();

        return new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title(mapMarker.getTitle()).snippet(mapMarker.getSnippet()).draggable(draggable).anchor(0.5f, 0.5f).flat(true);
    }

    public MarkerOptions getMarker(@NonNull MapMarker mapMarker, boolean draggable, @NonNull Bitmap bitmap) {
        Location location = mapMarker.getLocation();

        return new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title(mapMarker.getTitle()).snippet(mapMarker.getSnippet()).icon(BitmapDescriptorFactory.fromBitmap(bitmap)).draggable(draggable).anchor(0.5f, 0.5f).flat(true);
    }

    public MarkerOptions getMarker(@NonNull MapMarker mapMarker, boolean draggable, @DrawableRes int markerIcon) {
        Location location = mapMarker.getLocation();
        Bitmap bitmap = getFromView(markerIcon);

        return new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title(mapMarker.getTitle()).snippet(mapMarker.getSnippet()).icon(BitmapDescriptorFactory.fromBitmap(bitmap)).draggable(draggable).anchor(0.5f, 0.5f).flat(true);
    }

    public void clear(@NonNull Marker marker) {
        if (Validator.isValid(marker))
            marker.remove();
    }

    public void animate(@NonNull GoogleMap googleMap, @NonNull Location location, int zoomLevel, int tiltAngle) {
        if (Validator.isValid(googleMap) && Validator.isValid(location)) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).tilt(tiltAngle).zoom(zoomLevel).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

//            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoomLevel));
        }
    }

    public void moveMarker(@NonNull final GoogleMap googleMap, @NonNull final Marker marker, @NonNull final Location location) {
        if (Validator.isValid(googleMap) && Validator.isValid(marker) && Validator.isValid(location)) {
            final boolean hideMarker = false;
            final long start = SystemClock.uptimeMillis();
            final long duration = 1000;

            Projection projection = googleMap.getProjection();
            Point startPoint = projection.toScreenLocation(marker.getPosition());
            final LatLng startLatLng = projection.fromScreenLocation(startPoint);

            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = new LinearInterpolator().getInterpolation((float) elapsed / duration);
                    double longitude = t * location.getLongitude() + (1 - t) * startLatLng.longitude;
                    double latitude = t * location.getLatitude() + (1 - t) * startLatLng.latitude;

                    marker.setPosition(new LatLng(latitude, longitude));

                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    } else {
                        if (hideMarker)
                            marker.setVisible(false);
                        else
                            marker.setVisible(true);
                    }
                }
            });
        }
    }

    public void moveAndAnimate(@NonNull GoogleMap googleMap, @NonNull Marker marker, @NonNull Location location, int zoomLevel, int tiltAngle) {
        moveMarker(googleMap, marker, location);
        animate(googleMap, location, zoomLevel, tiltAngle);
    }

    public void setZoomLevel(@NonNull GoogleMap googleMap, @NonNull List<Marker> markerList, int padding) {
        if (Validator.isValid(markerList) && !markerList.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i = 0; i < markerList.size(); i++) {
                builder.include(markerList.get(i).getPosition());
            }

            LatLngBounds bounds = builder.build();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        }
    }

    public Bitmap getFromView(@DrawableRes int resId) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_map_marker, null);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();

        ImageView imageView = view.findViewById(R.id.mapMarker_imageView);
        imageView.setImageResource(resId);

        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);

        Drawable drawable = view.getBackground();
        if (Validator.isValid(drawable))
            drawable.draw(canvas);

        view.draw(canvas);

        return bitmap;
    }
}

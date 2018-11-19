package com.br.maputil.data;

import com.br.commonutils.data.common.Location;

import java.io.Serializable;

public class MapMarker implements Serializable {

    private String title;
    private String snippet;
    private Location location;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}

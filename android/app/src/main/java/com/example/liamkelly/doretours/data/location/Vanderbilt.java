package com.example.liamkelly.doretours.data.location;

public class Vanderbilt {

    private static class Vertex {
        private double y, x;

        private Vertex(double latitude, double longitude) {
            this.x = latitude;
            this.y = longitude;
        }
    }

    // started at 21st and west end, only went up to
    // NOTE: This must be in connected ordering
    private static final Vertex[] VERTICES = {
            new Vertex(36.150381, -86.80139),
            new Vertex(36.148129, -86.799406),
            new Vertex(36.143651, -86.799972),// a little past edgehill on 21st
            new Vertex(36.144145, -86.806319),
            new Vertex(36.146907, -86.808347)
    };

    /**
     * This method computes whether a point is in Vanderbilt (a 2D polygon) based on an RPI professor's
     * algorithm. It essentially calculates the number of edge crossings, (odd = inside, even = outside).
     * @param latitude the point's latitude
     * @param longitude the point's longitude
     * @return whether the point is within Vanderbilt (defined by hard coded GPS vertices)
     */
    public static boolean inVanderbilt(double latitude, double longitude) {
        int i, j;
        double x = latitude, y = longitude;
        boolean inside = false;
        for(i = 0, j = VERTICES.length - 1; i < VERTICES.length; j = i++) {
            if( (VERTICES[i].y > y) != (VERTICES[j].y > y) &&
            (x < (VERTICES[j].x - VERTICES[i].x) * (y - VERTICES[i].y) / (VERTICES[j].y - VERTICES[i].y) + VERTICES[i].x)) {
                inside = !inside;
            }
        }
        return inside;
    }
}

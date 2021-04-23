/*
 * Copyright (C) 2021 Benjamin Alexander Albert
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class Contour {
    
    public static final int    CENTROID_RADIUS   = 5;
    public static final int    LINE_THICKNESS    = 1;
    public static final Scalar DEFAULT_COLOR     = new Scalar(  0, 255,   0);
    public static final Scalar CONVEX_HULL_COLOR = new Scalar(255,   0,   0);
    public static final Scalar CENTROID_COLOR    = new Scalar(  0,   0, 255);

    public final MatOfPoint             edges;
    public final Moments                moments;
    public final Point                  centroid;
    public final double                 perimeter;
    public final double                 area;
    public final ArrayList<MatOfPoint>  convexHull;
    public final double                 convexHullPerimeter;
    public final double                 convexHullArea;

    public Contour(MatOfPoint edges) {
        this.edges               = edges;
        this.moments             = Imgproc.moments(this.edges);
        this.centroid            = new Point(moments.get_m10() / moments.get_m00(),
                                             moments.get_m01() / moments.get_m00());
        this.perimeter           = Imgproc.arcLength(new MatOfPoint2f(edges.toArray()), true);
        this.area                = Imgproc.contourArea(this.edges);
        this.convexHull          = calculateConvexHull();
        this.convexHullPerimeter = Imgproc.arcLength(new MatOfPoint2f(convexHull.get(0).toArray()), true);
        this.convexHullArea      = Imgproc.contourArea(convexHull.get(0));
    }
    
    public Contour scale(final double scaleFactor){
        return new Contour(new MatOfPoint(this.edges.toList().stream().map(p ->
                new Point(p.x * scaleFactor, p.y * scaleFactor)).toArray(Point[]::new)));
    }
    
    private ArrayList<MatOfPoint> calculateConvexHull() {
        final List<MatOfPoint> edgesList = Arrays.asList(edges);
        final ArrayList<MatOfInt> hull = new ArrayList<>();
        edgesList.stream().forEach(point -> {
            MatOfInt matOfInt = new MatOfInt();
            Imgproc.convexHull(point, matOfInt);
            hull.add(matOfInt);
        });

        ArrayList<MatOfPoint> convexHull = new ArrayList<>();
        for (int x = 0; x < hull.size(); x++) {
            Point[] points = new Point[hull.get(x).rows()];
            for (int y = 0; y < hull.get(x).rows(); y++) {
                int index = (int) hull.get(x).get(y, 0)[0];
                points[y] = new Point(edgesList.get(x).get(index, 0)[0], edgesList.get(x).get(index, 0)[1]);
            }
            MatOfPoint matOfPoint = new MatOfPoint();
            matOfPoint.fromArray(points);
            convexHull.add(matOfPoint);
        }
        return convexHull;
    }
    
    public void draw(final Mat mat) {
        Imgproc.drawContours(mat, Arrays.asList(this.edges), 0, DEFAULT_COLOR, 2);
    }
    
    public void drawConvexHull(final Mat mat){
        Imgproc.drawContours(mat, this.convexHull, 0, CONVEX_HULL_COLOR, 1);
    }
    
    public void drawCentroid(final Mat mat){
        Imgproc.circle(mat, this.centroid, 5, CENTROID_COLOR, -1);
    }
}
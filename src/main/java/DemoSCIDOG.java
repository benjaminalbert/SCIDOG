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

import java.io.File;
import org.opencv.core.Core;
import org.opencv.core.Mat;

public class DemoSCIDOG {
    
    public static void main(String[] args) {
        nu.pattern.OpenCV.loadShared();
        final Mat melanoma = Imager.read(new File(DemoSCIDOG.class.getResource("melanoma.jpg").getPath()));
        final Mat naevus   = Imager.read(new File(DemoSCIDOG.class.getResource("naevus.jpg"  ).getPath()));
        
        final Contour melanomaContour = Segmenter.SCIDOG(melanoma);
        final Contour naevusContour = Segmenter.SCIDOG(naevus);
        
        /* draw green contour, blue convex hull, red centroid */
        
        melanomaContour.draw(melanoma);
        melanomaContour.drawConvexHull(melanoma);
        melanomaContour.drawCentroid(melanoma);
        naevusContour.draw(naevus);
        naevusContour.drawConvexHull(naevus);
        naevusContour.drawCentroid(naevus);
        
        final Streamer melanomaStreamer = new Streamer();
        final Streamer naevusStreamer   = new Streamer();
        melanomaStreamer.initializeFrame(0, 0, melanoma.cols(), melanoma.rows());
        naevusStreamer.initializeFrame(melanoma.cols(), 0, naevus.cols(), naevus.rows());
        melanomaStreamer.setImage(melanoma);
        naevusStreamer.setImage(naevus);
        melanomaStreamer.setTitle("SCIDOG segmentation - Melanoma");
        naevusStreamer.setTitle("SCIDOG segmentation - Naevus");
    }
}
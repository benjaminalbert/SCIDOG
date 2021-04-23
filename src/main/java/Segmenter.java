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
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Segmenter {
    
    public static Scalar contrastMultiplier(final Mat mat) {
        final double[][] channelValues = Imager.channelValues(mat);
        final double[] median = Stats.medians(channelValues);
        double[] m = new double[median.length];
        for (int x = 0; x < m.length; x++) {
                double q = 255 / median[x];
                m[x] = Math.pow(q, Math.pow(q, -q) + 1);
        }
        return new Scalar(m);
    }

    /* Synthesis and Convergence of Intermediate Decaying Omnigradients */
    public static Contour SCIDOG(final Mat src) {
        try {
            final double scaleFactor = Imager.scaleFactor(src.cols(), src.rows(), 512, 512);
            final Mat multiplied = (scaleFactor < 1) ? Imager.scale(src, scaleFactor) : src.clone();
            final Scalar multiplier = contrastMultiplier(multiplied);
            Core.multiply(multiplied, multiplier, multiplied);

            final double deltaAverageThreshold = 0.995;
            double previousAverage;
            double currentAverage = -1;
            int iteration = 1;

            final ArrayList<Mat> merges = new ArrayList<>();

            final Mat mat = new Mat();
            final Mat gray = new Mat();

            do {
                Imgproc.medianBlur(multiplied, mat, 2 * iteration + 1);
                Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);

                final Mat[] mergeMats = new Mat[15];
                mergeMats[0] = Imager.scharr(gray);
                for (int x = 1; x < mergeMats.length; x++) {
                        mergeMats[x] = Imager.sobel(gray, 1, x * 2 + 3);
                }

                final Mat omnigradient = Imager.mean(mergeMats);

                for (final Mat merge : mergeMats){
                        merge.release();
                }

                previousAverage = currentAverage;
                currentAverage = Core.mean(omnigradient).val[0];

                Imgproc.medianBlur(omnigradient, omnigradient, 2 * iteration + 1);
                merges.add(omnigradient);
                
                /* ****************************************
                
                to not display omnigradient sequences,
                remove the streamer lines below
                
                **************************************** */
                
                Streamer streamer = new Streamer();
                streamer.initializeFrame(0, 0, src.cols(), src.rows());
                streamer.setImage(omnigradient);
                streamer.setTitle(String.valueOf(iteration));

                iteration++;

            } while (currentAverage / previousAverage < deltaAverageThreshold);

            final Mat finalMerge = Imager.mean(merges.toArray(new Mat[0]));
            Imgproc.medianBlur(finalMerge, finalMerge, (2 * iteration + 1));

            Core.inRange(finalMerge, Core.mean(finalMerge), new Scalar(255), finalMerge);

            final Contour contour = Imager.findLargestContour(finalMerge);

            multiplied.release();
            mat.release();
            gray.release();
            merges.forEach((merge) -> {
                    merge.release();
            });
            finalMerge.release();

            return (scaleFactor < 1) ? contour.scale(1 / scaleFactor) : contour;

        } catch (Exception e) {
                e.printStackTrace();
                return null;
        }
    }
}
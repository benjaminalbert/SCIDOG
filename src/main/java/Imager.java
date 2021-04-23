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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Imager {

    public static Mat read(File file) {
        return Imgcodecs.imread(file.getAbsolutePath());
    }

    public static void write(Mat mat, String filename) throws IOException {
        Imgcodecs.imwrite(filename, mat);
    }
    
    public static BufferedImage matToBufferedImage(final Mat mat) {
        int type = BufferedImage.TYPE_CUSTOM;
        if (mat.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (mat.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage bufferedImage = new BufferedImage(mat.width(), mat.height(), type);
        byte[] data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        mat.get(0, 0, data);
        return bufferedImage;
    }
    
    public static Mat mean(Mat... mats) {
        final Mat mean = mats[0].clone();
        final double weight = 1d / mats.length;
        Core.multiply(mean, new Scalar(weight), mean);
        for (int x = 1; x < mats.length; x++){
            Core.addWeighted(mean, 1, mats[x], weight, 0, mean);
        }
        return mean;
    }

    public static Mat scale(Mat mat, double factor) {
        Mat scaled = new Mat();
        Imgproc.resize(mat, scaled, new Size(mat.cols() * factor, mat.rows() * factor));
        return scaled;
    }
    
    public static Mat scale(Mat mat, int maxWidth, int maxHeight) {
        return Imager.scale(mat, scaleFactor(mat.cols(), mat.rows(), maxWidth, maxHeight));
    }
    
    public static double scaleFactor(final int width, final int height, final int maxWidth, final int maxHeight){
        final double scaleWidth = (double) maxWidth / width;
        final double scaleHeight = (double) maxHeight / height;
        final double scaleFactor = (scaleWidth < scaleHeight) ? scaleWidth : scaleHeight;
        return scaleFactor;
    }
    
    public static double[][] channelValues(final Mat mat){
        final ArrayList<Double>[] pixels = new ArrayList[mat.channels()];
        for (int c = 0; c < pixels.length; c++){
            pixels[c] = new ArrayList<>();
        }
        for (int y = 0; y < mat.rows(); y++){
            for (int x = 0; x < mat.cols(); x++){
                final double[] values = mat.get(y,x);
                for (int c = 0; c < values.length; c++){
                    pixels[c].add(values[c]);
                }
            }
        }
        final double[][] channelValues = new double[pixels.length][];
        for (int c = 0; c < channelValues.length; c++){
            channelValues[c] = pixels[c].stream().mapToDouble(Double::doubleValue).toArray();
        }
        return channelValues;
    }
    
    public static Contour findLargestContour(final Mat threshold) {
        final List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(threshold, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
        Collections.sort(contours, (c1, c2) -> Double.compare(Imgproc.contourArea(c2), Imgproc.contourArea(c1)));
        return new Contour(contours.get(0));
    }

    public static Mat sobel(Mat mat, int order, int kSize) {
        Mat sobelX = new Mat();
        Mat sobelY = new Mat();
        Imgproc.Sobel(mat, sobelX, CvType.CV_16S, order, 0, kSize, 1, 0);
        Imgproc.Sobel(mat, sobelY, CvType.CV_16S, 0, order, kSize, 1, 0);
        Core.convertScaleAbs(sobelX, sobelX);
        Core.convertScaleAbs(sobelY, sobelY);
        Mat sobel = new Mat();
        Core.addWeighted(sobelX, 0.5, sobelY, 0.5, 0, sobel);
        return sobel;
    }

    public static Mat scharr(Mat mat) {
        return sobel(mat, 1, Imgproc.CV_SCHARR);
    }
}
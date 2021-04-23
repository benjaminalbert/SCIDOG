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

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Stats {
    
    public static double[][] transpose(final double[]... data){
        final double[][] transpose = new double[data[0].length][data.length];
        for (int y = 0; y < data.length; y++){
            for(int x = 0; x < data[y].length; x++){
                transpose[x][y] = data[y][x];
            }
        }
        return transpose;
    }
    
    public static double[] medians(final double[]... data){
        return transpose(percentiles(data, 50))[0];
    }
    
    public static double[][] percentiles(final double[][] data, final int... percentiles){
        return percentiles(data, IntStream.of(percentiles).mapToDouble(i -> i / 100d).toArray());
    }
    
    public static double[][] percentiles(final double[][] data, final double... percentiles){
        return Stream.of(data).parallel().map(vec -> percentilesFromSorted(DoubleStream.of(vec)
                .parallel().sorted().toArray(), percentiles)).toArray(double[][]::new);
    }
    
    private static double[] percentilesFromSorted(final double[] sorted, final double... percentages){
        return DoubleStream.of(percentages).parallel().map(p -> {
            if (p < 0 || p > 1){
                return Double.NaN;
            } else  if (p == 0){
                return sorted[0];
            } else{
                final double index = sorted.length * p - 1;
                final int    intIndex = (int) index;
                final double decIndex = index - intIndex;
                final double tolerance = 1e-5;
                /* if the index is approximately an int */
                if (decIndex < tolerance){
                    return sorted[intIndex];
                /* else return a weighted average of the values on either side of the double index
                 * the weights for these values is a linear proportion of the distance between 
                 * their integer index to the double index. Therefore, the weights add to 1 and
                 * a double index with decimal part 0.5 yield equivalent weights */
                } else{
                    return sorted[intIndex] * (1 - decIndex) + sorted[intIndex + 1] * decIndex;
                }
            }
        }).toArray();
    }
}
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.opencv.core.Mat;

public class Streamer extends JFrame {
    
    public static void show(final Mat mat){
        Streamer.show(Imager.matToBufferedImage(mat));
    }
    
    public static void show(final BufferedImage bufferedImage){
        final Streamer streamer = new Streamer();
        streamer.initializeFrame(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
        streamer.setImage(bufferedImage);
        streamer.setVisible(true);
    }

    private final ImagePanel imagePanel = new ImagePanel();

    public Streamer() {
        super();
    }
    
    public void setImage(final Mat mat){
        setImage(Imager.matToBufferedImage(mat));
    }
    
    public void setImage(final BufferedImage bufferedImage){
        this.imagePanel.setImage(bufferedImage);
        this.getContentPane().setPreferredSize(new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight()));
        this.pack();
    }
    
    public void initializeFrame(final int x, final int y, final int width, final int height) {
        this.getContentPane().add(imagePanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(width, height);
        this.setLocation(x, y);
        this.setVisible(true);
    }

    private class ImagePanel extends JPanel {

        private BufferedImage image;

        @Override
        public void paint(final Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            g.drawImage(image, 0, 0, this);
        }

        public void setImage(final BufferedImage image) {
            this.image = image;
        }
    }
}
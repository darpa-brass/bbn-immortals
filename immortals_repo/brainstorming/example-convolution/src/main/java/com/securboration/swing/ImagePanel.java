package com.securboration.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * ImagePanel extends swing.JPanel to support drawing of image
 * 
 * @author jstaples
 */
public class ImagePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private BufferedImage image;
    private final int WIDTH_LIMIT = 512;
    private final int HEIGHT_LIMIT = 512;

    /**
     * constructor
     */
    public ImagePanel() {
        image = null;
    }

    /**
     *
     * @param g
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            int h = image.getHeight();
            int w = image.getWidth();
            if (h > HEIGHT_LIMIT)
                h = HEIGHT_LIMIT;
            if (w > WIDTH_LIMIT)
                w = WIDTH_LIMIT;

            g.drawImage(image, 0, 0, w, h, Color.LIGHT_GRAY, null);
        }
    }

    /**
     * setImage : set the image that is drawn
     * 
     * @param bi
     *            : Buffered Image
     */
    public void setImage(BufferedImage bi) {
        this.image = bi;
        this.repaint();
    }

}

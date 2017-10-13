package com.securboration.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.securboration.demo.ImageProcessingBenchmark;

/**
 * 
 * @author jstaples
 *
 */
public class ImageProcessingKernelPanel extends JFrame {
    private static final long serialVersionUID = 1L;
    private static int framePositionOffset = 0;

    private static ImageProcessingBenchmark benchmark = null;

    public static void runBenchmark() throws IOException {
        if (benchmark == null) {
            benchmark = new ImageProcessingBenchmark(originalPanel,
                    transformedPanel, statistics);

            benchmark.launchBenchmarkThread();

            kernelSelector = new KernelSelector(benchmark);
            setOffset(kernelSelector);
            kernelSelector.setTitle("kernel selection");
            kernelSelector.setVisible(true);
        }
    }

    private static ImagePanel originalPanel = null;
    private static ImagePanel transformedPanel = null;
    private static ImageProcessingKernelPanel original = null;
    private static ImageProcessingKernelPanel transformed = null;
    private static KernelSelector kernelSelector = null;

    private static ImageProcessingKernelPanel performance = null;
    private static StatisticsGraph statistics = null;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());

                originalPanel = new ImagePanel();
                original = new ImageProcessingKernelPanel(originalPanel);
                original.setResizable(false);

                transformedPanel = new ImagePanel();
                transformed = new ImageProcessingKernelPanel(transformedPanel);
                transformed.setResizable(false);

                statistics = new StatisticsGraph("performance monitoring");
                performance = new ImageProcessingKernelPanel(statistics);

                original.setTitle("original image");
                original.setVisible(true);
                transformed.setTitle("processed image");
                transformed.setVisible(true);
                performance.setTitle("kernel performance metrics");
                performance.setVisible(true);

                runBenchmark();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void setOffset(Component c) {
        framePositionOffset += 150;

        c.setBounds(framePositionOffset, framePositionOffset, c.getWidth(),
                c.getHeight());
    }

    /**
     * Create the frame.
     */
    private ImageProcessingKernelPanel(ImagePanel panel) {
        framePositionOffset += 150;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(framePositionOffset, framePositionOffset, 512, 512);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        contentPane.add(panel);
    }

    /**
     * Create the frame.
     */
    private ImageProcessingKernelPanel(StatisticsGraph panel) {
        framePositionOffset += 150;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(framePositionOffset, framePositionOffset, 512, 512);

        panel.setVisible(true);
        setContentPane(panel);
    }

}

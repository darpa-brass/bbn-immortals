package com.securboration.demo;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.securboration.demo.kernel.DoNothingKernel;
import com.securboration.demo.kernel.IImageProcessingKernel;
import com.securboration.swing.ImagePanel;
import com.securboration.swing.StatisticsGraph;

/**
 * Launches four windows:
 * One selects the operation to perform on a set of input images
 * One shows the current input image being processed
 * One shows the output after processing the previous image 
 * One shows the performance of the operation over time
 * 
 * @author jstaples
 *
 */
public class ImageProcessingBenchmark {
    private final List<BufferedImage> imagesToProcess = new ArrayList<>();

    private final ImagePanel originalPanel;
    private final ImagePanel transformedPanel;
    private final StatisticsGraph graph;
    private final MetricSource metrics = new MetricSource();

    private Object lock = new Object();
    private IImageProcessingKernel kernel = new DoNothingKernel();

    public ImageProcessingBenchmark(ImagePanel original, ImagePanel transformed,
            StatisticsGraph statisticsGraph) throws IOException {
        this.originalPanel = original;
        this.transformedPanel = transformed;
        this.graph = statisticsGraph;

        for (InputStream image : getInputs()) {
            imagesToProcess.add(read(image));
        }
    }

    private Collection<InputStream> getInputs() {
        List<InputStream> streams = new ArrayList<>();
        boolean stop = false;
        int count = 1;
        int errorCount = 0;

        while (!stop) {
            InputStream is = this.getClass().getClassLoader()
                    .getResourceAsStream("frames/frame" + count + ".jpg");

            if (is == null) {
                errorCount++;
            } else {
                errorCount = 0;
                streams.add(is);
            }
            count++;

            // 5 or more errors means we should stop trying
            if (errorCount > 5) {
                stop = true;
            }

            // read at most 100 images into memory
            if (count > 100) {
                stop = true;
            }
        }

        return streams;
    }

    private class MetricSource {
        Timer t = new Timer(new ExponentiallyDecayingReservoir());

        MetricSource() {
        }

        Context start() {
            return t.time();
        }

        void reset() {
            t = new Timer(new ExponentiallyDecayingReservoir());
            graph.reset();
        }
    }

    public Thread launchBenchmarkThread() {
        Thread t = new Thread(() -> {

            while (true)// infinite loop
            {
                try {
                    for (BufferedImage image : imagesToProcess) {
                        updatePanel(originalPanel, image);

                        double elapsed = -System.currentTimeMillis();
                        BufferedImage processed;
                        synchronized (lock) {
                            Context c = metrics.start();
                            processed = kernel.process(image);
                            final long elapsedTimer = c.stop();

                            final long currentTime = System.currentTimeMillis();

                            Snapshot s = metrics.t.getSnapshot();

                            graph.receiveMetric("average fps", currentTime,
                                    (1d * 1000d * 1000d * 1000d) / s.getMean());

                            graph.receiveMetric("max fps", currentTime,
                                    (1d * 1000d * 1000d * 1000d) / s.getMin());

                            graph.receiveMetric("min fps", currentTime,
                                    (1d * 1000d * 1000d * 1000d) / s.getMax());

                            graph.receiveMetric("last fps", currentTime,
                                    (1d * 1000d * 1000d * 1000d)
                                            / elapsedTimer);
                        }

                        elapsed += System.currentTimeMillis();

                        elapsed = elapsed / 1000;

                        updatePanel(transformedPanel, processed);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ee) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

        });

        t.setName("benchmark thread");
        t.setDaemon(true);
        t.start();

        return t;
    }

    private void updatePanel(ImagePanel panel, BufferedImage image) {
        EventQueue.invokeLater(() -> {
            panel.setImage(image);
        });
    }

    private static BufferedImage read(InputStream image) throws IOException {
        return ImageIO.read(image);
    }

    public void setKernel(IImageProcessingKernel kernel) {
        synchronized (lock) {
            this.kernel = kernel;
            this.metrics.reset();
        }
    }

}

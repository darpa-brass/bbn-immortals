package com.securboration.swing;

import java.awt.Color;
import java.text.SimpleDateFormat;

import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * Swing frame that displays performance metrics for the kernels
 * 
 * @author jstaples
 *
 */
public class StatisticsGraph extends JInternalFrame {
    private static final long serialVersionUID = 1L;

    private static final long MAX_DATA_AGE = Long.MAX_VALUE;
    private static final int MAX_DATA_POINTS = 1000;

    protected final JFreeChart chart;
    protected final ChartPanel panel;
    protected final TimeSeriesCollection dataset;
    protected final String chartName;

    public StatisticsGraph(String chartName) {
        this.chartName = chartName;
        this.dataset = new TimeSeriesCollection();

        this.chart = createChart();

        this.panel = getChartPanel(this.chart);

        super.setContentPane(this.panel);

        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI())
                .setNorthPane(null);

        super.setBackground(Color.lightGray);
    }

    private ChartPanel getChartPanel(JFreeChart chart) {
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);

        panel.addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                if (event.getEntity() instanceof XYItemEntity) {
                    XYItemEntity e = (XYItemEntity) event.getEntity();

                    System.out.printf("\tselected series %d: %s\n",
                            e.getSeriesIndex(),
                            dataset.getSeries(e.getSeriesIndex()).getKey());
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent arg0) {
            }

        });

        return panel;
    }

    private JFreeChart createChart() {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(null, // title
                "time", // x-axis label
                "frames per second", // y-axis label
                dataset, // data
                true, // create legend?
                true, // generate tooltips?
                false // generate URLs?
        );

        chart.setBackgroundPaint(Color.lightGray);
        chart.getLegend().setBackgroundPaint(Color.lightGray);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(false);
            renderer.setBaseShapesFilled(false);
            renderer.setDrawSeriesLineAsPath(true);
        }

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));

        return chart;
    }

    public void reset() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < dataset.getSeriesCount(); i++) {
                    dataset.getSeries(i).clear();
                }
            }
        });
    }

    public void receiveMetric(final String tag, final long timestamp,
            final double value) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                TimeSeries s = dataset.getSeries(tag);

                if (s == null) {
                    s = new TimeSeries(tag);
                    s.setMaximumItemAge(MAX_DATA_AGE);
                    s.setMaximumItemCount(MAX_DATA_POINTS);
                    dataset.addSeries(s);
                }

                s.addOrUpdate(new FixedMillisecond(timestamp), value);
            }
        });
    }

}

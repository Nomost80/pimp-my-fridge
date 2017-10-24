package models.db;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;

public class TimeSeries_AWT extends ApplicationFrame {
    public TimeSeries_AWT( final String title, DB_ValuesSensors test ) {
        super( title );
        final XYDataset dataset = test.select_Series("2017-10-24 10:08:30", "2017-10-24 10:09:30");
        final JFreeChart chart = createChart( dataset );
        final ChartPanel chartPanel = new ChartPanel( chart );
        chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 370 ) );
        chartPanel.setMouseZoomable( true , false );
        final XYPlot plot = (XYPlot) chart.getPlot();
        final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseStroke(new BasicStroke(3));
        renderer.setSeriesStroke(0, new BasicStroke(3));
        renderer.setSeriesStroke(1, new BasicStroke(3));
        renderer.setSeriesStroke(2, new BasicStroke(3));
        renderer.setSeriesStroke(3, new BasicStroke(3));
        setContentPane( chartPanel );
    }

    public static void content(DB_ValuesSensors test) {
        final String title = "Time Series Management";
        final TimeSeries_AWT demo = new TimeSeries_AWT(title, test);
        demo.pack( );
        RefineryUtilities.positionFrameRandomly( demo );
        demo.setVisible( true );
    }

    private JFreeChart createChart( final XYDataset data ) {
        return ChartFactory.createTimeSeriesChart(
                "Computing Test",
                "Seconds",
                "Value",
                data,
                true,
                false,
                false);
    }
}

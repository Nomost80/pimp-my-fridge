package views;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;

class Graphe {
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private final String title;
    private final JFrame jFrame;
    private final String borderLayout;

    Graphe(final String name, final JFrame jFrame, String borderLayout) {
        this.title = name;
        this.jFrame = jFrame;
        this.borderLayout = borderLayout;
    }

    void updateGraphe(XYDataset dataset){
        if (this.chart == null)
            initializeGraphe(dataset);
        this.chart.getXYPlot().setDataset(dataset);
        this.chartPanel.repaint();
    }

    private void initializeGraphe(XYDataset dataset){
        this.chart = createChart(dataset);
        this.chartPanel = new ChartPanel(chart);
        this.chartPanel.setPreferredSize( new java.awt.Dimension( 336 , 222 ) ); // Initial width : 560     height : 370
        this.chartPanel.setMouseZoomable( true , false );
        final XYPlot plot = (XYPlot) chart.getPlot();
        final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseStroke(new BasicStroke(3));
        renderer.setSeriesStroke(0, new BasicStroke(3));
        renderer.setSeriesStroke(1, new BasicStroke(3));
        renderer.setSeriesStroke(2, new BasicStroke(3));
        renderer.setSeriesStroke(3, new BasicStroke(3));
        this.jFrame.add(this.chartPanel/*, this.borderLayout*/);
    //    setContentPane( chartPanel );
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

    JPanel getJPanel(){
        return this.chartPanel;
    }

 /*   public static void content(DB_ValuesSensors test) {
        final String title = "Time Series Management";
        final TimeSeries_AWT demo = new TimeSeries_AWT(title, test);
        demo.pack( );
        RefineryUtilities.positionFrameRandomly( demo );
        demo.setVisible( true );
    }*/
}

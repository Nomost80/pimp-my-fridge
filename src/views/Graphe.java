package views;

import javafx.scene.Group;
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
    private final JPanel jPanel;
    private final JFrame jFrame;

    Graphe(final String name, final JPanel jPanel, final JFrame jFrame) {
        this.title = name;
        this.jPanel = jPanel;
        this.jFrame = jFrame;
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
        System.out.println("Initialisation du graphe :)");
        addChartPanelToJPanel();
}

    private void addChartPanelToJPanel(){
        GroupLayout layout = (GroupLayout) this.jPanel.getLayout();
        layout.setHorizontalGroup(      // MAX : 0.6
                layout.createSequentialGroup()
                        .addComponent(this.chartPanel, 0, (int) Math.round(this.jFrame.getWidth() * 0.6), Short.MAX_VALUE)
        );
        layout.setVerticalGroup(      // MAX : 0.25
                layout.createSequentialGroup()
                        .addComponent(this.chartPanel, 0, (int) Math.round(this.jFrame.getHeight() * 0.25), Short.MAX_VALUE)
        );
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

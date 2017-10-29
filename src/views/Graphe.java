package views;

import javafx.scene.Group;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
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
    private final float widthFactor;
    private final float heightFactor;
    private XYPlot plot;
    private XYLineAndShapeRenderer renderer;

    Graphe(final String name, final JPanel jPanel, final JFrame jFrame, final float widthFactor, final float heightFactor) {
        this.title = name;
        this.jPanel = jPanel;
        this.jFrame = jFrame;
        this.widthFactor = widthFactor;
        this.heightFactor = heightFactor;
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
        this.chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 370 ) ); // Initial width : 560     height : 370
        this.chartPanel.setMouseZoomable( true , false );
        this.plot = (XYPlot) chart.getPlot();
        this.renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        this.renderer.setBaseStroke(new BasicStroke(3));
        addChartPanelToJPanel();
}

    void temperaturesSettings(){
        this.renderer.setSeriesStroke(0, new BasicStroke(3));
        this.renderer.setSeriesStroke(1, new BasicStroke(3));
        this.renderer.setSeriesStroke(2, new BasicStroke(3));
        this.renderer.setSeriesStroke(3, new BasicStroke(3));
        this.renderer.setSeriesStroke(4, new BasicStroke(3));
    //    NumberAxis domain = (NumberAxis) plot.getDomainAxis();
    //    domain.setRange(10.00, 32.00);
        //    domain.setTickUnit(new NumberTickUnit(1));
        //    domain.setVerticalTickLabels(true);
            NumberAxis range = (NumberAxis) plot.getRangeAxis();
            range.setRange(10.00, 32.00);
        //    range.setTickUnit(new NumberTickUnit(0.1));
    }

    void dampnessSettings(){
        this.renderer.setSeriesStroke(0, new BasicStroke(3));
    //    NumberAxis domain = (NumberAxis) plot.getDomainAxis();
    //    domain.setRange(30.00, 100.00);
        //    domain.setTickUnit(new NumberTickUnit(1));
        //    domain.setVerticalTickLabels(true);
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0.00, 100.00);
        //    range.setTickUnit(new NumberTickUnit(0.1));
    }



    private void addChartPanelToJPanel(){
        GroupLayout layout = (GroupLayout) this.jPanel.getLayout();
        layout.setHorizontalGroup(      // MAX : 0.6
                layout.createSequentialGroup()
                        .addComponent(this.chartPanel, 0, (int) Math.round(this.jFrame.getWidth() * this.widthFactor), Short.MAX_VALUE)
        );
        layout.setVerticalGroup(      // MAX : 0.25
                layout.createSequentialGroup()
                        .addComponent(this.chartPanel, 0, (int) Math.round(this.jFrame.getHeight() * this.heightFactor), Short.MAX_VALUE)
        );
    }

    private JFreeChart createChart(final XYDataset data) {
        return ChartFactory.createTimeSeriesChart(
                this.title,
                "Seconds",
                "Value",
                data,
                true,
                false,
                false);
    }
}

package views;

import javafx.scene.Group;
import models.Enum_AlarmStates;
import models.FridgeState;
import models.IQuery;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.Flow;
import java.util.logging.Level;
import java.util.logging.Logger;

public class View implements Flow.Subscriber<FridgeState> {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private IQuery publisher;
    private Thread thread;
    private volatile boolean threadActive;
    private static final Logger logger = Logger.getLogger("View");
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel panel_Titre;
    private JPanel panel_Graphes;
    private JPanel panel_Values;
    private Flow.Subscription subscription;
    private ArrayList<FridgeState> fridgeStates;
    private JSlider slider;
    private JButton button;
    private JLabel label;

    private Graphe graphTemperatures;
    private Graphe graphDampness;
    private JButton startButton;
    private JButton stopButton;

    public View(final String title) {
        this.fridgeStates = new ArrayList<>();

        this.buildFrameSettings(title);
        this.buildFrameContent();

        this.create_ThreadGraphes();
        this.start_ThreadGraphes();
        this.create_Graphes();

        this.setVisibility(true);
    }

    /*
    * Hiérarchie des panels :
    *       mainPanel contient :
    *               panel_Titre
    *               panel_Graphes
    *               panel_Values
    *
    * WARNING : il est nécessaire de créer les layouts en partant des conteneurs finaux pour remonter jusqu'au conteneur central mainPanel
    */

    private void buildFrameSettings(final String title){
        this.frame = new JFrame(title);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setLocationRelativeTo(null);
        this.setResizeable(true);
        this.setSize(800, 800);
    }

    private void buildFrameContent(){
        this.panel_Titre = buildTitlePanel();
        this.panel_Graphes = buildGraphesPanel();
        this.panel_Values = buildValuesPanel();
        this.mainPanel = buildMainPanel();
        this.frame.setContentPane(this.mainPanel);
    }

    private JPanel buildMainPanel(){
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        panel.setBackground(Color.BLACK);
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                    .addComponent(this.panel_Titre, 0, this.frame.getWidth(), Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(this.panel_Graphes, 0, (int) Math.round(this.frame.getWidth() * 0.6), Short.MAX_VALUE)
                        .addComponent(this.panel_Values, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                    )
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                    .addComponent(this.panel_Titre, 0, (int) Math.round(this.frame.getHeight() * 0.15), Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup()
                        .addComponent(this.panel_Graphes, 0, (int) Math.round(this.frame.getHeight() * 0.85), Short.MAX_VALUE)
                        .addComponent(this.panel_Values, 0, (int) Math.round(this.frame.getHeight() * 0.85), Short.MAX_VALUE)
                    )
        );
        return panel;
    }

    private JPanel buildOnePanel(Color color){
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        setAutoGapsGroupLayout(panel);
        panel.setBackground(color);
        return panel;
    }

    private JPanel buildTitlePanel(){
        JPanel panel = buildOnePanel(Color.BLUE);
        GroupLayout layout = (GroupLayout) panel.getLayout();
        // Horizontal : 1
        // Vertical : 0.15
        return panel;
    }

    private JPanel buildGraphesPanel(){
        JPanel panel = buildOnePanel(Color.RED);
        this.startButton = new JButton("Start");
        this.stopButton = new JButton("Stop");
        GroupLayout layout = (GroupLayout) panel.getLayout();
        layout.setHorizontalGroup(  // MAX : 0.6
                layout.createSequentialGroup()
                        .addComponent(this.startButton, 0, (int) Math.round(this.frame.getWidth() * 0.1), (int) Math.round(this.frame.getWidth() * 0.1))
                        .addComponent(this.stopButton, 0, (int) Math.round(this.frame.getWidth() * 0.1), (int) Math.round(this.frame.getWidth() * 0.1))
        );
        layout.setVerticalGroup(    // MAX : 0.85
                layout.createParallelGroup()
                        .addComponent(this.startButton, 0, (int) Math.round(this.frame.getHeight() * 0.04), (int) Math.round(this.frame.getHeight() * 0.04))
                        .addComponent(this.stopButton, 0, (int) Math.round(this.frame.getHeight() * 0.04), (int) Math.round(this.frame.getHeight() * 0.04))
        );
        return panel;
    }

    private JPanel buildValuesPanel(){
        JPanel panel = buildOnePanel(Color.YELLOW);
        this.slider = new JSlider(10, 30, 18);
        this.button = new JButton("Valider la nouvelle consigne");
        this.label = new JLabel();
        GroupLayout layout = (GroupLayout) panel.getLayout();
        layout.setHorizontalGroup(      // MAX : 0.4
                layout.createParallelGroup()
                        .addComponent(this.slider, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                        .addComponent(this.button, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                        .addComponent(this.label, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
        );
        layout.setVerticalGroup(        // MAX : 0.85
                layout.createSequentialGroup()
                        .addComponent(this.slider, 0, (int) Math.round(this.frame.getHeight() * 0.05), (int) Math.round(this.frame.getHeight() * 0.05))
                        .addComponent(this.button, 0, (int) Math.round(this.frame.getHeight() * 0.05), (int) Math.round(this.frame.getHeight() * 0.05))
                        .addComponent(this.label, 0, (int) Math.round(this.frame.getHeight() * 0.05), (int) Math.round(this.frame.getHeight() * 0.05))
        );
        return panel;
    }

    private void setAutoGapsGroupLayout(JPanel panel){
        GroupLayout layout = (GroupLayout) panel.getLayout();
        layout.setAutoCreateGaps(true);        // Ecart entre les éléments
        layout.setAutoCreateContainerGaps(true);   // Ecart entre les éléments et le conteneur
    }

    private void layoutSettings(){ // https://docs.oracle.com/javase/tutorial/uiswing/layout/group.html

      /*  this.layout.setHorizontalGroup(
                this.layout.createSequentialGroup()
                    .addGroup(this.layout.createParallelGroup()
                        .addComponent())
        );*/
    }

    private void create_Graphes(){
        this.graphTemperatures = new Graphe("Temperatures", this.frame, BorderLayout.WEST);
        this.graphDampness = new Graphe("Dampness", this.frame, BorderLayout.WEST);
    //    this.frame.add(this.graphTemperatures.getJPanel());
      //  this.labelGraphTemperatures.setText("Graphique");
    }

    private void create_ThreadGraphes(){
        this.thread = new Thread("Check Graphes") {
            public void run() {
                wait_FirstValue();
                check_graphes();
            }
        };
    }

    private void start_ThreadGraphes(){
        this.threadActive = true;
        this.thread.start();
    }

    private void stop_ThreadGraphes(){
        this.threadActive = false;
    }

    private void wait_FirstValue(){
        while (fridgeStates.size() == 0)
        {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private void check_graphes(){
        while (this.threadActive){
            LocalDateTime now = LocalDateTime.now();
            String strDateStart = now.minusHours(4).format(formatter);
            String strDateEnd = now.format(formatter);
//            System.out.println("Coucou :)");
//            System.out.println("Start : " + strDateStart);
//            System.out.println("End : " + strDateEnd);
            TimeSeriesCollection temperaturesCollection = this.publisher.select_TemperaturesSeries(fridgeStates.get(0), strDateStart, strDateEnd);
            TimeSeriesCollection dampnessCollection = this.publisher.select_DampnessSerie(fridgeStates.get(0), strDateStart, strDateEnd);
            repaintGraphe(temperaturesCollection, this.graphTemperatures);
            repaintGraphe(dampnessCollection, this.graphDampness);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private void repaintGraphe(TimeSeriesCollection collection, Graphe graphe){
        if (collection != null){
            graphe.updateGraphe(collection);
        }
    }

    public void setIQuery(IQuery publisher){
        this.publisher = publisher;
    }

    public Flow.Subscription getSubscription() {
        return subscription;
    }

    public JButton getButton() {
        return button;
    }

    public JSlider getSlider() {
        return slider;
    }

    public JButton getStopButton() {
        return stopButton;
    }

    public JButton getStartButton() {
        return startButton;
    }

    public void setSize(int width, int height) {
        this.frame.setSize(width, height);
    }

    public void setVisibility(boolean isVisible) {
        this.frame.setVisible(isVisible);
    }

    public void setResizeable(boolean isResizeable) {
        this.frame.setResizable(isResizeable);
    }

    private void updateDampnessAlarm(Enum_AlarmStates alarmState){
        System.out.println("AlarmState : " + alarmState.toString());
        switch (alarmState){
            case GOOD: break;
            case WARNING: break;
            case CRITICAL: break;
            default: break;
        }
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        System.out.println("On est abonné, on demande la 1ère valeur");
        /* It signals that the current Subscriber is ready to consume more messages. */
        subscription.request(1);
    }

    @Override
    public void onNext(FridgeState item) {
        if (item == null)
        {
            subscription.request(1);
            return ;
        }
        System.out.println("coucou :)");
        this.label.setText(item.toString());
        this.slider.setValue(item.getBrink());
        this.fridgeStates.add(item);
        /*this.xxxx.setText(*/this.publisher.pntRosee_Value(item)/*)*/;
        this.updateDampnessAlarm(this.publisher.pntRosee_Alarm(item));
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        logger.log(Level.SEVERE, throwable.toString());
    }

    @Override
    public void onComplete() {
        logger.log(Level.INFO, "Item request Complete");
    }
}

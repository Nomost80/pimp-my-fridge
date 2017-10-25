package views;

import models.Enum_AlarmStates;
import models.FridgeState;
import models.IQuery;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.Flow;
import java.util.logging.Level;
import java.util.logging.Logger;

public class View implements Flow.Subscriber<FridgeState> {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static String TITLE ;
    static int hoursPeriodTemperatures = 4;
    static int hoursPeriodDampness = 4;
    private IQuery publisher;
    private Thread thread;
    private volatile boolean threadActive;
    private static final Logger logger = Logger.getLogger("View");

    private JFrame frame;
    private JPanel mainPanel;
    private JPanel panel_Titre;
    private JPanel panel_Graphes;
    private JPanel panel_Graphe1;
    private JPanel panel_Graphe2;
    private JPanel panel_Values;

    private Flow.Subscription subscription;
    private ArrayList<FridgeState> fridgeStates;

    private JSlider slider;
    private JButton button;
    private JLabel label;

    private JComboBox<Integer> periodTemperatures;
    private Graphe graphTemperatures;
    private JComboBox<Integer> periodDampness;
    private Graphe graphDampness;

    private JButton startButton;
    private JButton stopButton;

    public View(final String title) {
        TITLE = title;
        this.fridgeStates = new ArrayList<>();

        this.buildFrameSettings();
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

    private void buildFrameSettings(){
        this.frame = new JFrame(TITLE);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizeable(true);
        this.setSize(800, 800);
        this.frame.setLocationRelativeTo(null);
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
                    .addComponent(this.panel_Titre, (int) Math.round(this.frame.getHeight() * 0.07), (int) Math.round(this.frame.getHeight() * 0.15), Short.MAX_VALUE)
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
        JPanel panel = buildOnePanel(Color.WHITE);
        GroupLayout layout = (GroupLayout) panel.getLayout();
        JLabel title = new JLabel();
        title.setText(TITLE);
        title.setFont(new Font("Comic Sans MS", Font.BOLD, 48));
        title.setForeground(Color.BLUE);
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setHorizontalTextPosition(JLabel.CENTER);
        title.setVerticalAlignment(JLabel.CENTER);
        title.setVerticalTextPosition(JLabel.CENTER);

        layout.setHorizontalGroup(      // MAX : 1
                layout.createSequentialGroup()
                        .addComponent(title, 0, this.frame.getWidth(), Short.MAX_VALUE)
        );
        layout.setVerticalGroup(        // MAX : 0.15
                layout.createSequentialGroup()
                        .addComponent(title, 0, (int) Math.round(this.frame.getHeight() * 0.1), (int) Math.round(this.frame.getHeight() * 0.15))
        );
        return panel;
    }

    private JPanel buildGraphesPanel(){
        JPanel panel = buildOnePanel(Color.WHITE);
        this.startButton = new JButton("Start");
        this.stopButton = new JButton("Stop");
        this.panel_Graphe1 = buildOnePanel(Color.RED);
        this.panel_Graphe2 = buildOnePanel(Color.RED);
        JLabel periode1 = new JLabel();
        periode1.setText("Temperatures Period: ");
        JLabel periode2 = new JLabel();
        periode2.setText("Dampness Period: ");
        JLabel typeDuree1 = new JLabel();
        typeDuree1.setText(" Hour(s)");
        JLabel typeDuree2 = new JLabel();
        typeDuree2.setText(" Hour(s)");
        this.periodTemperatures = generateHoursPeriod(hoursPeriodTemperatures - 1, (e -> {
            hoursPeriodTemperatures = (int) periodTemperatures.getSelectedItem();
            check_TemperaturesGraphe();
        }));
        this.periodDampness = generateHoursPeriod(hoursPeriodDampness - 1, (e -> {
            hoursPeriodDampness = (int) periodDampness.getSelectedItem();
            check_DampnessGraphe();
        }));
        GroupLayout layout = (GroupLayout) panel.getLayout();
        layout.setHorizontalGroup(  // MAX : 0.6
                layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(this.startButton, 0, (int) Math.round(this.frame.getWidth() * 0.1), (int) Math.round(this.frame.getWidth() * 0.1))
                                .addComponent(this.stopButton, 0, (int) Math.round(this.frame.getWidth() * 0.1), (int) Math.round(this.frame.getWidth() * 0.1))
                        )
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(periode1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(periodTemperatures, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(typeDuree1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                        .addComponent(panel_Graphe1, 0, (int) Math.round(this.frame.getWidth() * 0.6), Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(periode2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(periodDampness, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(typeDuree2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                        .addComponent(panel_Graphe2, 0, (int) Math.round(this.frame.getWidth() * 0.6), Short.MAX_VALUE)
        );
        layout.setVerticalGroup(    // MAX : 0.85
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                                .addComponent(this.startButton, 0, (int) Math.round(this.frame.getHeight() * 0.04), (int) Math.round(this.frame.getHeight() * 0.04))
                                .addComponent(this.stopButton, 0, (int) Math.round(this.frame.getHeight() * 0.04), (int) Math.round(this.frame.getHeight() * 0.04))
                        )
                        .addGroup(layout.createParallelGroup()
                                .addComponent(periode1, 0, (int) Math.round(this.frame.getHeight() * 0.03), (int) Math.round(this.frame.getHeight() * 0.03))
                                .addComponent(periodTemperatures, 0, (int) Math.round(this.frame.getHeight() * 0.03), (int) Math.round(this.frame.getHeight() * 0.03))
                                .addComponent(typeDuree1, 0, (int) Math.round(this.frame.getHeight() * 0.03), (int) Math.round(this.frame.getHeight() * 0.03))
                        )
                        .addComponent(panel_Graphe1, 0, (int) Math.round(this.frame.getHeight() * 0.25), Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(periode2, 0, (int) Math.round(this.frame.getHeight() * 0.03), (int) Math.round(this.frame.getHeight() * 0.03))
                                .addComponent(periodDampness, 0, (int) Math.round(this.frame.getHeight() * 0.03), (int) Math.round(this.frame.getHeight() * 0.03))
                                .addComponent(typeDuree2, 0, (int) Math.round(this.frame.getHeight() * 0.03), (int) Math.round(this.frame.getHeight() * 0.03))
                        )
                        .addComponent(panel_Graphe2, 0, (int) Math.round(this.frame.getHeight() * 0.25), Short.MAX_VALUE)
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

    private JComboBox<Integer> generateHoursPeriod(int indexSelectItem, ActionListener actionListener){
        JComboBox<Integer> comboBox = new JComboBox<>();
        for (int i = 1 ; i <= 6 ; i++){
            comboBox.addItem(i);
        }
        comboBox.setSelectedIndex(indexSelectItem);
        comboBox.addActionListener(actionListener);
        return comboBox;
    }

    private void create_Graphes(){
        this.graphTemperatures = new Graphe("Temperatures", this.panel_Graphe1);
        this.graphDampness = new Graphe("Dampness", this.panel_Graphe2);
    }

    private void create_ThreadGraphes(){
        this.thread = new Thread("Check Graphes") {
            public void run() {
                wait_FirstValue();
                check_Thread_Graphe();
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

    private void check_Thread_Graphe(){
        while (this.threadActive){
            check_graphes();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private void check_graphes(){
        check_TemperaturesGraphe();
        check_DampnessGraphe();
    }

    private void check_TemperaturesGraphe(){
        if (fridgeStates.size() == 0)
            return;
        TimeSeriesCollection temperaturesCollection = this.publisher.select_TemperaturesSeries(fridgeStates.get(0), LocalDateTime.now().minusHours(hoursPeriodTemperatures).format(formatter), LocalDateTime.now().format(formatter));
        repaintGraphe(temperaturesCollection, this.graphTemperatures);
    }

    private void check_DampnessGraphe(){
        if (fridgeStates.size() == 0)
            return;
        TimeSeriesCollection dampnessCollection = this.publisher.select_DampnessSerie(fridgeStates.get(0), LocalDateTime.now().minusHours(hoursPeriodDampness).format(formatter), LocalDateTime.now().format(formatter));
        repaintGraphe(dampnessCollection, this.graphDampness);
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

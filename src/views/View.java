package views;

import com.github.lgooddatepicker.components.DateTimePicker;
import models.Enum_AlarmStates;
import models.FridgeState;
import models.IQuery;
import models.Measurement;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
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
    private FridgeState fridgeStateExample;

    private JSlider slider;
    private JButton button;
    private JLabel sliderValue;
    private JProgressBar progressBar;
    private JLabel insideTemp;
    private JLabel moduleTemp;
    private JLabel outsideTemp;
    private JLabel dampness;
    private JLabel ptRosee;
    private JLabel currentBrink;
    private JLabel pbTitle;
    private JLabel spacer1;
    private JLabel spacer2;
    private JLabel spacer3;
    private JLabel spacer4;
    private JLabel sliderTitle;
    private JLabel warningTemp;
    private JLabel condensation;

    private JComboBox<Integer> periodTemperatures;
    private Graphe graphTemperatures;
    private JComboBox<Integer> periodDampness;
    private Graphe graphDampness;

    private JButton startButton;
    private JButton stopButton;
    private JButton periodTemp;
    private JButton submitPeriod;

    private String failureText = " échec de la mesure !";

    private JFrame periodFrame;
    private JPanel periodPanel;
    private DateTimePicker startDatePicker;
    private DateTimePicker endDatePicker;

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
        this.setSize(850, 1000);
        this.frame.setLocationRelativeTo(null);
    }

    private void buildFrameContent(){
        this.spacer1 = new JLabel();
        this.spacer2 = new JLabel();
        this.spacer3 = new JLabel();
        this.spacer4 = new JLabel();
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
        if (color != null)
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
        this.startButton = new JButton("Lancer la prise des mesures");
        this.startButton.setBackground(Color.GREEN);
        this.startButton.setPreferredSize(new Dimension(150, 20));
        this.stopButton = new JButton("Arrêter la prise des mesures");
        this.stopButton.setBackground(Color.RED);
        this.stopButton.setVisible(false);
        this.panel_Graphe1 = buildOnePanel(Color.WHITE);
        this.panel_Graphe2 = buildOnePanel(Color.WHITE);
        JLabel periode1 = new JLabel();
        periode1.setText("Temperatures Period: ");
        JLabel periode2 = new JLabel();
        periode2.setText("Dampness Period: ");
        JLabel typeDuree1 = new JLabel();
        typeDuree1.setText(" Hour(s)");
        JLabel typeDuree2 = new JLabel();
        typeDuree2.setText(" Hour(s)");
        this.warningTemp = new JLabel();
        this.warningTemp.setForeground(Color.RED);
        this.warningTemp.setText("Température anormalement élevée.\n La porte est sûrement ouverte !");
        this.warningTemp.setVisible(false);
        this.condensation = new JLabel();
        this.periodTemp = new JButton("Historique des valeurs enregistrées");
        this.periodTemp.addActionListener(e -> {
            periodFrame = new JFrame("Historique des valeurs enregistrées");
            periodFrame.setSize(500, 800);
            periodFrame.setLocationRelativeTo(null);
            periodPanel = buildOnePanel(Color.WHITE);
            GroupLayout layout = (GroupLayout) periodPanel.getLayout();
            periodFrame.setContentPane(periodPanel);
            startDatePicker = new DateTimePicker();
            endDatePicker = new DateTimePicker();
            submitPeriod = new JButton("Valider la période");
            JPanel pan1 = buildOnePanel(Color.WHITE);
            JPanel pan2 = buildOnePanel(Color.WHITE);
            JLabel space = new JLabel();
            Graphe g1 = new Graphe("Temperatures", pan1, periodFrame, (float) 1, (float) 0.4);
            Graphe g2 = new Graphe("Dampness", pan2, periodFrame, (float) 1, (float) 0.4);
            submitPeriod.addActionListener(e1 -> {
                LocalDateTime startAt = startDatePicker.getDateTimePermissive();
                LocalDateTime endAt = endDatePicker.getDateTimePermissive();
                if ((startAt != null) && (endAt != null))
                {
                    check_TemperaturesGraphe(g1, startAt, endAt);
                    check_DampnessGraphe(g2, startAt, endAt);
                }
            });
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(space, 1, periodFrame.getWidth(), Short.MAX_VALUE)
                            .addComponent(startDatePicker, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(endDatePicker, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(submitPeriod, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(pan1, 0, periodFrame.getWidth(), Short.MAX_VALUE)
                            .addComponent(pan2, 0, periodFrame.getWidth(), Short.MAX_VALUE)
            );
            layout.setVerticalGroup(
                    layout.createSequentialGroup()
                            .addComponent(space, 0, 0, 0)
                            .addComponent(startDatePicker, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(endDatePicker, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(submitPeriod, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(pan1, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                            .addComponent(pan2, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
            );
            periodFrame.setResizable(true);
            periodFrame.setVisible(true);
        });
        this.periodTemperatures = generateHoursPeriod(hoursPeriodTemperatures - 1, (e -> {
            hoursPeriodTemperatures = (int) periodTemperatures.getSelectedItem();
            check_graphes();
        }));
        this.periodDampness = generateHoursPeriod(hoursPeriodDampness - 1, (e -> {
            hoursPeriodDampness = (int) periodDampness.getSelectedItem();
            check_graphes();
        }));
        GroupLayout layout = (GroupLayout) panel.getLayout();
        layout.setHorizontalGroup(  // MAX : 0.6
                layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(this.startButton, 0, (int) Math.round(this.frame.getWidth() * 0.6), Short.MAX_VALUE)
                                .addComponent(this.stopButton, 0, (int) Math.round(this.frame.getWidth() * 0.6), Short.MAX_VALUE)
                        )
                        .addComponent(this.periodTemp, 0, (int) Math.round(this.frame.getWidth() * 0.6), Short.MAX_VALUE)
                        .addComponent(this.spacer4, 0, (int) Math.round(this.frame.getWidth() * 0.6), Short.MAX_VALUE)
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
                        .addComponent(this.spacer4, 0, (int) Math.round(this.frame.getWidth() * 0.6), Short.MAX_VALUE)
                        .addComponent(this.warningTemp, 0, (int) Math.round(this.frame.getWidth() * 0.6), Short.MAX_VALUE)
                        .addComponent(this.condensation, 0, (int) Math.round(this.frame.getWidth() * 0.6), Short.MAX_VALUE)
        );
        layout.setVerticalGroup(    // MAX : 0.85
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                                .addComponent(this.startButton, 0, (int) Math.round(this.frame.getHeight() * 0.04), (int) Math.round(this.frame.getHeight() * 0.04))
                                .addComponent(this.stopButton, 0, (int) Math.round(this.frame.getHeight() * 0.04), (int) Math.round(this.frame.getHeight() * 0.04))
                        )
                        .addComponent(this.periodTemp, 0, (int) Math.round(this.frame.getHeight() * 0.04), (int) Math.round(this.frame.getHeight() * 0.04))
                        .addComponent(this.spacer4, 0, (int) Math.round(this.frame.getHeight() * 0.02), (int) Math.round(this.frame.getHeight() * 0.04))
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
                        .addComponent(this.spacer4, 0, (int) Math.round(this.frame.getHeight() * 0.04), (int) Math.round(this.frame.getHeight() * 0.04))
                        .addComponent(this.warningTemp, 0, (int) Math.round(this.frame.getHeight() * 0.04), (int) Math.round(this.frame.getHeight() * 0.04))
                        .addComponent(this.condensation, 0, (int) Math.round(this.frame.getHeight() * 0.04), (int) Math.round(this.frame.getHeight() * 0.04))
        );
        return panel;
    }

    private JPanel buildValuesPanel(){
        JPanel panel = buildOnePanel(Color.WHITE);
        this.slider = new JSlider(10, 30, 18);
        this.slider.setToolTipText("Min : " + this.slider.getMinimum() + " - Max : " + this.slider.getMaximum());
        this.slider.addChangeListener(e -> sliderValue.setText(sliderValue.getText().split(":")[0] + ": " + Integer.toString(slider.getValue())));
        this.button = new JButton("Valider la nouvelle consigne");
        this.button.setBackground(Color.GREEN);
        this.pbTitle = new JLabel("Progression de l'atteinte de la consigne :");
        this.sliderValue = new JLabel("Nouvelle consigne sélectionnée : " + Integer.toString(this.slider.getValue()));
        this.progressBar = new JProgressBar();
        this.insideTemp = new JLabel("Température interne : ");
        this.moduleTemp = new JLabel("Température du module : ");
        this.outsideTemp = new JLabel("Température extérieur : ");
        this.dampness = new JLabel("Humidité : ");
        this.ptRosee = new JLabel("Point de rosée : ");
        this.currentBrink = new JLabel("Consigne actuelle : ");
        this.sliderTitle = new JLabel("Réglage de la consigne :");
        GroupLayout layout = (GroupLayout) panel.getLayout();
        layout.setHorizontalGroup(      // MAX : 0.4
                layout.createParallelGroup()
                        .addComponent(this.currentBrink, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                        .addComponent(this.spacer1, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                        .addComponent(this.sliderTitle, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                        .addComponent(this.slider, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                        .addComponent(this.sliderValue, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                        .addComponent(this.button, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                        .addComponent(this.spacer2, 0, (int) Math.round(this.frame.getWidth() * 0.2), Short.MAX_VALUE)
                        .addComponent(this.pbTitle, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                        .addComponent(this.progressBar, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                        .addComponent(this.spacer3, 0, (int) Math.round(this.frame.getWidth() * 0.2), Short.MAX_VALUE)
                        .addComponent(this.insideTemp, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                        .addComponent(this.moduleTemp, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                        .addComponent(this.outsideTemp, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                        .addComponent(this.dampness, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
                        .addComponent(this.ptRosee, 0, (int) Math.round(this.frame.getWidth() * 0.4), Short.MAX_VALUE)
        );
        layout.setVerticalGroup(        // MAX : 0.85
                layout.createSequentialGroup()
                        .addComponent(this.currentBrink, 0, (int) Math.round(this.frame.getHeight() * 0.05), (int) Math.round(this.frame.getHeight() * 0.05))
                        .addComponent(this.spacer1, 0, (int) Math.round(this.frame.getHeight() * 0.02), (int) Math.round(this.frame.getHeight() * 0.02))
                        .addComponent(this.sliderTitle, 0, (int) Math.round(this.frame.getHeight() * 0.05), (int) Math.round(this.frame.getHeight() * 0.05))
                        .addComponent(this.slider, 0, (int) Math.round(this.frame.getHeight() * 0.05), (int) Math.round(this.frame.getHeight() * 0.05))
                        .addComponent(this.sliderValue, 0, (int) Math.round(this.frame.getHeight() * 0.05), (int) Math.round(this.frame.getHeight() * 0.05))
                        .addComponent(this.button, 0, (int) Math.round(this.frame.getHeight() * 0.05), (int) Math.round(this.frame.getHeight() * 0.05))
                        .addComponent(this.spacer2, 0, (int) Math.round(this.frame.getHeight() * 0.02), (int) Math.round(this.frame.getHeight() * 0.02))
                        .addComponent(this.pbTitle, 0, (int) Math.round(this.frame.getHeight() * 0.05), (int) Math.round(this.frame.getHeight() * 0.05))
                        .addComponent(this.progressBar, 0, (int) Math.round(this.frame.getHeight() * 0.05), (int) Math.round(this.frame.getHeight() * 0.05))
                        .addComponent(this.spacer3, 0, (int) Math.round(this.frame.getHeight() * 0.02), (int) Math.round(this.frame.getHeight() * 0.02))
                        .addComponent(this.insideTemp, 0, (int) Math.round(this.frame.getHeight() * 0.05), (int) Math.round(this.frame.getHeight() * 0.05))
                        .addComponent(this.moduleTemp, 0, (int) Math.round(this.frame.getHeight() * 0.05), (int) Math.round(this.frame.getHeight() * 0.05))
                        .addComponent(this.outsideTemp, 0, (int) Math.round(this.frame.getHeight() * 0.05), (int) Math.round(this.frame.getHeight() * 0.05))
                        .addComponent(this.dampness, 0, (int) Math.round(this.frame.getHeight() * 0.05), (int) Math.round(this.frame.getHeight() * 0.05))
                        .addComponent(this.ptRosee, 0, (int) Math.round(this.frame.getHeight() * 0.05), (int) Math.round(this.frame.getHeight() * 0.05))

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
        this.graphTemperatures = new Graphe("Temperatures", this.panel_Graphe1, this.frame, (float) 0.6, (float) 0.25);
        this.graphDampness = new Graphe("Dampness", this.panel_Graphe2, this.frame, (float) 0.6, (float) 0.25);
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
      //  while (fridgeStates.size() == 0)
        while (fridgeStateExample == null)
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

    private void check_graphes(){ // hoursPeriodTemperatures hoursPeriodDampness
        check_TemperaturesGraphe(this.graphTemperatures, LocalDateTime.now().minusHours(hoursPeriodTemperatures), LocalDateTime.now());
        check_DampnessGraphe(this.graphDampness, LocalDateTime.now().minusHours(hoursPeriodDampness), LocalDateTime.now());
    }

    private void check_TemperaturesGraphe(Graphe graphe, LocalDateTime start, LocalDateTime end){
        if (fridgeStateExample == null)
            return;
        TimeSeriesCollection temperaturesCollection = this.publisher.select_TemperaturesSeries(fridgeStateExample, start.format(formatter), end.format(formatter));
        if (temperaturesCollection != null) {
            repaintGraphe(temperaturesCollection, graphe);
            graphe.temperaturesSettings();
        }
    }

    private void check_DampnessGraphe(Graphe graphe, LocalDateTime start, LocalDateTime end){
        if (fridgeStateExample == null)
            return;
        TimeSeriesCollection dampnessCollection = this.publisher.select_DampnessSerie(fridgeStateExample, start.format(formatter), end.format(formatter));
        if (dampnessCollection != null) {
            repaintGraphe(dampnessCollection, graphe);
            graphe.dampnessSettings();
        }
    }

    private void repaintGraphe(TimeSeriesCollection collection, Graphe graphe){
        graphe.updateGraphe(collection);
    }

    public void setIQuery(IQuery publisher){
        this.publisher = publisher;
        this.fridgeStateExample = this.publisher.getFridgeStateExample();
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
            case GOOD:
                this.condensation.setForeground(Color.GREEN);
                this.condensation.setText("Il n'y a pas de condensation !");
                break;
            case WARNING:
                this.condensation.setForeground(Color.ORANGE);
                this.condensation.setText("Le niveau de condensation est négligeable !");
                break;
            case CRITICAL:
                this.condensation.setForeground(Color.RED);
                this.condensation.setText("Il y a de la condensation !");
                break;
            default: break;
        }
    }

    private void updateProgressBar(FridgeState fridgeState) {
        int firstInsideTemp = 0;
        for (Measurement measurement : this.fridgeStates.get(0).getMeasurements()) {
            if (Objects.equals(measurement.getLabel(), "Inside temperature"))
                firstInsideTemp = (int)measurement.getValue();
        }

        int insideTemp = 0;
        for (Measurement measurement : fridgeState.getMeasurements()) {
            if (Objects.equals(measurement.getLabel(), "Inside temperature"))
                insideTemp = (int)measurement.getValue();
        }

        int max = firstInsideTemp - fridgeState.getBrink().intValue();
        int value = insideTemp - fridgeState.getBrink().intValue();

        System.out.println(max);
        System.out.println(value);

        this.progressBar.setMinimum(0);
        this.progressBar.setMaximum(max);
        this.progressBar.setValue(value);

        if (value < max * 0.33)
            this.progressBar.setForeground(Color.GREEN);
        else if (value <  max * 0.66)
            this.progressBar.setForeground(Color.ORANGE);
        else
            this.progressBar.setForeground(Color.RED);
    }

    private void updateLabels(FridgeState fridgeState) {
        for (Measurement measurement : fridgeState.getMeasurements()) {
            if (Objects.equals(measurement.getLabel(), "Inside temperature")) {
                String content;
                if (measurement.getValue() == -1)
                    content = this.failureText;
                else
                    content = Float.toString(measurement.getValue()) + " °C";
                this.insideTemp.setText(this.insideTemp.getText().split(":")[0] + ": " + content);
            }
            if (Objects.equals(measurement.getLabel(), "Module temperature")) {
                String content;
                if (measurement.getValue() == -1)
                    content = this.failureText;
                else
                    content = Float.toString(measurement.getValue()) + " °C";
                this.moduleTemp.setText(this.moduleTemp.getText().split(":")[0] + ": " + content);

            }
            if (Objects.equals(measurement.getLabel(), "Outside temperature")) {
                String content;
                if (measurement.getValue() == -1)
                    content = this.failureText;
                else
                    content = Float.toString(measurement.getValue()) + " °C";
                this.outsideTemp.setText(this.outsideTemp.getText().split(":")[0] + ": " + content);
            }
            if (Objects.equals(measurement.getLabel(), "Dampness")) {
                String content;
                if (measurement.getValue() == -1)
                    content = this.failureText;
                else
                    content = Float.toString(measurement.getValue()) + " %";
                this.dampness.setText(this.dampness.getText().split(":")[0] + ": " + content);
            }
        }
    }

    private void checkTemperature(FridgeState fridgeState) {
        for (Measurement measurement : this.fridgeStates.get(0).getMeasurements()) {
            if (Objects.equals(measurement.getLabel(), "Inside temperature")) {
                for (Measurement msr : fridgeState.getMeasurements()) {
                    if (Objects.equals(msr.getLabel(), "Inside temperature")) {
                        if (msr.getValue() > measurement.getValue() + 5)
                            this.warningTemp.setVisible(true);
                    }
                }
            }
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
        this.fridgeStates.add(item);
        this.currentBrink.setText(this.currentBrink.getText().split(":")[0] + ": " + Float.toString(item.getBrink()) + " °C");
        this.updateProgressBar(item);
        this.updateLabels(item);
        this.checkTemperature(item);
        this.ptRosee.setText(this.ptRosee.getText().split(":")[0] + ": " + this.publisher.pntRosee_Value(item) + " °C");
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

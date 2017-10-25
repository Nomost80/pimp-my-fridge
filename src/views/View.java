package views;

import models.FridgeState;
import models.IQuery;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
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
    private JPanel panel;
    private Flow.Subscription subscription;
    private ArrayList<FridgeState> fridgeStates;
    private JSlider slider;
    private JButton button;
    private JLabel label;
    private JButton startButton;
    private JButton stopButton;

    public View(String title) {
        this.frame = new JFrame(title);
        this.panel = new JPanel();
        this.frame.setContentPane(this.panel);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setLocationRelativeTo(null);
        this.fridgeStates = new ArrayList<>();
        this.setResizeable(true);
        this.setSize(500, 500);
        this.buildFrame();
        this.setVisibility(true);
        this.create_ThreadGraphes();
        this.start_ThreadGraphes();
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
            repaintGraphe(temperaturesCollection);
            repaintGraphe(dampnessCollection);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private void repaintGraphe(TimeSeriesCollection collection){
        if (collection != null){

        }
    }

    public void setIQuery(IQuery publisher){
        this.publisher = publisher;
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
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

    public void buildFrame() {
        this.slider = new JSlider(10, 30, 18);
        this.button = new JButton("Valider la nouvelle consigne");
        this.label = new JLabel();
        this.startButton = new JButton("Start");
        this.stopButton = new JButton("Stop");
        this.panel.add(this.button);
        this.panel.add(this.slider);
        this.panel.add(this.label);
        this.panel.add(this.startButton);
        this.panel.add(this.stopButton);
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

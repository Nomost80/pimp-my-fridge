package views;

import models.FridgeState;

import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.Flow;
import java.util.logging.Level;
import java.util.logging.Logger;

public class View implements Flow.Subscriber<FridgeState> {

    private static final Logger logger = Logger.getLogger("View");
    private JFrame frame;
    private JPanel panel;
    private Flow.Subscription subscription;
    private ArrayList<FridgeState> fridgeStates;
    private JSlider slider;
    private JButton button;
    private JLabel label;

    public View(String title) {
        this.frame = new JFrame(title);
        this.panel = new JPanel();
        this.frame.setContentPane(this.panel);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setLocationRelativeTo(null);
        this.fridgeStates = new ArrayList<>();
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

    public JButton getButton() {
        return button;
    }

    public JSlider getSlider() {
        return slider;
    }

    public void buildFrame() {
        this.slider = new JSlider(10, 30, 18);
        this.button = new JButton("Valider la nouvelle consigne");
        this.label = new JLabel();
        this.panel.add(this.button);
        this.panel.add(this.slider);
        this.panel.add(this.label);
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
        /* It signals that the current Subscriber is ready to consume more messages. */
        subscription.request(1);
    }

    @Override
    public void onNext(FridgeState item) {
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

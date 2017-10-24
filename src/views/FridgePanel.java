package views;

import controllers.IFridgeService;
import models.FridgeState;
import models.Measurement;

import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.Flow;
import java.util.logging.Level;
import java.util.logging.Logger;

class FridgePanel extends JPanel implements Flow.Subscriber<FridgeState> {

    private static final Logger logger = Logger.getLogger("FridgePanel");
    private Flow.Subscription subscription;
    private ArrayList<FridgeState> fridgeStates;
    private JLabel label = new JLabel();
    private boolean shouldInit = true;
    private ArrayList<JLabel> measurementsLabel;
    private JButton button = new JButton("Send data");

    FridgePanel() {
        this.fridgeStates = new ArrayList<>();
        this.measurementsLabel = new ArrayList<>();
        this.add(this.label);
        this.add(this.button);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        /* It signals that the current Subscriber is ready to consume more messages. */
        subscription.request(1);
    }

    @Override
    public void onNext(FridgeState item) {
        this.fridgeStates.add(item);
        this.label.setText(item.getMeasuredAt().toString());
        if (this.shouldInit) {
            for (Measurement measurement : item.getMeasurements()) {
                JLabel measurementLabel = new JLabel();
                measurementLabel.setText(measurement.toString());
                this.measurementsLabel.add(measurementLabel);
                this.add(measurementLabel);
            }
            this.shouldInit = false;
        }
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

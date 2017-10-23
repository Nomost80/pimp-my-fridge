package views;

import models.FridgeState;

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

    FridgePanel() {
        this.fridgeStates = new ArrayList<>();
        this.add(label);
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
        this.label.setText(item.toString());
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

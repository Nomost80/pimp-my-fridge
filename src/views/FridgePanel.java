package views;

import com.sun.tools.internal.ws.util.ClassNameInfo;
import models.FridgeState;

import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.Flow;
import java.util.logging.Level;
import java.util.logging.Logger;

class FridgePanel extends JPanel implements Flow.Subscriber<FridgeState> {

    private static final Logger logger = Logger.getLogger(ClassNameInfo.class.getName());
    private Flow.Subscription subscription;
    private ArrayList<FridgeState> fridgeStates;

    FridgePanel() {
        this.fridgeStates = new ArrayList<>();
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        /* It signals that the current Subscriber is ready to consume more messages. */
        subscription.request(1);
    }

    @Override
    public void onNext(FridgeState item) {
        subscription.request(1);
        this.fridgeStates.add(item);
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

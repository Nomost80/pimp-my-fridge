package controllers;

import models.*;
import views.View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FridgeService implements IFridgeService {

    private View view;
    private static final ICommunicator<FridgeState> communicator = new Communicator();
    private static final SerialPublisher publisher = new SerialPublisher(communicator);

    public FridgeService(View view) {
        this.view = view;
    }

    @Override
    public void control() {
        this.view.getButton().addActionListener(e -> sendData(Integer.toString(view.getSlider().getValue())));
        this.view.getStartButton().addActionListener(e -> {
            communicator.openPort();
            publisher.subscribe(view);
        });
        this.view.getStopButton().addActionListener(e -> {
            view.getSubscription().cancel();
            communicator.closePort();
        });
    }

    private void sendData(String data) {
        this.communicator.writeData(data);
    }
}

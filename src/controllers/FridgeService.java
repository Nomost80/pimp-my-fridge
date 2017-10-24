package controllers;

import models.FridgeState;
import models.ICommunicator;
import views.View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class FridgeService implements IFridgeService {

    private View view;
    private ICommunicator<FridgeState> communicator;

    public FridgeService(View view, ICommunicator<FridgeState> communicator) {
        this.view = view;
        this.communicator = communicator;
    }

    @Override
    public void addListeners() {
        this.view.getButton().addActionListener(e -> sendData(Integer.toString(view.getSlider().getValue())));
        this.view.getStopButton().addActionListener(e -> {
            view.getSubscription().cancel();
            communicator.closePort();
        });
    }

    private void sendData(String data) {
        this.communicator.writeData(data);
    }
}

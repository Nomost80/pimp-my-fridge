package controllers;

import models.*;
import views.View;

public class FridgeService implements IFridgeService {

    private View view;
    private static final ICommunicator<FridgeState> communicator = new Communicator();
    private static final SerialPublisher publisher = new SerialPublisher(communicator);

    public FridgeService(View view)
    {
        this.view = view;
        this.view.setIQuery(publisher);
    }

    @Override
    public void control() {
        this.view.getButton().addActionListener(e -> sendData(Integer.toString(view.getSlider().getValue())));
        this.view.getStartButton().addActionListener(e -> {
            if (communicator.isSerialPortAvailable()){
                communicator.openPort();
                publisher.subscribe(view);
                this.view.getStartButton().setVisible(false);
                this.view.getStopButton().setVisible(true);
            }
        });
        this.view.getStopButton().addActionListener(e -> {
            if (communicator.isSerialPortAvailable()){
                view.getSubscription().cancel();
                communicator.closePort();
                this.view.getStartButton().setVisible(true);
                this.view.getStopButton().setVisible(false);
            }
        });
    }

    private void sendData(String data) {
        this.communicator.writeData(data);
    }
}

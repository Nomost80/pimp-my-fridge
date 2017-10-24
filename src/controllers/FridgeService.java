package controllers;

import models.FridgeState;
import models.ICommunicator;
import views.View;

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
        this.view.getButton().addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                sendData(Integer.toString(view.getSlider().getValue()));
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    private void sendData(String data) {
        this.communicator.writeData(data);
    }
}

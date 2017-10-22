package controllers;

import models.SerialPublisher;
import views.FridgeFrame;

public class FridgeManager {

    private SerialPublisher serialPublisher;
    private FridgeFrame fridgeFrame;

    public FridgeManager() {
        this.serialPublisher = new SerialPublisher();
        this.fridgeFrame = new FridgeFrame("Fridge Manager", 500, 500);
    }
}

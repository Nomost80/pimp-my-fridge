import controllers.FridgeManager;
import models.SerialPublisher;
import views.FridgeFrame;

public class Main {

    public static void main(String[] args) {
        SerialPublisher serialPublisher = new SerialPublisher();
        FridgeManager fridgeManager = new FridgeManager();
        FridgeFrame fridgeFrame = new FridgeFrame("Fridge Manager", 500, 500);
        serialPublisher.subscribe(fridgeFrame.getFridgePanel());
    }
}

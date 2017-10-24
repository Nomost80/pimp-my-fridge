import controllers.FridgeService;
import controllers.IFridgeService;
import models.Communicator;
import models.FridgeState;
import models.ICommunicator;
import models.SerialPublisher;
import views.View;

public class Main {

    public static void main(String[] args) {
        SerialPublisher serialPublisher = new SerialPublisher();
        ICommunicator<FridgeState> communicator = new Communicator();
        View view = new View("Fridge Manager");
        view.setVisibility(true);
        view.setResizeable(false);
        view.setSize(500, 500);
        view.buildFrame();
        new FridgeService(view, communicator);
        serialPublisher.subscribe(view);
    }
}

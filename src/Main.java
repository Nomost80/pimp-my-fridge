import controllers.FridgeService;
import controllers.IFridgeService;
import models.Communicator;
import models.FridgeState;
import models.ICommunicator;
import models.SerialPublisher;
import views.View;

public class Main {

    public static void main(String[] args) {
        ICommunicator<FridgeState> communicator = new Communicator();
//            communicator.writeData("test");


        SerialPublisher serialPublisher = new SerialPublisher(communicator);
        View view = new View("Fridge Manager");
        view.setVisibility(true);
        view.setResizeable(false);
        view.setSize(500, 500);
        view.buildFrame();
        IFridgeService fridgeService = new FridgeService(view, communicator);
        fridgeService.addListeners();
        serialPublisher.subscribe(view);
    }
}

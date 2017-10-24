import controllers.FridgeService;
import controllers.IFridgeService;
import views.View;

public class Main {

    public static void main(String[] args) {
        View view = new View("Fridge Manager");
        view.setVisibility(true);
        view.setResizeable(true);
        view.setSize(500, 500);
        view.buildFrame();
        IFridgeService fridgeService = new FridgeService(view);
        fridgeService.control();
    }
}

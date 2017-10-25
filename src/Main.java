import controllers.FridgeService;
import controllers.IFridgeService;
import views.View;

public class Main {
    public static void main(String[] args) {
        View view = new View("Fridge Manager");
        IFridgeService fridgeService = new FridgeService(view);
        fridgeService.control();
    }
}

package views;

import javax.swing.*;
import java.util.logging.Logger;

public class FridgeFrame extends JFrame {

    public FridgeFrame(String title, int screenWidth, int screenHeight) {
        this.setTitle(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);
        this.setVisible(true);
        this.setSize(screenWidth, screenHeight);
        this.setLocationRelativeTo(null);
        FridgePanel fridgePanel = new FridgePanel();
        this.setContentPane(fridgePanel);
    }
}

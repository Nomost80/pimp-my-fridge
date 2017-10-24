package views;

import javax.swing.*;

public class FridgeFrame extends JFrame {

    private FridgePanel fridgePanel;

    public FridgeFrame(String title, int screenWidth, int screenHeight) {
        this.setTitle(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);
        this.setVisible(true);
        this.setSize(screenWidth, screenHeight);
        this.setLocationRelativeTo(null);
        this.fridgePanel = new FridgePanel();
        this.setContentPane(fridgePanel);
    }

    public FridgePanel getFridgePanel() {
        return fridgePanel;
    }

    public void setVisibility(boolean isVisible) {
        this.setVisible(isVisible);
    }
}

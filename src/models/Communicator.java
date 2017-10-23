package models;

import com.fazecast.jSerialComm.SerialPort;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Communicator implements ICommunicator<FridgeState> {

    private static final Logger logger = Logger.getLogger("Communicator");
    private SerialPort serialPort;

    public Communicator() {
        try {
            this.serialPort = SerialPort.getCommPorts()[0];
            if (!this.openPort())
                throw new Exception("Serial Port not opened");
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }

    public boolean openPort() {
        this.serialPort.openPort();
        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        return this.serialPort.isOpen();
    }

    public boolean closePort() {
        this.serialPort.closePort();
        return this.serialPort.isOpen();
    }

    public FridgeState readData() {
        Scanner scanner = new Scanner(this.serialPort.getInputStream()).useDelimiter("\n");
        String json = scanner.nextLine();
        FridgeState fridgeState = new FridgeState();
        fridgeState.setInsideTemperature(Integer.parseInt(temperature.split(":")[1]));
        fridgeState.setDampness(Integer.parseInt(dampness.split(":")[1]));
        return fridgeState;
    }

    public void writeData(FridgeState fridgeState) {
        this.serialPort.writeBytes()
    }
}

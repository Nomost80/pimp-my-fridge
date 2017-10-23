package models;

import com.fazecast.jSerialComm.SerialPort;

import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Communicator {

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

    private boolean openPort() {
        this.serialPort.openPort();
        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        return this.serialPort.isOpen();
    }

    public boolean closePort() {
        this.serialPort.closePort();
        return this.serialPort.isOpen();
    }

    public FridgeState readData() {
//        Pattern pattern = Pattern.compile("t:\\d+\\|d:\\d+");
//        byte[] readBuffer = new byte[17];
//        try {
//            String data;
//            /* On lire 17 octets sur le port série pour être sur d'avoir la trame des 9 octets dans l'ordre */
//            do {
//                this.serialPort.readBytes(readBuffer, readBuffer.length);
//                data = new String(readBuffer, "UTF-8");
//            } while (this.serialPort.bytesAvailable() < 17);
//            String[] str = data.split("t:\\d+\\|d:\\d+");
//            return str.
//        } catch (UnsupportedEncodingException e) {
//            logger.log(Level.SEVERE, e.toString());
//            return null;
//        }
        Scanner sc = new Scanner(this.serialPort.getInputStream()).useDelimiter("\n");
        String temperature = sc.nextLine();
        String dampness = sc.nextLine();
        FridgeState fridgeState = new FridgeState();
        fridgeState.setTemperature(Integer.parseInt(temperature.split(":")[1]));
        fridgeState.setDampness(Integer.parseInt(dampness.split(":")[1]));
        return fridgeState;
    }
}

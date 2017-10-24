package models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
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
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, FridgeState.class);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
            return null;
        }
    }

    public void writeData(FridgeState fridgeState) {
        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            byte[] data = writer.writeValueAsString(fridgeState).getBytes();
            this.serialPort.writeBytes(data, data.length);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }
}

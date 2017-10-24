package models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
        String json = null;
        try {
            byte[] readBuffer = new byte[this.serialPort.bytesAvailable()];
            this.serialPort.readBytes(readBuffer, readBuffer.length);
            String data = new String(readBuffer, "UTF-8");
            /*
                On reçoit plusieurs json string sur le port série qui sont délimités par des retour chariot
                Je les split puis je récupère une qui est situé entre la première et dernière comme ça on sait qu'on a
                bien reçu tous les octets qui la compose tout en enlevant le premier caractère qui est un \n
            */
            json = data.split("\\r")[5].substring(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);
        try {
            return mapper.readValue(json, FridgeState.class);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
            return null;
        }
    }

    public void writeData(String data) {
        try {
            byte[] bytes = data.getBytes();
            this.serialPort.writeBytes(bytes, bytes.length);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }
}

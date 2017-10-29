package models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;
import models.db.DB_ValuesSensors;
import org.jfree.data.time.TimeSeriesCollection;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Communicator implements ICommunicator<FridgeState> {
    private static final Logger logger = Logger.getLogger("Communicator");
    private SerialPort serialPort;
    private boolean serialPortAvailable = false;

    public Communicator() {
        selectSerialPort();
    }

    private void selectSerialPort(){
        if (SerialPort.getCommPorts().length > 0)
        {
            this.serialPort = SerialPort.getCommPorts()[0];
            this.serialPort.setBaudRate(19200);
            this.serialPortAvailable = true;
        }
        else
        {
            System.out.println("Aucun port série disponible ! :/");
            this.serialPortAvailable = false;
        }
    }

    public boolean isSerialPortAvailable(){
        return serialPortAvailable;
    }

    public boolean openPort() {
        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        return this.serialPort.openPort();
    }

    public boolean closePort() {
        this.serialPort.closePort();
        return this.serialPort.isOpen();
    }

    public FridgeState readData() {
        String json = null;
        System.out.println("coucou, on lit les données");
        try {
            byte[] readBuffer = new byte[this.serialPort.bytesAvailable()];
            this.serialPort.readBytes(readBuffer, readBuffer.length);
            String data = new String(readBuffer, "UTF-8");
            /*
                On reçoit plusieurs json string sur le port série qui sont délimités par des retour chariot
                Je les split puis je récupère une qui est situé entre la première et dernière comme ça on sait qu'on a
                bien reçu tous les octets qui la compose tout en enlevant le premier caractère qui est un \n
            */
            String[] dataSplit = data.split("\\r");
            if (dataSplit.length > 4 )
            {
                json = dataSplit[1].substring(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (json == null)
            return null;
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
            OutputStream outputStream = this.serialPort.getOutputStream();
            outputStream.write(bytes);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }
}

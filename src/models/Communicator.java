package models;

import com.fazecast.jSerialComm.SerialPort;
import com.sun.tools.internal.ws.util.ClassNameInfo;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Communicator {

    private static final Logger logger = Logger.getLogger(ClassNameInfo.class.getName());
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
        return this.serialPort.isOpen();
    }

    public boolean closePort() {
        this.serialPort.closePort();
        return this.serialPort.isOpen();
    }

    public String readData() {
        return null;
    }
}

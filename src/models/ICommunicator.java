package models;

public interface ICommunicator<T> {
    boolean isSerialPortAvailable();
    boolean openPort();
    boolean closePort();
    T readData();
    void writeData(String data);
}

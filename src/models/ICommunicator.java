package models;

public interface ICommunicator<T> {
    boolean openPort();
    boolean closePort();
    T readData();
    void writeData(T t);
}

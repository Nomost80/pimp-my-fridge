import controllers.FridgeManager;

public class Main {

    public static void main(String[] args) {
        new FridgeManager();
//        SerialPort comPort = SerialPort.getCommPorts()[0];
//        comPort.openPort();
//        try {
//            while (true)
//            {
//                while (comPort.bytesAvailable() == 0)
//                    Thread.sleep(20);
//
//                byte[] readBuffer = new byte[comPort.bytesAvailable()];
//                int numRead = comPort.readBytes(readBuffer, readBuffer.length);
//                System.out.println("Read " + numRead + " bytes.");
//                String data = new String(readBuffer, "UTF-8");
//                System.out.println("Data : " + data);
//            }
//        } catch (Exception e) { e.printStackTrace(); }
//        comPort.closePort();
    }
}

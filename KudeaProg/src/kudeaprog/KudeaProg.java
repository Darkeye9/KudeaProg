/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kudeaprog;

import java.io.BufferedReader;
import java.io.FileReader;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 *
 * @author Dark
 */
public class KudeaProg {

    static SerialPort serialPort;
    static BufferedReader reader;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            reader = new BufferedReader(new FileReader(args[0]+"\\"+args[1]));

            serialPort = new SerialPort("COM4");
            try {
                System.out.println("KudeaProg v1.0 by Dark_eye");
                System.out.println("http://d-eye.eu");
                System.out.println("");
                
                
                serialPort.openPort();//Open port
                serialPort.setParams(9600, 8, 1, 0);//Set params
                int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
                serialPort.setEventsMask(mask);//Set mask
                serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
            } catch (SerialPortException ex) {
                System.out.println(ex);
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    static class SerialPortReader implements SerialPortEventListener {

        String acum = "";
        Boolean c = false;

        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR()) {//If data is available
                if (event.getEventValue() > 0) {//Check bytes count in the input buffer
                    //Read data, if 10 bytes available 
                    try {
                        byte buffer[] = serialPort.readBytes(event.getEventValue());
                        acum += new String(buffer);
                        System.out.print(new String(buffer));

                        if (acum.contains("Seguro? [S/N]")) {
                            serialPort.writeBytes("s\r".getBytes());
                            acum = "";
                        } else if (acum.contains("PIC>")) {
                            if (!c) {
                                serialPort.writeBytes("\r".getBytes());
                                Thread.sleep(50);
                                serialPort.writeBytes("c\r".getBytes());
                                Thread.sleep(50);
                                acum = "";
                                c = true;
                            } else {
                                serialPort.writeBytes("s\r".getBytes());
                                Thread.sleep(50);
                                serialPort.closePort();
                                reader.close();
                            }
                        } else if (acum.contains("Preparado")) {
                            serialPort.writeBytes(reader.readLine().getBytes());
                            serialPort.writeBytes("\r".getBytes());
                            Thread.sleep(50);
                            acum = "";
                        } else if (acum.contains("#") || acum.contains("*")) {
                            if (reader.ready()) {
                                serialPort.writeBytes(reader.readLine().getBytes());
                                serialPort.writeBytes("\r".getBytes());
                            } else {
                                serialPort.writeBytes("\r".getBytes());
                            }
                            Thread.sleep(50);
                            acum = "";
                        }
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
            } else if (event.isCTS()) {//If CTS line has changed state
                if (event.getEventValue() == 1) {//If line is ON
                    System.out.println("CTS - ON");
                } else {
                    System.out.println("CTS - OFF");
                }
            } else if (event.isDSR()) {///If DSR line has changed state
                if (event.getEventValue() == 1) {//If line is ON
                    System.out.println("DSR - ON");
                } else {
                    System.out.println("DSR - OFF");
                }
            }
        }
    }
}

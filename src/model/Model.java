package model;

import flightSetting.FlightSetting;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import viewModel.TimeSeries;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.Socket;
import java.util.Observable;

public class Model extends Observable implements SimulatorModel {

    public Socket socket;
    public PrintWriter out;
    public TimeSeries ts;
    public Options op = new Options();
    Thread displaySetting;

    // static double time = 0;
    private double playSpeed = 100;
    private double time = 1;
    private volatile boolean pause = false;
    private boolean stop = false;
    public static boolean afterPause = false;
    public static boolean afterStop = false;
    public static boolean afterRewind = false;
    public static boolean afterForward = false;
    public boolean isConnect;

    public boolean isStop() {
        return stop;
    }

    public void close() {
        time = 0;
        this.stop = true;

    }

    public double getTime() {
        return this.time;
    }

    public double getPlaySpeed() {
        return playSpeed;
    }

    public void setPlaySpeed(double playSpeed) {
        this.playSpeed = playSpeed;
    }

    @Override
    public boolean ConnectToServer(String ip, double port) {
        try {
            socket = new Socket("127.0.0.1", 5402);
            out = new PrintWriter(socket.getOutputStream());
            System.out.println("connected to server");
            return true;

        } catch (IOException e) {
            System.out.println("didnt connect");
            return false;
        }
    }

    public void setTimeSeries(TimeSeries ts) {
        this.ts = ts;
    }

    synchronized public void displayFlight(boolean conncetServer) {
        int i = 0;
        boolean condition = op.rewind ? i >= 0 : i < ts.rows.size();//if rewind go while>0 else (regula) go while <ts.size
        op.setPlaySpeed(op.forward ? op.playSpeed / 2 : 100);
//        time = op.plus15 ? time + 150 : time;
//        time = op.minus15 ? time - 15 : time;

        for (i = (int) time; condition && !stop; ) {
            while (pause || op.scroll || afterStop || op.forward)  //pause needs to be replaced with thread( works only one time now)
            {
                System.out.println("get here after pause,afterStop:" + pause + " " + afterStop);
                try {
                    System.out.println(Thread.currentThread().getName());
                    System.out.println(this);
                    if (afterStop) {
                        displaySetting.stop();
                    }
                    if (afterPause) {
                        this.wait();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(ts.rows.get(i));
            if (conncetServer) {
                out.println(ts.rows.get(i));
                out.flush();
            }
            time = i;
            setChanged();
            notifyObservers();
            try {
                Thread.sleep((long) getPlaySpeed());//responsible for the speed of the display
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i = op.rewind ? i - 1 : i + 1;
        }
    }

    public void setTime(double time) {
        this.time = time;
        new Thread(() -> displayFlight(true)).start();
    }

    public void openXML() {
        FileChooser fc = new FileChooser();
        fc.setTitle("open XML file");
        fc.setInitialDirectory(new File("./"));
        File chosen = fc.showOpenDialog(null);
        if (chosen != null) {
            System.out.println("the name of the file is:" + chosen.getName());
        }
        if (!chosen.getName().contains(".xml"))  //checking the file
        {
            //System.err.println("wrong file, choose xml file");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Wrong file chosen");
            alert.setContentText("please choose a csv file");
            alert.showAndWait();
        }
    }

    synchronized public void playFile() {

        if (afterForward) {//somehow it does not responded to it and cannot go back to normal rate
            afterForward = false;
            System.out.println("blablalalal");
            op.setPlaySpeed(100);

        } else if (afterRewind) {
            System.out.println("was in after rewind");
            op.rewind = false;
        } else if (afterPause) {
            System.out.println("afterPause in display");
            this.notify();
            pause = false;
            afterPause = false;
        } else if (afterStop) {//creating a new thread to run displayFlight()
            if (isConnect) {
                displaySetting = new Thread(() -> displayFlight(true), "Thread of displaySetting function");
                displaySetting.start();
                afterStop = false;
            } else {
                displaySetting = new Thread(() -> displayFlight(false), "Thread of displaySetting function");
                displaySetting.start();
                afterStop = false;
            }

        } else {//first time of Play

            isConnect = ConnectToServer("127.0.0.1", 5402);
            if (isConnect) {

                displaySetting = new Thread(() -> displayFlight(true), "Thread of displaySetting function");
                displaySetting.start();
            } else {//if not connectToFG
                displaySetting = new Thread(() -> displayFlight(false), "Thread of displaySetting function");
                displaySetting.start();
            }

            System.out.println("inside playFile  " + Thread.currentThread().getName());
        }
    }

    public void pauseFile() {
        System.out.println("afterPause is true");
        pause = true;
        afterPause = true;
    }


    public void stopFile() {
        afterStop = true;
        this.time = 0;

    }

    public void rewindFile() {
        op.rewind = true;
        //new Thread(() -> displayFlight()).start();
    }

    public void forwardFile() {

        op.forward = true;
        // new Thread(() -> displayFlight()).start();
    }

    public void plus151File() {
        op.plus15 = true;
        //   new Thread(() -> displayFlight()).start();
    }

    public void minus15File() {
        op.minus15 = true;
        //  new Thread(() -> displayFlight()).start();
    }

    @Override
    public void writeToXML(FlightSetting settings) throws IOException {
        FileOutputStream fos = new FileOutputStream("settings.xml");
        XMLEncoder encoder = new XMLEncoder(fos);
        encoder.setExceptionListener(new ExceptionListener() {
            public void exceptionThrown(Exception e) {
                System.out.println("Exception! :" + e.toString());
            }
        });
        encoder.writeObject(settings);
        encoder.close();
        fos.close();
    }

    @Override
    public FlightSetting readFromXML() throws IOException {

        FileInputStream fis = new FileInputStream("settings.xml");
        XMLDecoder decoder = new XMLDecoder(fis);
        FlightSetting decodedSettings = (FlightSetting) decoder.readObject();
        decoder.close();
        fis.close();
        return decodedSettings;
    }

    @Override
    public void openFile() {

    }


    //NOTE:we'll need to add get the result of each functions when needed-
    // and we'll get them from the update of the viewModelController

}
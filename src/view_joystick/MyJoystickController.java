package view_joystick;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.shape.Circle;
import viewModel.ViewModelController;

public class MyJoystickController {

    @FXML
    Slider rudder;

    @FXML
    Slider throttle;

    @FXML
    Circle joystick;

    ViewModelController vmc;
    public DoubleProperty aileron, elevators;
    private double jx,jy;
    private double mx,my;

    public MyJoystickController() {
        jx=70; jy=80;
        aileron=new SimpleDoubleProperty();
        elevators= new SimpleDoubleProperty();
    }

    public void init(ViewModelController vmc) {
        this.vmc=vmc;
        rudder.valueProperty().bind(vmc.rudder);
        throttle.valueProperty().bind(vmc.throttle);
    }
/*
    public void paint()
    {
        GraphicsContext gc= joystick.getGraphicsContext2D();
        mx= joystick.getWidth()/2;
        my=joystick.getHeight()/2;
        gc.clearRect(0,0,joystick.getWidth(),joystick.getHeight());
        gc.strokeOval(jx-50, jy-50, 100,100);
    }
 */
}

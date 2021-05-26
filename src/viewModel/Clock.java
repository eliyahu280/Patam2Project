package viewModel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Clock {
    public IntegerProperty miliSec;
    public IntegerProperty seconds;
    public IntegerProperty minutes;

    public Clock() {
        this.miliSec = new SimpleIntegerProperty();
        this.seconds = new SimpleIntegerProperty();
        this.minutes = new SimpleIntegerProperty();
        this.miliSec.setValue(0);
        this.seconds.setValue(0);
        this.minutes.setValue(0);
    }

    public void increcment() {
        this.miliSec.set(this.miliSec.get() + 10);
        if(this.miliSec.get() == 100) {
            this.seconds.set(this.seconds.get() + 1);
            this.miliSec.set(0);
        }
        if(this.seconds.get() == 60) {
            this.minutes.set(this.minutes.get() + 1);
            this.seconds.set(0);
        }
    }
}
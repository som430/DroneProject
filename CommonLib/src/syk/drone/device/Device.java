package syk.drone.device;

public abstract class Device {
    public int no;
    public Device(int no) {
        this.no = no;
    }

    public void on() {}
    public void off() {}
}

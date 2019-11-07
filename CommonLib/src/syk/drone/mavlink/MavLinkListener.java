package syk.drone.mavlink;

import syk.drone.mavlink.Messages.MAVLinkMessage;

public interface MavLinkListener {
    void receive(MAVLinkMessage mavLinkMessage);
}

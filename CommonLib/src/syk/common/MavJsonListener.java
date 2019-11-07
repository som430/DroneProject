package syk.common;

import org.json.JSONObject;

public interface MavJsonListener {
    public void receive(JSONObject jsonMessage);
}

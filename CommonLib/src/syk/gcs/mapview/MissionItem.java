package syk.gcs.mapview;

import syk.common.MavJsonMessage;

import java.io.Serializable;

public class MissionItem implements Serializable {
    private int seq;
    private int command;
    private String strCommand;
    private float param1;
    private float param2;
    private float param3;
    private float param4;
    private double x;
    private double y;
    private float z;

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
        if(command == MavJsonMessage.MAVJSON_MISSION_COMMAND_WAYPOINT) {
            if(seq == 0) {
                strCommand = MavJsonMessage.MAVJSON_MISSION_COMMAND_HOME_STRING;
            } else {
                strCommand = MavJsonMessage.MAVJSON_MISSION_COMMAND_WAYPOINT_STRING;
            }
        } else if(command == MavJsonMessage.MAVJSON_MISSION_COMMAND_RTL) {
            strCommand = MavJsonMessage.MAVJSON_MISSION_COMMAND_RTL_STRING;
        } else if(command == MavJsonMessage.MAVJSON_MISSION_COMMAND_LAND) {
            strCommand = MavJsonMessage.MAVJSON_MISSION_COMMAND_LAND_STRING;
        } else if(command == MavJsonMessage.MAVJSON_MISSION_COMMAND_TAKEOFF) {
            strCommand = MavJsonMessage.MAVJSON_MISSION_COMMAND_TAKEOFF_STRING;
        } else if(command == MavJsonMessage.MAVJSON_MISSION_COMMAND_JUMP) {
            strCommand = MavJsonMessage.MAVJSON_MISSION_COMMAND_JUMP_STRING;
        } else if(command == MavJsonMessage.MAVJSON_MISSION_COMMAND_ROI) {
            strCommand = MavJsonMessage.MAVJSON_MISSION_COMMAND_ROI_STRING;
        } else if(command == MavJsonMessage.MAVJSON_MISSION_COMMAND_DELAY) {
            strCommand = MavJsonMessage.MAVJSON_MISSION_COMMAND_DELAY_STRING;
        } else if(command == MavJsonMessage.MAVJSON_MISSION_COMMAND_GRIPPER) {
            strCommand = MavJsonMessage.MAVJSON_MISSION_COMMAND_GRIPPER_STRING;
        }
    }

    public float getParam1() {
        return param1;
    }

    public void setParam1(float param1) {
        this.param1 = param1;
    }

    public float getParam2() {
        return param2;
    }

    public void setParam2(float param2) {
        this.param2 = param2;
    }

    public float getParam3() {
        return param3;
    }

    public void setParam3(float param3) {
        this.param3 = param3;
    }

    public float getParam4() {
        return param4;
    }

    public void setParam4(float param4) {
        this.param4 = param4;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public String getStrCommand() {
        return strCommand;
    }

    public void setStrCommand(String strCommand) {
        this.strCommand = strCommand;
        if(strCommand.equals(MavJsonMessage.MAVJSON_MISSION_COMMAND_WAYPOINT_STRING)) {
            command = MavJsonMessage.MAVJSON_MISSION_COMMAND_WAYPOINT;
        } else if(strCommand.equals(MavJsonMessage.MAVJSON_MISSION_COMMAND_RTL_STRING)) {
            command = MavJsonMessage.MAVJSON_MISSION_COMMAND_RTL;
        } else if(strCommand.equals(MavJsonMessage.MAVJSON_MISSION_COMMAND_LAND_STRING)) {
            command = MavJsonMessage.MAVJSON_MISSION_COMMAND_LAND;
        } else if(strCommand.equals(MavJsonMessage.MAVJSON_MISSION_COMMAND_TAKEOFF_STRING)) {
            command = MavJsonMessage.MAVJSON_MISSION_COMMAND_TAKEOFF;
        } else if(strCommand.equals(MavJsonMessage.MAVJSON_MISSION_COMMAND_JUMP_STRING)) {
            command = MavJsonMessage.MAVJSON_MISSION_COMMAND_JUMP;
        } else if(strCommand.equals(MavJsonMessage.MAVJSON_MISSION_COMMAND_ROI_STRING)) {
            command = MavJsonMessage.MAVJSON_MISSION_COMMAND_ROI;
        } else if(strCommand.equals(MavJsonMessage.MAVJSON_MISSION_COMMAND_DELAY_STRING)) {
            command = MavJsonMessage.MAVJSON_MISSION_COMMAND_DELAY;
        } else if(strCommand.equals(MavJsonMessage.MAVJSON_MISSION_COMMAND_GRIPPER_STRING)) {
            command = MavJsonMessage.MAVJSON_MISSION_COMMAND_GRIPPER;
        }
    }
}

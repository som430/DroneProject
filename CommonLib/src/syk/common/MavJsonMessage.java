package syk.common;

public class MavJsonMessage {
    //FlightController MavJsonMessage ID
    public static final String MAVJSON_MSG_ID_HEARTBEAT = "HEARTBEAT";
    public static final String MAVJSON_MSG_ID_ARM = "ARM";
    public static final String MAVJSON_MSG_ID_TAKEOFF = "TAKEOFF";
    public static final String MAVJSON_MSG_ID_SET_MODE = "SET_MODE";
    public static final String MAVJSON_MSG_ID_SET_POSITION_TARGET_GLOBAL_INT = "SET_POSITION_TARGET_GLOBAL_INT";
    public static final String MAVJSON_MSG_ID_GET_HOME_POSITION = "GET_HOME_POSITION";
    public static final String MAVJSON_MSG_ID_MISSION_UPLOAD = "MISSION_UPLOAD";
    public static final String MAVJSON_MSG_ID_MISSION_DOWNLOAD = "MISSION_DOWNLOAD";
    public static final String MAVJSON_MSG_ID_MISSION_START = "MISSION_START";
    public static final String MAVJSON_MSG_ID_FENCE_UPLOAD = "FENCE_UPLOAD";
    public static final String MAVJSON_MSG_ID_FENCE_DOWNLOAD = "FENCE_DOWNLOAD";
    public static final String MAVJSON_MSG_ID_FENCE_ENABLE = "FENCE_ENABLE";

    public static final String MAVJSON_MSG_ID_GLOBAL_POSITION_INT = "GLOBAL_POSITION_INT";
    public static final String MAVJSON_MSG_ID_ATTITUDE = "ATTITUDE";
    public static final String MAVJSON_MSG_ID_VFR_HUD = "VFR_HUD";
    public static final String MAVJSON_MSG_ID_SYS_STATUS = "SYS_STATUS";
    public static final String MAVJSON_MSG_ID_GPS_RAW_INT = "GPS_RAW_INT";
    public static final String MAVJSON_MSG_ID_STATUSTEXT = "STATUSTEXT";
    public static final String MAVJSON_MSG_ID_HOME_POSITION = "HOME_POSITION";
    public static final String MAVJSON_MSG_ID_MISSION_ACK = "MISSION_ACK";
    public static final String MAVJSON_MSG_ID_MISSION_ITEMS = "MISSION_ITEMS";
    public static final String MAVJSON_MSG_ID_MISSION_CURRENT = "MISSION_CURRENT";
    public static final String MAVJSON_MSG_ID_FENCE_ACK = "FENCE_ACK";
    public static final String MAVJSON_MSG_ID_FENCE_POINTS = "FENCE_POINTS";

    public static final String MAVJSON_MSG_ID_RECEIVE_MESSAGE_ALL = "RECEIVE_MESSAGE_ALL";
    public static final String MAVJSON_MSG_ID_SEND_MESSAGE_ALL = "SEND_MESSAGE_ALL";

    public static final String MAVJSON_MSG_ID_FINE_CONTROL = "MAVJSON_MSG_ID_FINE_CONTROL";

    public static final String MAVJSON_MODE_STABILIZE = "STABILIZE";
    public static final String MAVJSON_MODE_GUIDED = "GUIDED";
    public static final String MAVJSON_MODE_AUTO = "AUTO";
    public static final String MAVJSON_MODE_LAND = "LAND";
    public static final String MAVJSON_MODE_RTL = "RTL";

    public static final int MAVJSON_MISSION_COMMAND_WAYPOINT = 16;
    public static final String MAVJSON_MISSION_COMMAND_HOME_STRING = "HOME";
    public static final String MAVJSON_MISSION_COMMAND_WAYPOINT_STRING = "WAYPOINT";
    public static final int MAVJSON_MISSION_COMMAND_RTL = 20;
    public static final String MAVJSON_MISSION_COMMAND_RTL_STRING = "RTL";
    public static final int MAVJSON_MISSION_COMMAND_LAND = 21;
    public static final String MAVJSON_MISSION_COMMAND_LAND_STRING = "LAND";
    public static final int MAVJSON_MISSION_COMMAND_TAKEOFF = 22;
    public static final String MAVJSON_MISSION_COMMAND_TAKEOFF_STRING = "TAKEOFF";
    public static final int MAVJSON_MISSION_COMMAND_JUMP = 177;
    public static final String MAVJSON_MISSION_COMMAND_JUMP_STRING = "JUMP";
    public static final int MAVJSON_MISSION_COMMAND_ROI = 201;
    public static final String MAVJSON_MISSION_COMMAND_ROI_STRING = "ROI";
    public static final int MAVJSON_MISSION_COMMAND_DELAY = 93; //MAV_CMD_NAV_DELAY
    public static final String MAVJSON_MISSION_COMMAND_DELAY_STRING = "DELAY";
    public static final int MAVJSON_MISSION_COMMAND_GRIPPER = 211; //MAV_CMD_DO_GRIPPER
    public static final String MAVJSON_MISSION_COMMAND_GRIPPER_STRING = "ACTION";
}

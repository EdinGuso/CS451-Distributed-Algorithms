package cs451;

import cs451.util.FlowControl;

/*
 * Only file edited within the provided files (apart from main)
 */
public class Constants {
    public static final int ARG_LIMIT_CONFIG = 7;

    // indexes for id
    public static final int ID_KEY = 0;
    public static final int ID_VALUE = 1;

    // indexes for hosts
    public static final int HOSTS_KEY = 2;
    public static final int HOSTS_VALUE = 3;

    // indexes for output
    public static final int OUTPUT_KEY = 4;
    public static final int OUTPUT_VALUE = 5;

    // indexes for config
    public static final int CONFIG_VALUE = 6;



    // UDP LEVEL CONSTANTS
    public static int NETWORK_HEADER_SIZE;
    public static int LATTICE_PAYLOAD_SIZE;
    public static int UDP_MESSAGE_SIZE;
    public static int UDP_MESSAGE_LIMIT;
    public static int UDP_PACKET_SIZE;



    // MEMORY LIMITATION
    public static final int MEMORY_LIMIT = (int) Math.pow(2,26);



    // COMMON DATA STRUCTURES' SIZES
    public static int MESSAGE_CLASS_SIZE;
    public static int MESSAGE_BATCH_CLASS_SIZE;
    public static int RANGE_CLASS_SIZE;
    public static int RANGESET_CLASS_SIZE;
    public static int HASHMAP_BYTE_RANGESET_SIZE;
    public static int SCHEDULER_PROCESS_CLASS_SIZE;



    //PROTOCOLS AND THEIR SIZE LIMITATIONS
    public static int APP_QUEUE_SIZE;
    public static int SL_RESEND_LIMIT;
    public static int UDP_SENDER_QUEUE_SIZE;
    public static int UDP_ACK_QUEUE_SIZE;
    public static int UDP_DELIVERER_QUEUE_SIZE;



    // FLOW CONTROL OBJECTS
    public static FlowControl FLOW_CONTROL = new FlowControl();
}

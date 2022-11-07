package cs451;

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
    public static final int UDP_MESSAGE_SIZE = 5; //1 byte id + 4 bytes msg
    public static final int UDP_MESSAGE_LIMIT = 8; //described in the 
    public static final int UDP_PACKET_SIZE = UDP_MESSAGE_SIZE * UDP_MESSAGE_LIMIT + 4; //44bytes = 8 msgs x 5bytes + 4 bytes to store the number of msgs in the batch

    // DATA STRUCTURES SIZE LIMITATIONS
    public static final int APP_QUEUE_SIZE = (int) Math.pow(2,13);
    // Stores a list of MessageZip
    // - Used size per object = 5 bytes (int + byte)
    // - Java objects have 16 bytes header and are padded to next 8 bytes
    // - Expected object size = 24 bytes
    // We allow for 2^13 = 8192 objects to be stored
    // - Expected total size = 192KB
    // - Upper bound on size = 256KB

    public static final int PL_HASHSET_SIZE = (int) Integer.MAX_VALUE; //no limit on perfect links storage
    // Stores a list of MessageZip
    // - Used size per object = 5 bytes (int + byte)
    // - Java objects have 16 bytes header and are padded to next 8 bytes
    // - Expected object size = 24 bytes
    // No limit on perfect links storage
    // - Expected total size = 24MB per million messages
    // - Upper bound on size = INF

    public static final int SL_HASHSET_SIZE = (int) Math.pow(2,13);
    // Stores a hashmap of <MessageZip, boolean>
    // - Used size per object = 5 bytes (int + byte) + 1 byte for boolean as the value in HashMap
    // - Java objects have 16 bytes header and are padded to next 8 bytes
    // - Expected object size = 24 bytes + 8 bytes for boolean as the value in HashMap
    // We allow for 2^13 = 8192 objects to be stored
    // - Expected total size = 256KB
    // - Upper bound on size = 512KB
    
    public static final int UDP_SENDER_QUEUE_SIZE = (int) Math.pow(2,13);
    // Stores a list of Message
    // - Used size per object = 41 bytes (String(32) + int + int + byte)
    // - Java objects have 16 bytes header and are padded to next 8 bytes
    // - Expected object size = 64 bytes
    // We allow for 2^13 = 8192 objects to be stored
    // - Expected total size = 512KB
    // - Upper bound on size = 1MB

    public static final int UDP_ACK_QUEUE_SIZE = (int) Math.pow(2,11);
    // Stores a list of MessageBatch
    // - Used size per object = 228 bytes (8xMessageZip(192) + String(32) + int) (Will be more due to linked list holding MessageZips)
    // - Java objects have 16 bytes header and are padded to next 8 bytes
    // - Expected object size = >256 bytes
    // We allow for 2^11 = 2048 objects to be stored
    // - Expected total size = >512KB
    // - Upper bound on size = 1MB

    public static final int UDP_DELIVERER_QUEUE_SIZE = (int) Math.pow(2,13);
    // Stores a list of byte[]
    // - Used size per object = 44 bytes this is the size of byte[]
    // - Java objects have 16 bytes header and are padded to next 8 bytes
    // - Expected object size = 64 bytes
    // We allow for 2^13 = 8192 objects to be stored
    // - Expected total size = 512KB
    // - Upper bound on size = 1MB

    // APART FROM THE PERFECT LINKS SUM OF UPPER BOUNDS ON DATA STRUCTURES IS 3,75MB
    // IN THE WORST CASE, NOT PROCESS SHOULD CONSUME >10 MB apart from RECEIVER
    // but they do :( they consume between 40-50MB on average :(
}

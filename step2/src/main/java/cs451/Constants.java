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
    public static final int UDP_MESSAGE_SIZE = 6; //1 byte id + 1 byte origin + 4 bytes msg
    public static final int UDP_MESSAGE_LIMIT = 8; //described in the 
    public static final int UDP_PACKET_SIZE = UDP_MESSAGE_SIZE * UDP_MESSAGE_LIMIT + 4; //44bytes = 8 msgs x 5bytes + 4 bytes to store the number of msgs in the batch

    // DATA STRUCTURES SIZE LIMITATIONS
    public static final int APP_QUEUE_SIZE = (int) Math.pow(2,10);
    // Stores a list of MessageZip
    // - Used size per object = 5 bytes (int + byte)
    // - Java objects have 16 bytes header and are padded to next 8 bytes
    // - Expected object size = 24 bytes
    // We allow for 2^13 = 8192 objects to be stored
    // - Expected total size = 192KB
    // - Upper bound on size = 256KB

    public static final int URB_HASHSET_SIZE = (int) Math.pow(2,14); //...
    // ...

    public static final int PL_HASHSET_SIZE = (int) Integer.MAX_VALUE; //perfect links stop delivering when this limit is reached
    // Stores a hasmap of <Integer, DeliveredZip> where DeliveredZip stores a treeset of <Integer>
    // - Used size per object = 16 bytes (Integer)
    // - Java objects have 16 bytes header and are padded to next 8 bytes
    // - Expected object size = 16 bytes
    // We approximate that no more than 2^14 = 16384 objects to be stored
    // - Expected total size = >256KB
    // - Upper bound on size = 512KB

    public static final int SL_HASHSET_SIZE = (int) Math.pow(2,12);
    // Stores a hashmap of <Message, boolean>
    // - Used size per object = 42 bytes (String(32) + int + int + byte + byte) + 1 byte for boolean as the value in HashMap
    // - Java objects have 16 bytes header and are padded to next 8 bytes
    // - Expected object size = 64 bytes + 8 bytes for boolean as the value in HashMap
    // We allow for 2^12 = 4096 objects to be stored
    // - Expected total size = >256KB
    // - Upper bound on size = 512KB
    
    public static final int UDP_SENDER_QUEUE_SIZE = (int) Math.pow(2,12);
    // Stores a list of Message
    // - Used size per object = 42 bytes (String(32) + int + int + byte + byte)
    // - Java objects have 16 bytes header and are padded to next 8 bytes
    // - Expected object size = 64 bytes
    // We allow for 2^12 = 4096 objects to be stored
    // - Expected total size = 256KB
    // - Upper bound on size = 512KB

    public static final int UDP_ACK_QUEUE_SIZE = (int) Math.pow(2,11);
    // Stores a list of MessageBatch
    // - Used size per object = 228 bytes (8xMessageZip(192) + String(32) + int) (Will be more due to linked list holding MessageZips)
    // - Java objects have 16 bytes header and are padded to next 8 bytes
    // - Expected object size = >256 bytes
    // We allow for 2^11 = 2048 objects to be stored
    // - Expected total size = >512KB
    // - Upper bound on size = 1MB

    public static final int UDP_DELIVERER_QUEUE_SIZE = (int) Math.pow(2,11);
    // Stores a list of MessageBatch
    // - Used size per object = 228 bytes (8xMessageZip(192) + String(32) + int) (Will be more due to linked list holding MessageZips)
    // - Java objects have 16 bytes header and are padded to next 8 bytes
    // - Expected object size = >256 bytes
    // We allow for 2^11 = 2048 objects to be stored
    // - Expected total size = >512KB
    // - Upper bound on size = 1MB

    // UP TP PERFECT LINKS SUM OF UPPER BOUNDS ON DATA STRUCTURES IS 3,5MB
    // IN THE WORST CASE, NO PROCESS SHOULD CONSUME >10 MB
    // but they do :( they consume between 30-50MB on average :(
}

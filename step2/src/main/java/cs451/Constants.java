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
    public static final int UDP_MESSAGE_SIZE = 7; //1 byte origin + 1 byte source + 1 byte dest + 4 bytes seq
    public static final int UDP_MESSAGE_LIMIT = 8; //msgs per batch
    public static final int UDP_PACKET_SIZE = UDP_MESSAGE_SIZE * UDP_MESSAGE_LIMIT + 4; //60bytes = 8 msgs x 7 bytes + 4 bytes to store the number of msgs in the batch



    // MEMORY LIMITATION
    public static final int MEMORY_LIMIT = (int) Math.pow(2,26);



    // COMMON DATA STRUCTURES' SIZES
    public static final int MESSAGE_CLASS_SIZE = 24; //theoretically unbounded memory
    // Stores 3 bytes and 1 int
    // - Used size per object = 7 bytes
    // - Java objects have 12 bytes header and are padded to next 8 bytes
    // - Total object size = 24 bytes

    public static final int MESSAGE_BATCH_CLASS_SIZE = Integer.MAX_VALUE; //theoretically unbounded memory
    // Stores a linkedlist of messages
    // - Used size per entry = 24 bytes
    // We know that there will be at most 8 entries in the linkedlist
    // - Java objects have 12 bytes header and are padded to next 8 bytes
    // - Total object size = 208 bytes

    public static final int RANGE_CLASS_SIZE = 24;
    // Stores 2 ints
    // - Used size per object = 8 bytes
    // - Java objects have 12 bytes header and are padded to next 8 bytes
    // - Total object size = 24 bytes

    public static final int RANGESET_CLASS_SIZE = Integer.MAX_VALUE; //theoretically unbounded memory
    // Stores TreeSet<Range> + 3xRange
    // - Used size per object ~ (3+x)*24 bytes + 12 bytes
    // - Java objects have 12 bytes header and are padded to next 8 bytes
    // - Total object size ~ (4+x)*24 bytes

    public static final int HASHMAP_BYTE_RANGESET_SIZE = Integer.MAX_VALUE; //theoretically unbounded memory
    // Stores a hashmap of <Byte, RangeSet>>
    // - Used size per entry ~ 16 bytes + (4+x)*24 bytes
    // We know that there will be at most 128 entries in the hashmaps
    // - Used size per object ~ 14KiB + 3*x KiB

    public static final int SCHEDULER_PROCESS_CLASS_SIZE = Integer.MAX_VALUE; //theoretically unbounded memory
    // Stores a hashmap of <Byte, RangeSet>> and hashmap of <Byte, Integer>
    // - Used size per entry ~ 16 bytes + (4+x)*24 bytes and 32 bytes
    // We know that there will be at most 128 entries in the hashmaps
    // - Used size per object ~ 22KiB + 3*x KiB




    //PROTOCOLS AND THEIR SIZE LIMITATIONS

    // APPLICATION SIZE LIMITATION
    public static final int APP_QUEUE_SIZE = (int) Math.pow(2,10);
    // Stores a queue of String
    // - Used size per object = [3,16] bytes (String)
    // - Java Strings use 32 bytes if they don't need more
    // - Expected object size = 32 bytes
    // We allow for 2^10 = 1024 objects to be stored
    // - Expected total size = 32KiB
    // - Upper bound on size = 64KiB (2^16)

    // FIFO SIZE LIMITATION
    public static final int FIFO_RECEIVED_SIZE = Integer.MAX_VALUE; //theoretically unbounded memory
    // Stores a hashmap of <Byte, RangeSet>> and int
    // - Used size per object ~ 14KiB + 3*x KiB + 4bytes
    // We expect that on average we store at most 3 ranges
    // - Expected total size ~ 23KiB
    // - Upper bound on size = 32KiB (2^15)

    // URB SIZE LIMITATION
    public static final int URB_SEND_LIMIT = (int) Math.pow(2,14); //does not limit data structures, thus results in a theoretically unbounded memory
    // Stores a hashmap of <Byte, RangeSet>> and HashMap<MessageOrigin, boolean[]>
    // 1)
    // - Used size per object ~ 14KiB + 3*x KiB + 4bytes
    // We expect that on average we store at most 5 ranges
    // - Expected total size ~ 29KiB
    // 2)
    // - Used size per entry = 24bytes + 128bytes
    // - Used size per object = 152*x bytes + 12 bytes
    // We expect that on average we store at most 4*2^14 entries in the hashmap (4*best case scenario)
    // - Expected total size ~ 10MiB
    // Total)
    // - Upper bound on size = 16MiB (2^24)

    // BEB SIZE LIMTIATION
    public static final int BEB_SIZE_LIMIT = 0;
    // BEB does not store any large data structures. Only a few references.

    // PL SIZE LIMITATION
    public static final int PL_SIZE_LIMIT = 0;
    // PL does not store any large data structures. Only a few references.

    // SL SIZE LIMITATION
    //...
    public static final int SL_RESEND_LIMIT = (int) Math.pow(2,14);
    // Stores a hashmap of <Byte, SchedulerProcess>
    // - Used size per entry ~ 22KiB + 3*x KiB
    // We know that there will be at most 128 entries in the hashmaps
    // - Used size per object ~ 1792KiB + 384*x KiB + 4bytes
    // We expect that on average we store at most 10 ranges
    // - Expected total size ~ 5MiB
    // - Upper bound on size = 8MiB (2^23)

    // FLL SIZE LIMITATION
    public static final int FLL_SIZE_LIMIT = 0;
    // FLL does not store any large data structures. Only a few references.

    // CLIENT SIZE LIMITATION
    public static final int UDP_SENDER_QUEUE_SIZE = (int) Math.pow(2,14);
    public static final int UDP_ACK_QUEUE_SIZE = (int) Math.pow(2,11);
    //Stores a hashmap of <Byte, ArrayBlockingQueue<Message>> and ArrayBlockingQueue<MessageBatch>
    // 1)
    // - Used size per entry = 16 + (12 + 24 * 2^14 / n)
    // We know that there will be at most 128 (n) entries in the hashmaps
    // - Expected total size ~ 400KiB
    // 2)
    // - Used size per entry = 208bytes
    // We know that there will be at most 2048 entries
    // - Used size per object ~ 430KiB
    // Total)
    // - Upper bound on size = 1MiB (2^20)

    // SERVER SIZE LIMITATION
    public static final int UDP_SERVER_SIZE_LIMIT = 0;
    // Server does not store any large data structures. Only a few references.

    // DELIVERER SIZE LIMITATION
    public static final int UDP_DELIVERER_QUEUE_SIZE = (int) Math.pow(2,12);
    // Stores a list of byte[]
    // - Used size per entry = 68 bytes
    // - Java objects have 12 bytes header and are padded to next 8 bytes
    // - Expected object size = 80 bytes
    // We allow for 2^12 = 4096 objects to be stored
    // - Expected total size = 320KiB
    // - Upper bound on size = 512KiB



    // FLOW CONTROL OBJECTS
    public static int MY_FLOW_RATE = 0;
    public static FlowControl FLOW_CONTROL = new FlowControl();
    // - Upper bound on size = 32KiB



    // SUM OF UPPER BOUNDS ON DATA STRUCTURES IS UPPER BOUNDED BY 32MiB
    // THIS IS DONE TO ENSURE THAT ALONG WITH SOME COMMON OBJECTS, CLASSES,
    // AND JAVA SHENANIGANS WE DON'T EXCEEDE 64MiB per process
}

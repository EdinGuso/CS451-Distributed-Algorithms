package cs451;

import java.util.List;
import java.util.HashMap;
import java.nio.file.Paths;
import java.nio.file.Files;

import cs451.app.Application;

public class Main {

    private static Application app;

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");

        //stop the app
        if (app != null) app.stop_();
        else System.out.println("SIGTERM received before initializing the application!");
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");

        System.out.println("Broadcasting and delivering messages...\n");


        System.out.println("===START OF MY OUTPUT===\n");

        //get the necessary parameters from cfg files
        int num_messages = 0;
        try {num_messages = Integer.parseInt((new String(Files.readAllBytes(Paths.get(parser.config())))).trim());} catch (Exception e) { e.printStackTrace(); }

        //map the ids to hosts for efficient retrieval of hosts
        List<Host> hosts = parser.hosts();
        HashMap<Byte, Host> hosts_map = new HashMap<Byte, Host>();
        for (Host host : hosts) {
            hosts_map.put((byte) host.getId(), host);
        }

        //initialize the app
        app = new Application(hosts_map, hosts_map.get((byte) parser.myId()), num_messages, parser.output());

        //start the app
        app.start();

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}

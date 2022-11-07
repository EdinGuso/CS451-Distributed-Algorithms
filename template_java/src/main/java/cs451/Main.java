package cs451;

import java.util.List;
import java.nio.file.Paths;
import java.nio.file.Files;

import cs451.app.Application;
import cs451.util.Message;

public class Main {

    private static Application app;

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");

        //stop the app
        if (app != null) app.stop_();
        else System.out.println("SIGTERM received before initializing application!");
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
        List<Host> hosts = parser.hosts();
        String data = "";
        try {data = new String(Files.readAllBytes(Paths.get(parser.config())));} catch (Exception e) {}
        int num_messages = Integer.parseInt(data.split(" ")[0].trim());
        int target_id = Integer.parseInt(data.split(" ")[1].trim());

        Host target = null;
        int port = -1;
        for (Host host : hosts) {
            if (host.getId() == target_id) {
                target = host;
            }
            if (host.getId() == parser.myId()) {
                port = host.getPort();
            }
        }

        //initialize the app
        app = new Application(hosts, parser.myId(), port, target, num_messages, parser.output());

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

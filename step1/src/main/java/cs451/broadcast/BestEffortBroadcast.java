// package cs451.broadcast;

// import java.util.List;
// import java.util.HashSet;

// import cs451.Host;
// import cs451.Constants;
// import cs451.app.Application;
// import cs451.util.Message;
// import cs451.util.MessageZip;
// import cs451.links.PerfectLinks;
// //import cs451.broadcast.UniformReliableBroadcast;

// /*
//  * Implements the perfect links part of the protocol.
//  * Constructs the lower layer and set for storing delivered messages,
//  * and provides functions to upper layer (app) for starting, sending
//  * and stopping; as well as a deliver function to lower layer.
//  */
// public class BestEffortBroadcast {

//     //private UniformReliableBroadcast upper_layer;
//     private PerfectLinks lower_layer;
//     private List<Host> hosts;
//     private int id;
//     private String ip;
//     private int port;
//     private int s_count;
//     private int d_count;

//     public BestEffortBroadcast(int id, String ip, int port, List<Host> hosts/*, UniformReliableBroadcast urb*/) {
//         //this.upper_layer = urb;
//         this.lower_layer = new PerfectLinks(id, port, hosts.size(), this);
//         this.hosts = hosts;
//         this.id = id;
//         this.ip = ip;
//         this.port = port;
//         this.s_count = 0;
//         this.d_count = 0;
//     }

//     public void broadcast(MessageZip m) {
//         for (Host host : hosts) { //we need to send this message to all hosts
//             System.out.println("Broadcasting (" + m.getId() + "," + m.getM() + ") to host ID: " + host.getId());
//             if (host.getId() != this.id) { //for all hosts other than us
//                 System.out.println("This was sent to another process.");
//                 this.lower_layer.send(new Message(host.getIp(), host.getPort(), m.getId(), m.getM())); //send using perfect links
//             }
//             else { //for us, immediately deliver
//                 this.deliver(new Message(this.ip, this.port, m.getId(), m.getM()));
//                 System.out.println("This was sent to ourselves.");
//             }
//         }
//         this.s_count++;
//     }

//     public void start() {
//         this.lower_layer.start();
//     }

//     public void stop_() {
//         this.lower_layer.stop_();
//         System.out.println("Broadcasted " + this.s_count + " many messages.");
//         System.out.println("Delivered " + this.d_count + " many messages.");
//     }

//     public void deliver(Message m) {
//         //this.upper_layer.deliver(m);
//         this.d_count++;
//     }
// }

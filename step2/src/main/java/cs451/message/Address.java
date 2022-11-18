package cs451.message;

import cs451.message.Message;

public class Address {

    private String ip;
    private int port;
    
    public Address(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public Address(Message m) {
        this.ip = m.getIp();
        this.port = m.getPort();
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        
        if (o == null)
            return false;
        
        if (this.getClass() != o.getClass())
            return false;

        Address a = (Address) o;

        return this.getIp().equals(a.getIp()) && this.getPort() == a.getPort();
    }

    @Override
    public int hashCode() {
        //port is in range 11001 - 11128
        return (this.port - 11001) + 128 * this.ip.hashCode(); //remove -11001 for performance
    }
}

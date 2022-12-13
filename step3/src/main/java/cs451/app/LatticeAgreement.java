package cs451.app;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import cs451.Host;
import cs451.Constants;
import cs451.app.Application;

/*
 * Single-shot LA; several instances used by APP in parallel
 */
public class LatticeAgreement {

    private Application app;
    private Set<Integer> proposed_value;
    private Set<Integer> accepted_value;
    private int active_proposal_number;
    private int ack_count;
    private int nack_count;
    private int f;
    private boolean active;
    private int lattice_number;
    private byte my_id;

    public LatticeAgreement(HashMap<Byte, Host> hosts_map, Host self, int lattice_number, Application app) {
        this.app = app;
        this.accepted_value = new ConcurrentHashMap<Integer, Boolean>().newKeySet();
        this.active_proposal_number = 0;
        this.f = (hosts_map.size() - 1) / 2; // n=2f+1 => f=(n-1)/2
        this.active = true;
        this.lattice_number = lattice_number;
        this.my_id = (byte) self.getId();
    }

    public void propose(Set<Integer> proposal) {
        this.proposed_value = proposal;
        this.active_proposal_number++;
        this.ack_count = 0;
        this.nack_count = 0;
        this.deliverProposal(this.active_proposal_number, this.proposed_value, this.my_id);
        this.app.propose(this.lattice_number, this.active_proposal_number, this.proposed_value);
    }

    public void deliverAck(int proposal_number) {
        if (proposal_number == this.active_proposal_number && this.active) {
            this.ack_count++;

            if (this.nack_count > 0 && (this.ack_count + this.nack_count) > this.f) {
                this.propose(this.proposed_value);
            }

            if (this.ack_count > this.f) {
                this.active = false;
                this.app.decide(this.lattice_number, this.proposed_value);
                // System.out.println("deliverack() : DECIDED (lattice_number=" + this.lattice_number + ") = " + this.proposed_value);
            }
        }
    }

    public void deliverNack(int proposal_number, Set<Integer> value) {
        if (proposal_number == this.active_proposal_number && this.active) {
            this.proposed_value.addAll(value);
            this.nack_count++;

            if (this.nack_count > 0 && (this.ack_count + this.nack_count) > this.f) {
                this.propose(this.proposed_value);
            }
        }
    }

    public void deliverProposal(int proposal_number, Set<Integer> value, byte source) {
        if (value.containsAll(this.accepted_value)) {
            this.accepted_value = value;
            if (source != this.my_id) {
                this.app.sendAck(this.lattice_number, proposal_number, source);
            }
            else {
                this.deliverAck(proposal_number);
            }

        }
        else {
            this.accepted_value.addAll(value);
            if (source != this.my_id) {
                this.app.sendNack(this.lattice_number, proposal_number, this.accepted_value, source);
            }
            else {
                this.deliverNack(proposal_number, this.accepted_value);
            }
        }
    }
}

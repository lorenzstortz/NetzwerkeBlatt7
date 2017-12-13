package fileReceiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public abstract class Transition {
	abstract public State execute(Action input);
}

class SendAck0 extends Transition {
	public State execute(Action input) {
		//TODO send ack0
		return State.WAIT_FOR_PACKET_1;
	}
}


class SendAck1 extends Transition {
	public State execute(Action input) {
		//TODO send ack1
		return State.WAIT_FOR_PACKET_0;
	}
}

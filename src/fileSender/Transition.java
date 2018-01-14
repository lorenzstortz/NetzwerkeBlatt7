package fileSender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public abstract class Transition {
	abstract public State execute(Action input);
}

class Send0 extends Transition {
	public State execute(Action input) {
		DatagramPacket packet = FileSender.getNextPacket();
		FileSender.lastPacket = packet;
		FileSender.sendPacket(packet);
		return State.WAIT_FOR_ACK_0;
	}
}

class ReceivedAck0 extends Transition {
	public State execute(Action input) {
		return State.WAIT_FOR_CALL_1;
	}
}

class Send1 extends Transition {
	public State execute(Action input) {
		DatagramPacket packet = FileSender.getNextPacket();
		FileSender.lastPacket = packet;
		FileSender.sendPacket(packet);
		return State.WAIT_FOR_ACK_1;
	}
}

class ReceivedAck1 extends Transition {
	public State execute(Action input) {
		return State.WAIT_FOR_CALL_0;
	}
}

package fileReceiver;

public abstract class Transition {
	abstract public State execute(Action input);
}

class SendAck0 extends Transition {
	public State execute(Action input) {
		//TODO send ack0
		FileReceiver.sendAckPacket((byte)0);
		return State.WAIT_FOR_PACKET_1;
	}
}


class SendAck1 extends Transition {
	public State execute(Action input) {
		//TODO send ack1
		FileReceiver.sendAckPacket((byte)1);
		return State.WAIT_FOR_PACKET_0;
	}
}

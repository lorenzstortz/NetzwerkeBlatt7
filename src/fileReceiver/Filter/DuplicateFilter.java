package fileReceiver.Filter;

import java.io.IOException;
import java.net.DatagramPacket;


public class DuplicateFilter implements Filter{
	private static final double DUPLICATEPERCENT = 0.1;

	private DatagramPacket packetToSendAgain = null;

	private Filter filter;

	public DuplicateFilter (Filter filter) {
		this.filter = filter;
	}

	@Override
	public DatagramPacket receive() throws IOException {
		if (packetToSendAgain == null) {
			DatagramPacket packet = filter.receive();
			if (Math.random() < DUPLICATEPERCENT) {
				packetToSendAgain = packet;
			}
			return packet;
		} else {
			System.out.println("Duplicating package");
			DatagramPacket packet = packetToSendAgain;
			packetToSendAgain = null;
			return packet;
		}
		
	
	}
}

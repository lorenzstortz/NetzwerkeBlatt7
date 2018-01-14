package fileReceiver.Filter;

import java.io.IOException;
import java.net.DatagramPacket;


public class LostFilter implements Filter{
	private static final double PROBABILITYLOST = 0.1;

	private Filter filter;

	public LostFilter (Filter filter) {
		this.filter = filter;
	}
	
	@Override
	public DatagramPacket receive() throws IOException {
		DatagramPacket packet = filter.receive();
		while (Math.random() < PROBABILITYLOST) {
			//System.out.println("Loosing package");
			packet = filter.receive();
		}
		return packet;
	}


}

package fileReceiver.Filter;

import java.io.IOException;
import java.net.DatagramPacket;


public class BitErrorFilter implements Filter{
	
	private static final double BITERRORPERCENT = 0.05;
	
	private Filter filter;

	public BitErrorFilter (Filter filter) {
		this.filter = filter;
	}

	@Override
	public DatagramPacket receive() throws IOException {
		DatagramPacket packet = filter.receive();
		if (Math.random() < BITERRORPERCENT) {
			byte[] data = packet.getData();
			byte errorpattern = 0b00001111;
			
			data[(int) (data.length * 0.5f)] = (byte) (data[(int) (data.length * 0.5f)] ^ errorpattern);
			
			packet = new DatagramPacket(data, data.length, packet.getSocketAddress());
			
			System.out.println("Packet contains biterror");
			return packet;
		}
		return packet;
	}
}

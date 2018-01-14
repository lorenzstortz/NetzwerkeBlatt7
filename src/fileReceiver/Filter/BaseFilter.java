package fileReceiver.Filter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class BaseFilter implements Filter{
	
	private DatagramSocket socket;
	private DatagramPacket packet;
	private Filter filter;

	public BaseFilter(DatagramSocket socket, DatagramPacket packet) throws SocketException {
		this.socket = socket;
		this.packet = packet;
		filter = new BitErrorFilter(this);
		filter = new DuplicateFilter(filter);
		filter = new LostFilter(filter);
	}

	@Override
	public void finalize() {
		if (socket != null)
			socket.close();
	}

	@Override
	public DatagramPacket receive() throws IOException {
		socket.receive(packet);		
		return packet;
	}
	
	public DatagramPacket getPacket() throws IOException {
		packet = filter.receive();
		return packet;
	}
}

package fileReceiver.Filter;

import java.io.IOException;
import java.net.DatagramPacket;

public interface Filter {
	
	DatagramPacket receive() throws IOException;
}

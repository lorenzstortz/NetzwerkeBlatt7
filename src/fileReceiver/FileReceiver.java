package fileReceiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;


public class FileReceiver {
	private static State currentState;
	private static Transition[][] transition;
	
	private final static int PORT = 4711;
	private static int timeout = 1000;
	private final static int DATA = 1000;

	static final String PATH = "./origin/";

	public static void main(String[] args) throws IOException {
		initialize();
		
		try {
			udpReceiver();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	private static void initialize(){
		currentState = State.WAIT_FOR_PACKET_0;
		transition = new Transition[State.values().length][Action.values().length];
		transition[State.WAIT_FOR_PACKET_0.ordinal()][Action.SEND_ACK_0.ordinal()] = new SendAck0();
		transition[State.WAIT_FOR_PACKET_1.ordinal()][Action.SEND_ACK_1.ordinal()] = new SendAck1();
		System.out.println("INFO FSM constructed, current state: " + currentState);
		
	}

	private static void udpReceiver() throws SocketException {
		//TODO wait for packet with right alternating bit and return when got
		
		DatagramSocket UDPSocket = new DatagramSocket(PORT);
		UDPSocket.setSoTimeout(timeout);
		int bytes = DATA;
		long timeStart = 0;
		long timeEnd = 0;
		int data = 0;
		while (true) {
			timeStart = System.currentTimeMillis();

			// wait for packet
			DatagramPacket packet = new DatagramPacket(new byte[bytes], bytes);
			try {
				UDPSocket.receive(packet);
				timeEnd = System.currentTimeMillis();
				// get data
				InetAddress address = packet.getAddress();
				data += packet.getLength();
				System.out.printf("package from %s recieved %n", address);
			} catch (SocketTimeoutException e) {
				// timeout
				System.out.println("timeout");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}

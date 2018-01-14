package fileReceiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import fileReceiver.Filter.BaseFilter;


public class FileReceiver {
	private static State currentState;
	private static Transition[][] transition;

	private static int timeout = 1000;

	static final String PATH = "./destination/";


	private static Checksum checksum = new CRC32();

	private static final int PORT_FILE_RECEIVER = 4711;
	private static final int PORT_FILE_SENDER = 4712;

	private static final int CHECKSUM_LENGTH = 8;
	private static final int AB_OFFSET = 2;
	private static final String DELIMITER = ";";
	private static final int ACK_PACKAGE_SIZE = 2;
	private static final int TIMEOUT = 1000;

	private static final int HEADER = 10;
	private static final int DATA = 1000;
	private static final int PACKET_SIZE = 1010;

	private static byte[] currentPacket = new byte[PACKET_SIZE];

	private static int packetCounter = 0;

	static DatagramSocket receiveSocket;

	private static InetAddress receivedAddress;
	private static int receivedPort;

	//static DatagramSocket sendSocket;

	public static void main(String[] args) throws IOException {
		initialize();

		receiveSocket = new DatagramSocket(PORT_FILE_RECEIVER);
		//sendSocket = new DatagramSocket(PORT_FILE_SENDER);

		//state machine
		while(true){
			switch(currentState){
				case WAIT_FOR_PACKET_0:
					if(receivePacket() == 0 && checkCRC(currentPacket))
					processAction(Action.SEND_ACK_0);
					break;
				case WAIT_FOR_PACKET_1:
					if(receivePacket() == 1 && checkCRC(currentPacket))
					processAction(Action.SEND_ACK_1);
					break;
			}
		}
	}

	private static void initialize() {
		currentState = State.WAIT_FOR_PACKET_0;
		transition = new Transition[State.values().length][Action.values().length];
		transition[State.WAIT_FOR_PACKET_0.ordinal()][Action.SEND_ACK_0.ordinal()] = new SendAck0();
		transition[State.WAIT_FOR_PACKET_1.ordinal()][Action.SEND_ACK_1.ordinal()] = new SendAck1();
		System.out.println("INFO FSM constructed, current state: " + currentState);

	}

	/**
	 * * Process a action (a condition has occurred). * @param input Message or
	 * condition that has occurred.
	 */
	public static void processAction(Action input) {
		System.out.println("INFO Received " + input + " in state " + currentState);
		Transition trans = transition[currentState.ordinal()][input.ordinal()];
		if (trans != null) {
			currentState = trans.execute(input);
		}
		System.out.println("INFO State: " + currentState);
	}

	///Returns the alternating bit
	private static byte receivePacket( ){
		while(true){
			try{
				DatagramPacket packet =  new DatagramPacket(currentPacket, ACK_PACKAGE_SIZE);
				receiveSocket.setSoTimeout(TIMEOUT);
				receiveSocket.receive(packet);
				if(receivedAddress == null){
					receivedAddress = packet.getAddress();
					receivedPort = packet.getPort();
				}
				System.out.println("Receceived a package with alternating bit:" + packet.getData()[1]);
				return packet.getData()[1];
			} catch (SocketTimeoutException e) {
				// resend Packet
			}catch (SocketException e){
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private static boolean checkCRC(byte[] packet){
		byte[] data = Arrays.copyOfRange(packet,HEADER, packet.length);
		
		//get checksum
		checksum.reset();
		checksum.update(data,0,data.length);
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		byte[] checksumBytes = buffer.putLong(checksum.getValue()).array();
		
		//check
		for(int i = 0; i < CHECKSUM_LENGTH; i++){
			if (packet[i+ AB_OFFSET] != checksumBytes[i]) {
				return false;
			}	
		}
		return true;
	}

	//Should be called with 0 or 1 accordingly
	@SuppressWarnings("Duplicates")
	public static void sendAckPacket(byte bit){
		byte[] content = new byte[ACK_PACKAGE_SIZE];
		content[1] = bit;
		DatagramPacket packet = new DatagramPacket(content, ACK_PACKAGE_SIZE, receivedAddress, PORT_FILE_SENDER);
		try (DatagramSocket dSocket = new DatagramSocket()) {
			long timeStart = System.currentTimeMillis();
			dSocket.send(packet);
			System.out.print("ACK " + bit+ " packet send to" + packet.getAddress() );

		} catch (SocketException e) {
			System.out.println("Can�t connect to server.");
		} catch (IOException e) {
			System.out.println("Can�t send to server.");
		}
	}
}

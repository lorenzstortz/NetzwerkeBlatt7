package fileReceiver;

import Wrapper.FileWrapper;
import fileReceiver.Filter.BaseFilter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


public class FileReceiver {
	private static State currentState;
	private static Transition[][] transition;

	private static int timeout = 1000;
	
	private static long timeStart = 0;
	private static long timeEnd = 0;
	
	private static byte[] finalData;

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

	private static boolean running = true;

	//static DatagramSocket sendSocket;

	private static ByteArrayOutputStream byos;

	public static void main(String[] args) {
		while (true) {
			run();
		}
	}
	
	private static void run() {
		initialize();
		try {
			receiveSocket = new DatagramSocket(PORT_FILE_RECEIVER);
		} catch (SocketException e) {
			System.out.println("Can´t connect to socket");
		}
		
		//sendSocket = new DatagramSocket(PORT_FILE_SENDER);
		byos = new ByteArrayOutputStream();
		//state machine
		while(running){
			switch(currentState){
				case WAIT_FOR_PACKET_0:
					if(receivePacket() == 0 && checkCRC(currentPacket)){
						saveCurrentPacket();
						processAction(Action.SEND_ACK_0);
					}
					break;
				case WAIT_FOR_PACKET_1:
					if(receivePacket() == 1 && checkCRC(currentPacket)){
						saveCurrentPacket();
						processAction(Action.SEND_ACK_1);
					}
					break;
			}
		}
		receiveSocket.close();
		running = true;
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
		//System.out.println("INFO Received " + input + " in state " + currentState);
		Transition trans = transition[currentState.ordinal()][input.ordinal()];
		if (trans != null) {
			currentState = trans.execute(input);
		}
		//System.out.println("INFO State: " + currentState);
	}

	///Returns the alternating bit
	private static byte receivePacket() {
		while(true){
			try{
				DatagramPacket packet =  new DatagramPacket(currentPacket, PACKET_SIZE);
				//receiveSocket.setSoTimeout(TIMEOUT);
				packet = new BaseFilter(receiveSocket,packet).getPacket();
				//receiveSocket.receive(packet);
				if(receivedAddress == null){
					receivedAddress = packet.getAddress();
					receivedPort = packet.getPort();
				}	
				currentPacket = packet.getData();
				//System.out.println("Receceived a package with alternating bit:" + packet.getData()[1]);
				if (timeStart == 0) {
					timeStart = System.currentTimeMillis();
				}
				timeEnd = System.currentTimeMillis();
				return packet.getData()[1];
			} catch (SocketTimeoutException e) {
				// resend Packet, ne nich wirklich
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
			//System.out.print("ACK " + bit+ " packet send to" + packet.getAddress() );

		} catch (SocketException e) {
			System.out.println("Canï¿½t connect to server.");
		} catch (IOException e) {
			System.out.println("Canï¿½t send to server.");
		}
	}
	
	public static void saveCurrentPacket() {		
		
		byte[] data = Arrays.copyOfRange(currentPacket, HEADER, PACKET_SIZE);
		++packetCounter;
		//System.out.println("Saving package Nr. :" + packetCounter);
        if (currentPacket[0] == 1) {
        	//last packet
			//FInd Delimiter and search for consequent zeros
			byte delimiter = 59;
			int pos = 0;
			//boolean truepos;
			for(int i = 0; i < data.length; i++){
				if(data[i] == delimiter){
					pos = i;
				}
			}

			data = Arrays.copyOfRange(data, 0, pos);
			try {
				byos.write(data);
			} catch (IOException e) {
				System.out.println("Something bad happened");
			}
        	System.out.println("Save file");
        	finalData = byos.toByteArray();
        	saveFile();
        	running = false;
        	return;
        }

		try {
			byos.write(data);
		} catch (IOException e) {
			System.out.println("Something bad happened");
		}
	}
	
	public static void saveFile() {
		
		ByteArrayInputStream bis = new ByteArrayInputStream(finalData);
		try {
			ObjectInputStream ois = new ObjectInputStream(bis);
			FileWrapper f = (FileWrapper) ois.readObject();
			Files.write(Paths.get(PATH + f.getFileName()), f.getFileData());
			//calculate goodput
			double difTime = timeEnd - timeStart;
			System.out.printf("Throughput: %.3f MBits/s %n %n", (f.getFileData().length * 8 / difTime) / 1000);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("couldn't parse data");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("couldn't parse data");
		}
		
	}
}

package fileSender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class FileSender {

	private static State currentState;
	// 2D array defining all transitions that can occur
	private static Transition[][] transition;
	static final int HEADER = 10;
	static final int DATA = 1000;
	static final int PORT = 4711;
	static final String PATH = "./origin/";
	private static InetAddress ia;
	private static String fileName;
	private static String targetHost;
	private static Queue<DatagramPacket> packets = new LinkedBlockingQueue<DatagramPacket>();
	private static Checksum checksum = new CRC32();

	private static final int CHECKSUM_LENGTH = 8;
	private static final int AB_OFFSET = 2;
	private static final String DELIMITER = ";";

	public static void main(String[] args) throws IOException, InterruptedException {	
		
		initialize();
		
		processAction(Action.SENT_0);
		while(true){

		}
	
	}
	
	private static void initialize() {
		currentState = State.WAIT_FOR_CALL_0;
		transition = new Transition[State.values().length][Action.values().length];
		transition[State.WAIT_FOR_CALL_0.ordinal()][Action.SENT_0.ordinal()] = new Send0();
		transition[State.WAIT_FOR_ACK_0.ordinal()][Action.RECEIVED_ACK_0.ordinal()] = new ReceivedAck0();
		transition[State.WAIT_FOR_CALL_1.ordinal()][Action.SENT_1.ordinal()] = new Send1();
		transition[State.WAIT_FOR_ACK_1.ordinal()][Action.RECEIVED_ACK_1.ordinal()] = new ReceivedAck1();
		System.out.println("INFO FSM constructed, current state: " + currentState);
		
		Scanner sc = new Scanner(System.in);
		//System.out.println("Enter filename:");
		//fileName = sc.next();
		fileName = "test.zip";
		//System.out.println("Enter target host:");
		//targetHost = sc.next();
		targetHost = "127.0.0.1";
		sc.close();
		
		try {
			ia = InetAddress.getByName(targetHost);
			Path file = Paths.get(PATH + fileName);
			byte[] rawData = Files.readAllBytes(file);

			byte [] parsedData = getWrappedFileAsByteArray(fileName, rawData);

			byte[] header = new byte[HEADER];
			byte[] packet = new byte[HEADER + DATA];
			

			//Construct packages
			int countDataBytes = DATA;

			while (countDataBytes < parsedData.length) {
				for (int i = 0; i < header.length; i++) {
					packet[i] = header[i];
				}

				//dirty approach for testing

				if(countDataBytes / DATA % 2 == 0){ //set alternating bit to 0
					packet[1] = 0;
				}
				else { //set alternating bit to 1
					packet[1] = 1;
				}


				//fill Array with Data
				int j = 0;
				for (int i = countDataBytes - DATA; i < countDataBytes; i++) {

					packet[j + header.length] = parsedData[i];
					j++;
				}

				//Create checksum and apply it
				byte[] checksumData = getChecksum(Arrays.copyOfRange(packet,HEADER, packet.length)); // to is exclusive, no -1 necessary
				for(int i = 0; i < CHECKSUM_LENGTH; i++){
					packet[i+ AB_OFFSET] = checksumData[i];
				}

				packets.offer(new DatagramPacket(packet, packet.length, ia, PORT));
				packet = new byte[HEADER + DATA];
				countDataBytes += DATA;
			}
			
			System.out.println(packet.length);
			
		} catch (UnknownHostException e ){
			System.out.println("Unkown host");
		} catch (IOException e) {
			System.out.println("Can�t connect to server");
		}

	}

	private static byte[] getWrappedFileAsByteArray(String fileName, byte[] data){
		FileWrapper f = new FileWrapper(fileName,data);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(f);
			return bos.toByteArray();

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("couldn't parse data");
			return null;
		}
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
	
	public static DatagramPacket getNextPacket() {
		return packets.poll();
	}

	private static byte[] getChecksum(byte[] data) {
		checksum.reset();
		checksum.update(data, 0, data.length);
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		byte[] checksumBytes = buffer.putLong(checksum.getValue()).array();
		return checksumBytes;
	}
	
}

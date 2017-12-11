package fileSender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class FileSender {

	private static State currentState;
	// 2D array defining all transitions that can occur
	private static Transition[][] transition;
	static final int HEADER = 10;
	static final int DATA = 1048576;
	static final int PORT = 4711;
	static final String PATH = "C:/Users/siebe/Desktop/Hochschule/Semester 3/Netzwerke/Praktikum/Blatt 7/";
	private static InetAddress ia;
	private static String fileName;
	private static String targetHost;

	public static void main(String[] args) throws IOException, InterruptedException {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter filename:");
		fileName = sc.next();
		System.out.println("Enter target host:");
		targetHost = sc.next();
		sc.close();
		
		FileSender fs = new FileSender(targetHost, fileName);
		fs.processAction(Action.SENT_0);
	}
	
	FileSender(String targetHost, String fileName) {
		try {
			ia = InetAddress.getByName(targetHost);
			Path file = Paths.get(PATH + fileName);
			byte[] data = Files.readAllBytes(file);
			byte[] header = new byte[HEADER];
			byte[] packet = new byte[header.length + data.length];
			for (int i = 0; i < packet.length; ++i) {
				packet[i] = i < header.length ? header[i] : data[i - data.length];
			}
			System.out.println(packet.length);
			
		} catch (UnknownHostException e ){
			
		} catch (IOException e) {
			
		}
				
		currentState = State.WAIT_FOR_CALL_0;
		transition = new Transition[State.values().length][Action.values().length];
		transition[State.WAIT_FOR_CALL_0.ordinal()][Action.SENT_0.ordinal()] = new Send0();
		transition[State.WAIT_FOR_ACK_0.ordinal()][Action.RECEIVED_ACK_0.ordinal()] = new ReceivedAck0();
		transition[State.WAIT_FOR_CALL_1.ordinal()][Action.SENT_1.ordinal()] = new Send1();
		transition[State.WAIT_FOR_ACK_1.ordinal()][Action.RECEIVED_ACK_1.ordinal()] = new ReceivedAck1();
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
	
}

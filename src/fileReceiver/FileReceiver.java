package fileReceiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class FileReceiver {

	private final static int PORT = 4711;
	private static int timeout = 1000;
	
	public static void main(String[] args) throws IOException {
	
		try {
			udpReciever();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private static void udpReciever() throws SocketException {
		DatagramSocket UDPSocket = new DatagramSocket(PORT);
		UDPSocket.setSoTimeout(timeout);
		int bytes = 1400;
		long timeStart = 0;
		long timeEnd = 0;
		int data = 0;
		while (true) {
			timeStart = System.currentTimeMillis();

			// wait for packet
			DatagramPacket packet = new DatagramPacket(new byte[bytes], bytes);
			try {
				UDPSocket.receive(packet);
			} catch (SocketTimeoutException e) {
				// timeout
				break;
			} catch (IOException e) {
				e.printStackTrace();
			}
			timeEnd = System.currentTimeMillis();

			// get data
			InetAddress address = packet.getAddress();
			data += packet.getLength();
			// System.out.printf("package no. %d from %s recieved %n", count,
			// address);

		}

		double difTime = timeEnd - timeStart;
		System.out.printf("Data recieved: %d bytes %n", data);
		System.out.printf("Time difference: %.2f s %n", difTime / 1000);
		System.out.printf("Throughput: %.0f kbits/s %n %n", (data / difTime) * 8);

	}
}

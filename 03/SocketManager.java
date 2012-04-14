import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;

public class SocketManager extends Thread {

	private Queue<DatagramPacket> pong_packets = new LinkedList<DatagramPacket>();
	private Queue<DatagramPacket> node_packets = new LinkedList<DatagramPacket>();
	private DatagramSocket socket;
	private boolean running = true;

	SocketManager(int port) throws SocketException {
		this.socket = new DatagramSocket(port);
		socket.setSoTimeout(Application.TIMEOUT_MS);
	}

	DatagramSocket getSocket() {
		return this.socket;
	}

	public DatagramPacket getPongPacket() {
		return findInPackets(pong_packets);
	}

	public DatagramPacket getNodePacket() {
		return findInPackets(node_packets);
	}

	public void run() {
		while (running) {
			try {
				receivePacket();
			} catch (SocketTimeoutException e) {
				continue;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void endThread() {
		running = false;
	}

	private DatagramPacket findInPackets(Queue<DatagramPacket> packets) {
		return packets.poll();
	}

	private void receivePacket() throws IOException {
		DatagramPacket p = new DatagramPacket(
				new byte[Application.MAX_PACKAGE_BYTES],
				Application.MAX_PACKAGE_BYTES);
		socket.receive(p);

		// classify packet and put it into corresponding queue
		String data = new String(p.getData(), p.getOffset(), p.getLength());
		if (data.length() >= 4 && data.substring(0, 4).equals("Pong"))
			pong_packets.add(p);
		else
			// by default, a packet is a node packet
			node_packets.add(p);
	}

	public static void main(String[] args) {
		SocketManager manager = null;
		try {
			manager = new SocketManager(5000);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		manager.start();
		System.out.println("Manager started");

		while (manager.getPongPacket() == null)
			;
		System.out.println("Pong received");

		while (manager.getNodePacket() == null)
			;
		System.out.println("Node received");

		manager.endThread();
		try {
			manager.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author csak7117
 * 
 */
public class ServiceLocator {
	private final int message_port;
	private final int response_port;
	private final String broadcast_address_str;
	private final DatagramSocket socket;

	/**
	 * @param broadcast
	 * @param out_port
	 * @param in_port
	 */
	public ServiceLocator(String broadcast, int out_port, DatagramSocket socket) {
		this.message_port = out_port;
		this.response_port = socket.getLocalPort();
		this.broadcast_address_str = broadcast;
		this.socket = socket;
	}

	public Node locateAnnouncer() throws IOException {

		// DatagramSocket in_socket = new DatagramSocket(response_port);
		// DatagramSocket out_socket = new DatagramSocket();

		InetAddress broadcast_address = InetAddress
				.getByName(broadcast_address_str);

		String message = "Ping," + Integer.toString(response_port) + "\n";
		byte[] message_data = message.getBytes();

		DatagramPacket packet = new DatagramPacket(message_data,
				message_data.length, broadcast_address, message_port);
		this.socket.send(packet);

		packet = new DatagramPacket(new byte[1024], 1024);
		this.socket.receive(packet);
		// identify sender
		InetAddress sender_address = packet.getAddress();
		int sender_port = packet.getPort();

		// get sent message
		int message_len = packet.getLength();
		message_data = packet.getData();
		// convert to string, removing trailing newline
		message = new String(message_data, 0, message_len - 1);

		System.out.println("Message from " + sender_address + " on port "
				+ sender_port + " of length " + message_len + ": " + message
				+ ".");

		// parse the message
		String[] st = message.split(",");

		// check if message has the right format
		if ((st.length != 2) || !st[0].equals("Pong")) {
			System.out.println("ServiceLocator: Message format error");
			return null;
		}

		// create node to return
		int port = Integer.parseInt(st[1]);
		System.out.println("First token: " + st[1]);
		Node node = new Node(sender_address.getHostAddress(), port, 0);

		return node;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ServiceAnnouncer s = new ServiceAnnouncer(4711, 4000);
		s.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		DatagramSocket socket;
		try {
			socket = new DatagramSocket(4712);
			ServiceLocator loc = new ServiceLocator("138.232.94.255", 4711,
					socket);
			try {
				loc.locateAnnouncer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

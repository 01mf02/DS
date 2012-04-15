import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

public class ServiceLocator {
	private final int message_port;
	private final int response_port;
	private final String broadcast_address_str;
	private final SocketManager socket;

	public ServiceLocator(String broadcast, int out_port, SocketManager socket) {
		this.message_port = out_port;
		this.response_port = socket.getSocket().getLocalPort();
		this.broadcast_address_str = broadcast;
		this.socket = socket;
	}

	public Node locateAnnouncer() throws IOException {

		InetAddress broadcast_address = InetAddress
				.getByName(this.broadcast_address_str);

		String message = "Ping," + Integer.toString(this.response_port) + "\n";
		byte[] message_data = message.getBytes();

		DatagramPacket packet = new DatagramPacket(message_data,
				message_data.length, broadcast_address, this.message_port);
		this.socket.getSocket().send(packet);

		while (true) {
			packet = this.socket.getPongPacket();

			if (packet == null) {
				continue;
			}

			// identify sender
			InetAddress sender_address = packet.getAddress();
			int sender_port = packet.getPort();

			// get sent message
			int message_len = packet.getLength();
			message_data = packet.getData();
			// convert to string, removing trailing newline
			message = new String(message_data, 0, message_len - 1);

			System.out.println("Message from " + sender_address + " on port "
					+ sender_port + " of length " + message_len + ": "
					+ message + ".");

			// parse the message
			String[] st = message.split(",");

			// check if message has the right format
			if ((st.length != 2) || !st[0].equals("Pong")) {
				System.out.println("ServiceLocator: Message format error");
				return null;
			}

			// create node to return
			int port = Integer.parseInt(st[1]);
			System.out.println("Port: " + port);

			// check if port equals message_port and sender_address equals
			// local address, and try again in that case
			if (port == this.message_port
					&& sender_address.getHostAddress().equals(
							InetAddress.getLocalHost().getHostAddress()))
				continue;

			Node node = new Node(sender_address.getHostAddress(), port, 0);

			return node;
		}
	}

	public static void main(String[] args) {

		ServiceAnnouncer s = new ServiceAnnouncer(4711, 4000);
		s.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			SocketManager socket = new SocketManager(4712);
			socket.start();

			ServiceLocator loc = new ServiceLocator("127.0.0.1", 4711, socket);
			try {
				loc.locateAnnouncer();
				socket.endThread();
				s.endThread();
				socket.join();
				s.join();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

	}
}

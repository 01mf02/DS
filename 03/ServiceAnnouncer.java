import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author csak7117
 * 
 */
public class ServiceAnnouncer {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int message_port = 4711;

		try {
			DatagramSocket in_socket = new DatagramSocket(message_port);
			DatagramSocket out_socket = new DatagramSocket();

			while (true) {
				// wait for request
				DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
				in_socket.receive(packet);

				// identify sender
				InetAddress sender_address = packet.getAddress();
				int sender_port = packet.getPort();

				// get sent message
				int message_len = packet.getLength();
				byte[] message_data = packet.getData();
				// convert to string, removing trailing newline
				String message = new String(message_data, 0, message_len - 1);

				System.out.println("Message from " + sender_address
						+ " on port " + sender_port + " of length "
						+ message_len + ": " + message + ".");

				try {
					int response_port = Integer.parseInt(message);

					String response = "Pong\n";
					byte[] response_data = response.getBytes();
					packet = new DatagramPacket(response_data,
							response_data.length, sender_address, response_port);
					out_socket.send(packet);
				} catch (NumberFormatException e) {
					System.out.println("Message is not an integer.");
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

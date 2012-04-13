import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author csak7117
 * 
 */
public class ServiceAnnouncer extends Thread {
	private final int in_port;
	private final int out_port;

	private boolean running = true;

	/**
	 * @param in_port
	 * @param out_port
	 * @param port
	 */
	public ServiceAnnouncer(int in_port, int out_port) {
		this.in_port = in_port;
		this.out_port = out_port;
	}

	/**
	 * @param args
	 */
	@Override
	public void run() {

		try {
			DatagramSocket in_socket = new DatagramSocket(in_port);
			// DatagramSocket out_socket = new DatagramSocket();

			while (running) {

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

					// parse the message
					String[] st = message.split(",");

					// check if message has the right format
					if ((st.length != 2) || !st[0].equals("Ping")) {
						System.out.println("Message Format Error");
						continue;
					}

					int response_port = Integer.parseInt(st[1]);

					// send back port
					String response = "Pong," + this.out_port + "\n";
					byte[] response_data = response.getBytes();
					packet = new DatagramPacket(response_data,
							response_data.length, sender_address, response_port);
					in_socket.send(packet);
				} catch (NumberFormatException e) {
					System.out.println("Message is not an integer.");
				}
			}
			in_socket.close();

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void endThread() {
		this.running = false;
	}
}

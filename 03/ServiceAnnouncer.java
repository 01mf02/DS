import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ServiceAnnouncer extends Thread {
	private final int in_port;
	private final int out_port;

	private boolean running = true;

	public ServiceAnnouncer(int in_port, int out_port) {
		this.in_port = in_port;
		this.out_port = out_port;
	}

	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket(in_port);

			while (running) {

				// wait for request
				DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
				socket.receive(packet);

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

					// parse message
					String[] st = message.split(",");

					// check if message has the right format
					if ((st.length != 2) || !st[0].equals("Ping")) {
						System.out
								.println("ServiceAnnouncer: Message format error");
						continue;
					}

					int response_port = Integer.parseInt(st[1]);

					// send back port
					String response = "Pong," + this.out_port + "\n";
					byte[] response_data = response.getBytes();
					packet = new DatagramPacket(response_data,
							response_data.length, sender_address, response_port);
					socket.send(packet);
				} catch (NumberFormatException e) {
					System.out.println("Message is not an integer.");
				}
			}
			socket.close();

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void endThread() {
		this.running = false;
	}
}

import java.io.IOException;
import java.net.*;

public class ServiceLocator
{
	private final int message_port;
	private final int response_port;
	private final String broadcast_address_str;
	
	public ServiceLocator(String broadcast, int out_port, int in_port) {
		this.message_port = out_port;
		this.response_port = in_port;
		this.broadcast_address_str = broadcast;
	}
	
	public InetAddress locateAnnouncer() {
		try {
			DatagramSocket in_socket = new DatagramSocket(response_port);
			DatagramSocket out_socket = new DatagramSocket();

			InetAddress broadcast_address =
				InetAddress.getByName(broadcast_address_str);

			String message = Integer.toString(response_port) + "\n";
			byte[] message_data = message.getBytes();

			DatagramPacket packet =
				new DatagramPacket(message_data, message_data.length,
								   broadcast_address, message_port);
			out_socket.send(packet);


			packet = new DatagramPacket(new byte[1024], 1024);
			in_socket.receive(packet);
			// identify sender
			InetAddress sender_address = packet.getAddress();
			int sender_port = packet.getPort();

			// get sent message
			int message_len = packet.getLength();
			message_data = packet.getData();
			// convert to string, removing trailing newline
			message = new String(message_data, 0, message_len-1);

			System.out.println("Message from " + sender_address +
							   " on port " + sender_port +
							   " of length " + message_len +
							   ": " + message + ".");
			
			return sender_address;
		}
		catch (SocketException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		
		return null;
	}
	
	public static void main(String[] args) {
		ServiceLocator loc =
			new ServiceLocator("138.232.94.255", 4711, 4712);
		InetAddress address = loc.locateAnnouncer();		
	}
}

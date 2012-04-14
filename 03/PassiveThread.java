import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;

public class PassiveThread extends SuperThread {

	private final boolean PULL_MODE = false;

	public PassiveThread(SocketManager socket, View view) {
		super(socket, view);
	}

	public void run() {
		while (isRunning()) {
			try {
				DatagramPacket packet = receivePacket();
				// as long as there is no incoming packet, try again
				if (packet == null)
					continue;

				View v = unpackData(packet);

				if (this.PULL_MODE) {
					View buf_out = this.view.getBuffer(this.socket.getSocket()
							.getPort());

					// send data to node from which we received original packet
					sendData(this.socket.getSocket(), packet.getAddress(),
							packet.getPort(), buf_out);
				}
				this.view
						.select(v, Application.H, Application.S, Application.C);
				this.view.age();
			} catch (SocketTimeoutException e) {
				continue;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class PassiveThread extends SuperThread {

	private final boolean PULL_MODE = true;

	public PassiveThread(DatagramSocket sock, View view) {
		super(sock, view);
	}

	public void run() {
		while (isRunning()) {
			try {
				DatagramPacket packet = receivePacket();
				View v = unpackData(packet);

				if (this.PULL_MODE) {
					View buf_out = this.view.getBuffer(this.socket.getPort());

					// send data to node from which we received original packet
					sendData(this.socket, packet.getAddress(),
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

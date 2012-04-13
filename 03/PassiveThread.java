import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class PassiveThread extends SuperThread {

	private final boolean PULL_MODE = true;

	public PassiveThread(DatagramSocket sock, View view) {
		super(sock, view);
	}

	public void run() {
		while (isRunning()) {
			try {
				View v = receiveBuffer();

				if (this.PULL_MODE) {
					View buf_out = this.view.getBuffer(this.socket.getPort());

					// TODO: don't send stuff to random node, but to original
					// package sender!

					// get random node
					Node n = getNode();
					if (n == null)
						continue;

					sendData(this.socket,
							InetAddress.getByName(n.getAddress()), n.getPort(),
							buf_out);
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

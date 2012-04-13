import java.io.IOException;
import java.net.DatagramPacket;
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
			// + 1 because of the ":" delimiter
			byte[] buf_in = new byte[Application.MAX_CAP
					* Application.MAX_BYTES + 1];
			DatagramPacket p = new DatagramPacket(buf_in, buf_in.length);
			try {
				this.sock.receive(p);
				if (this.PULL_MODE) {
					View buf_out = this.view.getBuffer(this.sock.getPort());

					// get random node
					Node n = getNode();
					if (n == null)
						continue;

					SendView.sendData(this.sock,
							InetAddress.getByName(n.getAddress()), n.getPort(),
							buf_out);
				}
				this.view.mergeViews(SendView.unpackData(p), Application.H,
						Application.S, Application.C);
				this.view.age();
			} catch (SocketTimeoutException e) {
				continue;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

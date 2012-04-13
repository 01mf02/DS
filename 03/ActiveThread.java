import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

class ActiveThread extends SuperThread {
	private final boolean PUSH_MODE = true;
	private final boolean PULL_MODE = false;

	public ActiveThread(DatagramSocket sock, View view) {
		super(sock, view);

		System.out.println("ActiveThread constructed.");
	}

	public void run() {
		while (isRunning()) {
			Node n = getNode();
			if (n == null)
				continue;

			try {
				if (this.PUSH_MODE) {
					View buf_out = this.view.getBuffer(this.sock.getPort());
					System.out.println("Address: " + n.getAddress());
					SendView.sendData(this.sock,
							InetAddress.getByName(n.getAddress()), n.getPort(),
							buf_out);
				} else {
					// send empty view to trigger response
					SendView.sendData(this.sock,
							InetAddress.getByName(n.getAddress()), n.getPort(),
							null);
				}
				if (this.PULL_MODE) {
					// + 1 because of the ":" delimiter
					byte[] buf_in = new byte[Application.MAX_CAP
							* Application.MAX_BYTES + 1];
					DatagramPacket p = new DatagramPacket(buf_in, buf_in.length);
					this.sock.receive(p);
					View v = SendView.unpackData(p);

					this.view.mergeViews(v, Application.H, Application.S,
							Application.C);
					this.view.age();
				}

			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (SocketTimeoutException e) {
				continue;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

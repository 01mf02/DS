import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

class ActiveThread extends SuperThread {
	private final boolean PUSH_MODE = true;
	private final boolean PULL_MODE = true;

	public ActiveThread(SocketManager socket, View view) {
		super(socket, view);

		System.out.println("ActiveThread constructed.");
	}

	public void run() {
		while (this.isRunning()) {
			try {
				sleep(Application.CYCLE_LENGTH_MS);

				Node n = this.getNode();
				if (n == null) {
					continue;
				}

				InetAddress n_address = InetAddress.getByName(n.getAddress());
				int n_port = n.getPort();

				if (this.PUSH_MODE) {
					View buf_out = this.view.getBuffer(this.socket.getSocket()
							.getLocalPort());
					sendData(this.socket.getSocket(), n_address, n_port,
							buf_out);
				} else
					// send empty view to trigger response
					sendData(this.socket.getSocket(), n_address, n_port, null);

				if (this.PULL_MODE) {
					DatagramPacket packet = this.receivePacket();
					if (packet == null)
						continue;

					View v = unpackData(packet);

					this.view.select(v, Application.H, Application.S,
							Application.C);
					this.view.age();
				}

			} catch (SocketTimeoutException e) {
				continue;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

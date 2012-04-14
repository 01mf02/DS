import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

class ActiveThread extends SuperThread {
	private final boolean PUSH_MODE = false;
	private final boolean PULL_MODE = false;

	public ActiveThread(SocketManager socket, View view) {
		super(socket, view);

		System.out.println("ActiveThread constructed.");
	}

	public void run() {
		while (isRunning()) {
			try {
				sleep(Application.CYCLE_LENGTH_MS);

				Node n = getNode();
				if (n == null)
					continue;

				if (this.PUSH_MODE) {
					View buf_out = this.view.getBuffer(this.socket.getSocket()
							.getPort());
					sendData(this.socket.getSocket(),
							InetAddress.getByName(n.getAddress()), n.getPort(),
							buf_out);
				} else {
					// send empty view to trigger response
					sendData(this.socket.getSocket(),
							InetAddress.getByName(n.getAddress()), n.getPort(),
							null);
				}
				if (this.PULL_MODE) {
					View v = unpackData(receivePacket());

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

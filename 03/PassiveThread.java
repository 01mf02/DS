
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author csak7117
 * 
 */
public class PassiveThread extends Thread {

	private final boolean PULL_MODE = true;
	private boolean running = true;
	private final DatagramSocket sock;
	private final View view;

	/**
	 * @param sock
	 * @param view
	 */
	public PassiveThread(DatagramSocket sock, View view) {
		this.sock = sock;
		this.view = view;
	}

	@Override
	public void run() {
		while (this.running) {
			// + 1 because of the ":" delimiter
			byte[] buf_in = new byte[Application.MAX_CAP
					* Application.MAX_BYTES + 1];
			DatagramPacket p = new DatagramPacket(buf_in, buf_in.length);
			try {
				this.sock.receive(p);
				if (this.PULL_MODE) {
					View buf_out = this.view.getBuffer(this.sock.getPort());
					Node n = this.view.selectNode();
					SendView.sendData(this.sock, InetAddress.getByName(n
							.getAddress()), Application.PORT, buf_out);
				}
				this.view.mergeViews(SendView.unpackData(p), Application.H,
						Application.S, Application.C);
				this.view.age();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 */
	public void endThread() {
		this.running = false;
	}
}

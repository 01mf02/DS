import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class SuperThread extends Thread {

	protected boolean running = true;
	protected final SocketManager socket;
	protected final View view;

	private static final String delimEntries = ";";
	private static final String delim = " ";
	private static final String delimPacket = ":";

	SuperThread(SocketManager socket, View view) {
		this.socket = socket;
		this.view = view;
	}

	public void endThread() {
		this.running = false;
	}

	protected boolean isRunning() {
		return this.running;
	}

	// get random node from view or try to find node via broadcast
	protected Node getNode() {
		// get random node
		Node node = this.view.selectNode();
		// if no nodes exist in view yet
		if (node == null) {
			// find node via broadcast on local network
			ServiceLocator locator = new ServiceLocator(
					Application.BROADCAST_ADDR, Application.BROADCAST_PORT,
					this.socket);
			try {
				node = locator.locateAnnouncer();
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return node;
	}

	protected DatagramPacket receivePacket() throws IOException {
		return this.socket.getNodePacket();
	}

	public static byte[] packData(View view) {
		if (view == null)
			// TODO: return an empty packet here!
			return null;

		ArrayList<Node> nodes = view.getNodes();
		StringBuilder b = new StringBuilder();
		Node node;
		for (int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			// append the address
			b.append(node.getAddress());
			// append a delimiter that parts the address and the port
			b.append(delim);
			// append the port
			b.append(node.getPort());
			// append a delimiter that parts the port and the age
			b.append(delim);
			// append the age
			b.append(node.getAge());
			// append a delimiter that parts different nodes
			b.append(delimEntries);
		}
		// append a last delimiter to signify that all nodes have been sent
		b.append(delimPacket);
		return b.substring(0).getBytes();
	}

	public synchronized static void sendData(DatagramSocket sock,
			InetAddress add, int port, View view) {
		byte[] buf = packData(view);
		DatagramPacket packet = new DatagramPacket(buf, buf.length, add, port);

		try {
			sock.send(packet);
		} catch (IOException e) {
			System.out.println("SendView: Error while sending partial view!");
			e.printStackTrace();
		}
	}

	public static View unpackData(DatagramPacket p) {
		// TODO: handle empty packet here!

		String data = new String(p.getData(), p.getOffset(), p.getLength());
		StringTokenizer st = new StringTokenizer(data, delim + delimEntries
				+ delimPacket, false);
		ArrayList<Node> nodes = new ArrayList<Node>();

		if (st.countTokens() % 3 != 0) {
			System.out.println("Failed parsing node data: " + data);
		}

		while (st.hasMoreTokens()) {
			String addr = st.nextToken();
			int port = Integer.parseInt(st.nextToken());
			int age = Integer.parseInt(st.nextToken());
			Node node = new Node(addr, port, age);
			nodes.add(node);
		}
		return new View(nodes);
	}

	// for test purposes
	public static void main(String[] args) throws UnknownHostException {
		String str = "1.1.1.1 123 0;2.2.2.2 1234 0;3.3.3.3 12345 1;:";
		DatagramPacket p = new DatagramPacket(str.getBytes(),
				str.getBytes().length, InetAddress.getByName("127.0.0.1"), 1222);
		View view = unpackData(p);
		ArrayList<Node> nodes = view.getNodes();
		for (int i = 0; i < nodes.size(); i++) {
			System.out.println(nodes.get(i).getAddress() + " "
					+ nodes.get(i).getPort() + " " + nodes.get(i).getAge());
		}

		System.out.println(new String(packData(view)));
	}
}

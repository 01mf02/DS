import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class SendView {

	public static final String delimEntries = ";";
	public static final String delim = " ";
	public static final String delimPacket = ":";

	public static byte[] packData(View view) {
		ArrayList<Node> nodes = view.getNodes();
		StringBuilder b = new StringBuilder();
		Node node;
		for (int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			// append the address
			b.append(node.getAddress());
			// append a delimiter that parts the address and the age
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
		String data = new String(p.getData(), p.getOffset(), p.getLength());
		StringTokenizer st = new StringTokenizer(data, delim + delimEntries
				+ delimPacket, false);
		ArrayList<Node> nodes = new ArrayList<Node>();
		Node node;
		String addr;
		int age;
		while (st.hasMoreTokens()) {
			addr = st.nextToken();
			age = Integer.parseInt(st.nextToken());
			node = new Node(addr, age);
			nodes.add(node);
		}
		return new View(nodes);
	}

	/*
	 * public static void main(String[] args) throws UnknownHostException {
	 * String str = "1.1.1.1 0;2.2.2.2 0;3.3.3.3 1;:"; DatagramPacket p = new
	 * DatagramPacket(str.getBytes(), str.getBytes().length,
	 * InetAddress.getByName("127.0.0.1"), 1222); View view = unpackData(p);
	 * ArrayList<Node> nodes = view.getNodes(); for (int i = 0; i < nodes.size();
	 * i++) { System.out.println(nodes.get(i).getAddress() + " " +
	 * nodes.get(i).getAge()); }
	 * 
	 * System.out.println(new String(packData(view))); }
	 */
}

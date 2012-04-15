import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;

public class Application {

	public static final int H = 5;
	public static final int S = 5;
	public static final int C = 5;
	public static final int MAX_CAP = 10;

	// broadcast address and port where broadcast should be sent on
	public static final String BROADCAST_ADDR = "192.168.178.255";
	public static final int BROADCAST_PORT = 5000;

	// port of the first instance and number of total instances
	public static final int BASE_PORT = 12345;
	public static final int INSTANCES = 2;

	public static final int TIMEOUT_MS = 2000;
	public static final int CYCLE_LENGTH_MS = 1000;

	public static final int MAX_NODE_BYTES = "255.255.255.255 65535 10000;"
			.getBytes().length;
	// + 1 because of the ":" delimiter
	public static final int MAX_PACKAGE_BYTES = Application.MAX_CAP
			* Application.MAX_NODE_BYTES + 1;

	private static ArrayList<SuperThread> sthreads = new ArrayList<SuperThread>();
	private static ArrayList<SocketManager> sockets = new ArrayList<SocketManager>();

	public static void main(String[] args) {

		// respond to broadcasts and tell senders that we listen on BASE_PORT
		ServiceAnnouncer announcer = new ServiceAnnouncer(BROADCAST_PORT,
				BASE_PORT);
		announcer.start();

		try {
			for (int i = 0; i < INSTANCES; i++) {
				initInstance(BASE_PORT + i);
			}

			// run threads in background until user wants to quit
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			do {
				System.out.println("To stop the application, type: quit");
			} while (!in.readLine().equals("quit"));

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Quitting application ...");

			// clean up
			try {
				stopInstances();
				announcer.endThread();
				announcer.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("Threads cleaned up");
		}
	}

	private static void initInstance(int port) {
		SocketManager socket = null;
		try {
			// construct socket manager
			socket = new SocketManager(port);
			socket.start();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		// construct active/passive threads with respective view
		View view = new View();
		ActiveThread aThread = new ActiveThread(socket, view);
		PassiveThread pThread = new PassiveThread(socket, view);
		aThread.start();
		pThread.start();

		// add threads and socket managers to array lists for later disposal
		sthreads.add(aThread);
		sthreads.add(pThread);
		sockets.add(socket);
	}

	private static void stopInstances() throws InterruptedException {
		// close super threads
		Iterator<SuperThread> sitr = sthreads.iterator();
		while (sitr.hasNext()) {
			SuperThread sThread = sitr.next();
			sThread.endThread();
			sThread.join();
		}

		// close sockets and stop socket manager threads
		Iterator<SocketManager> mitr = sockets.iterator();
		while (mitr.hasNext()) {
			SocketManager socket = mitr.next();
			socket.endThread();
			socket.join();
			socket.getSocket().close();
		}
	}
}

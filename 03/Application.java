
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * @author csak7117
 * 
 */
public class Application {

	public static final int H = 5;
	public static final int S = 5;
	public static final int C = 5;
	public static final int PORT = 12346;
	public static final int MAX_CAP = 10;
	public static final String BROADCAST_ADDR = "138.232.94.255";
	public static final int BROADCAST_PORT = 5000;

	public static final int MAX_BYTES = "255.255.255.255 65535 10000;"
			.getBytes().length;

	private ArrayList<Thread> threads;
	private ArrayList<DatagramSocket> sockets;

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException,
			InterruptedException {

		DatagramSocket sock = null;

		try {
			sock = new DatagramSocket(PORT);

			View view = new View();
			ActiveThread aThread = new ActiveThread(sock, view);
			PassiveThread pThread = new PassiveThread(sock, view);
			aThread.start();
			pThread.start();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			do {
				System.out.println("To stop the application, type: quit");
			} while (!in.readLine().equals("quit"));

			System.out.println("Quitting application ...");

			// if the user typed "quit" wait until the threads have ended
			aThread.endThread();
			pThread.endThread();
			aThread.join();
			pThread.join();

			System.out.println("Threads cleaned up");
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(-1);
		} finally {
			sock.close();
		}
	}

	public static void initInstance(int port) {

	}
}

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Application {

	public static final int H = 5;
	public static final int PORT = 12347;
	public static final int MAX_CAP = 10;
	public static final int MAX_BYTES = "255.255.255.255 10000;".getBytes().length;

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
			System.out.println("To stop the application type: quit");
			while (!in.readLine().equals("quit")) {
				System.out.println("To stop the application type: quit");
			}
			// if the user typed "quit" wait until the threads have ended
			aThread.endThread();
			pThread.endThread();
			aThread.join();
			pThread.join();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(-1);
		} finally {
			sock.close();
		}

	}
}

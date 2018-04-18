package app;

import java.util.ArrayList;
import java.util.List;

import com.scg.domain.TimeCard;
import com.scg.net.client.InvoiceClient;

/**
 * The client application for Assignment08.
 * 
 * @author chq-alexs
 *
 */
public final class Assignment09 {

	/**
	 * The port for the server to listen on.
	 */
	public static final int DEFAULT_PORT = 10888;

	/**
	 * The port for the server to listen on.
	 */
	public static final String LOCAL_HOST = "127.0.0.1";

	/**
	 * Instantiates an InvoiceClient, provides it with a set of timecards to
	 * server the server and starts it running.
	 * 
	 * @param args
	 *            - Command line parameters - not used.
	 * @throws Exception
	 *             - if anything goes awry
	 */
	public static void main(String[] args) throws Exception {

		final List<TimeCard> timeCards = new ArrayList<TimeCard>();

		final InvoiceClient netClient1 = new InvoiceClient(LOCAL_HOST, DEFAULT_PORT, timeCards);
		final InvoiceClient netClient2 = new InvoiceClient(LOCAL_HOST, DEFAULT_PORT, timeCards);
		final InvoiceClient netClient3 = new InvoiceClient(LOCAL_HOST, DEFAULT_PORT, timeCards);

		netClient1.start();
		netClient2.start();
		netClient3.start();

		netClient1.join();
		netClient2.join();
		netClient3.join();

		InvoiceClient.sendShutdown(LOCAL_HOST, DEFAULT_PORT);
	}

}

package com.scg.net.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;
import com.scg.domain.ClientAccount;
import com.scg.domain.Consultant;

/**
 * The server for creation of account invoices based on time cards sent from the
 * client.
 * 
 * @author chq-alexs
 *
 */
public final class InvoiceServer {

	/**
	 * Invoice server port to use.
	 */
	private int port;
	/**
	 * List of clients.
	 */
	private List<ClientAccount> clientList;
	/**
	 * List of consultants
	 */
	private List<Consultant> consultantList;
	/**
	 * Output directory to be used for invoices.
	 */
	private String outputDirectoryName;

	/**
	 * Logger for logging activities by application.
	 */
	private static final Logger log = Logger.getLogger(InvoiceServer.class.getName());

	/**
	 * Sever socket.
	 */
	ServerSocket serverSocket;
	private static int processorNumber;

	/**
	 * Construct an InvoiceServer with a port.
	 * 
	 * @param port
	 *            - The port for this server to listen on
	 * 
	 * @param clientList
	 *            - the initial list of clients
	 * 
	 * @param consultantList
	 *            - the initial list of consultants
	 * 
	 * @param outputDirectoryName
	 *            - the directory to be used for files output by commands
	 * 
	 */
	public InvoiceServer(int port, List<ClientAccount> clientList, List<Consultant> consultantList,
			String outputDirectoryName) {
		this.port = port;
		this.clientList = clientList;
		this.consultantList = consultantList;
		this.outputDirectoryName = outputDirectoryName;
		Runtime.getRuntime()
				.addShutdownHook(new InvoiceServerShutdownHook(clientList, consultantList, outputDirectoryName));
	}

	/**
	 * Run this server, establishing connections, receiving commands, and
	 * sending them to the CommandProcesser.
	 * 
	 */
	public void run() {

		Socket client = null;

		try {
			serverSocket = new ServerSocket(this.port);

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		while (!serverSocket.isClosed()) {

			try {
				client = serverSocket.accept();

				processorNumber++;

				log.info("New Connection Recieved Created Thread #" + processorNumber);

			} catch (IOException e) {
				e.printStackTrace();
			}
			final CommandProcessor cmdProc = new CommandProcessor(client, "*** Command Processor " + processorNumber,
					clientList, consultantList, this);

			final File serverDir = new File(outputDirectoryName + Integer.toString(processorNumber));

			cmdProc.setOutPutDirectoryName(serverDir.getAbsolutePath());
			
			final Thread thread = new Thread(cmdProc, "CommandProcessor_" + processorNumber);
			thread.start();

		}

	}

	/**
	 * Shutdown the server.
	 * 
	 */
	void shutdown() {
		if (serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}

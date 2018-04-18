package com.scg.net.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.scg.domain.ClientAccount;
import com.scg.domain.Consultant;
import com.scg.domain.Invoice;
import com.scg.domain.TimeCard;
import com.scg.net.cmd.AddClientCommand;
import com.scg.net.cmd.AddConsultantCommand;
import com.scg.net.cmd.AddTimeCardCommand;
import com.scg.net.cmd.Command;
import com.scg.net.cmd.CreateInvoicesCommand;
import com.scg.net.cmd.DisconnectCommand;
import com.scg.net.cmd.ShutdownCommand;

/**
 * The command processor for the invoice server. Implements the receiver role in
 * the Command design pattern.
 * 
 * @author chq-alexs
 *
 */
public final class CommandProcessor implements Runnable {
	/**
	 * Logger
	 */
	private static final Logger log = Logger.getLogger(InvoiceServer.class.getName());

	/**
	 * Count thread
	 */
	private static int count;

	/**
	 * Thread
	 */
	private Thread t;

	/**
	 * Thread Name
	 */
	private String threadName;

	/**
	 * Socket connection for the server.
	 */
	private Socket connection;

	/**
	 * List of clients.
	 */
	private static List<ClientAccount> clientList;

	/**
	 * List of consultants.
	 */
	private static List<Consultant> consultantList;

	/**
	 * Server to be used.
	 */
	private InvoiceServer server;

	/**
	 * Output directory for the invoices
	 */
	private String outPutDirectoryName;

	/**
	 * List of timecards used to create invoices.
	 */
	private List<TimeCard> timeCards = new ArrayList<TimeCard>();

	/**
	 * Construct a CommandProcessor.
	 * 
	 * @param connection
	 *            - the Socket connecting the server to the client.
	 * 
	 * @param clientList
	 *            - the ClientList to add Clients to.
	 * 
	 * @param consultantList
	 *            - the ConsultantList to add Consultants to.
	 * 
	 * @param server
	 *            - the server that created this command processor
	 * 
	 */
	public CommandProcessor(Socket connection, String name, List<ClientAccount> clientList,
			List<Consultant> consultantList, InvoiceServer server) {

		this.connection = connection;
		CommandProcessor.clientList = clientList;
		CommandProcessor.consultantList = consultantList;
		this.server = server;
		setOutPutDirectoryName(name);
		this.threadName = name;

	}

	/**
	 * Set the output directory name.
	 * 
	 * @param outPutDirectoryName
	 *            - the output directory name.
	 * 
	 */
	public void setOutPutDirectoryName(String outPutDirectoryName) {
		this.outPutDirectoryName = outPutDirectoryName;
	}

	/**
	 * Execute and AddTimeCardCommand.
	 * 
	 * @param command
	 *            - the command to execute.
	 * 
	 */
	public void execute(AddTimeCardCommand command) {
		log.info("ADDING TIMECARD DATE STARTING: " + command.getTarget().getWeekStartingDay() + " Proc:"
				+ this.threadName);

		if (!timeCards.contains(command.getTarget())) {
			timeCards.add(command.getTarget());
		}
	}

	/**
	 * Execute an AddClientCommand.
	 * 
	 * @param command
	 *            - the command to execute.
	 * 
	 */
	public void execute(AddClientCommand command) {
		synchronized (clientList) {
			if (!clientList.contains(command.getTarget())) {
				log.info("ADDING CLIENT " + command.getTarget() + "Processor " + threadName);

				clientList.add(command.getTarget());
			}
		}
	}

	/**
	 * Execute and AddConsultantCommand.
	 * 
	 * @param command
	 *            - the command to execute.
	 * 
	 */
	public void execute(AddConsultantCommand command) {
		synchronized (consultantList) {
			if (!consultantList.contains(command.getTarget())) {
				log.info("ADDING CONSULTANT " + command.getTarget() + "Processor " + threadName);

				consultantList.add(command.getTarget());
			}
		}

	}

	/**
	 * Execute a CreateInvoicesCommand.
	 * 
	 * @param command
	 *            - the command to execute.
	 * 
	 */
	public void execute(CreateInvoicesCommand command) {

		final File serverDir = new File(outPutDirectoryName);

		if (serverDir.exists() || serverDir.mkdirs()) {

			log.info("WRITING INVOICE");
			synchronized (clientList) {
				for (final ClientAccount account : clientList) {
					final Invoice invoice = new Invoice(account, command.getTarget().getMonth(),
							command.getTarget().getYear());

					for (final TimeCard currentTimeCard : timeCards) {

						invoice.extractLineItems(currentTimeCard);

					}

					if (invoice.getTotalHours() > 0) {
						String filePath = this.outPutDirectoryName + "\\" + account.getName() + "-"
								+ command.getTarget().getMonth() + ".txt";
						File theDir = new File(this.outPutDirectoryName);

						if (!theDir.exists()) {
							try {
								theDir.mkdir();
							} catch (SecurityException se) {
								
							}
						}

						File file = new File(filePath);
						if (!file.exists()) {
							try {
								file.createNewFile();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						try {
							PrintStream ps = new PrintStream(file);
							ps.append(invoice.toReportString());
							ps.close();

						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}

						try {
							FileWriter fw = new FileWriter(filePath);

							fw.write(invoice.toReportString());
							fw.close();

						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	/**
	 * Execute a DisconnectCommand.
	 * 
	 * @param command
	 *            - the input DisconnectCommand.
	 * 
	 */
	public void execute(DisconnectCommand command) {

		log.info("DISCONNECTING FROM SERVER " + threadName);

		try {
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Execute a ShutdownCommand. Closes any current connections, stops
	 * listening for connections and then terminates the server, without calling
	 * System.exit.
	 * 
	 * @param command
	 *            - the input ShutdownCommand.
	 * 
	 */
	public void execute(ShutdownCommand command) {

		log.info("SERVER SHUTDOWN " + threadName);

		try {
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.shutdown();

	}

	@Override
	public void run() {

		try {
			this.connection.shutdownOutput();

			InputStream is = this.connection.getInputStream();

			ObjectInputStream in = new ObjectInputStream(is);

			while (!this.connection.isClosed()) {
				Object obj = null;

				try {
					obj = in.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				if (obj == null) {
					this.connection.close();
				} else {
					if (obj instanceof Command<?>) {
						final Command<?> command = (Command<?>) obj;
						command.setReceiver(this);
						command.execute();
					}
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void start() {
		count++;

		this.outPutDirectoryName = this.outPutDirectoryName + count;
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}

	}

}

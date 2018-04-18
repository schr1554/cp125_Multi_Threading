package com.scg.net.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Logger;

import com.scg.domain.ClientAccount;
import com.scg.domain.Consultant;

/**
 * ShutdownHook for the InvoiceServer.
 * 
 * @author chq-alexs
 *
 */
public class InvoiceServerShutdownHook extends Thread {

	/**
	 * Logger
	 */
	private static final Logger log = Logger.getLogger(InvoiceServer.class.getName());

	/**
	 * The list of clients at shutdown.
	 */
	private List<ClientAccount> clientList;

	/**
	 * The list of consultants at shutdown.
	 */
	private List<Consultant> consultantList;

	/**
	 * The output directory to place lists.
	 */
	private String outputDirectoryName;

	/**
	 * @param clientList
	 *            - the ClientList to serialize.
	 * 
	 * @param consultantList
	 *            - the ConsultantList to serialize.
	 * 
	 * @param outputDirectoryName
	 *            - name of directory to write output to
	 * 
	 */
	public InvoiceServerShutdownHook(List<ClientAccount> clientList, List<Consultant> consultantList,
			String outputDirectoryName) {

		this.clientList = clientList;
		this.consultantList = consultantList;
		this.outputDirectoryName = outputDirectoryName;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		log.info("SHUTDOWN HOOK RUNNING.....");

		String filePathClientList = this.outputDirectoryName + "ClientList" + ".txt";
		File theDirClientList = new File(this.outputDirectoryName);

		if (!theDirClientList.exists()) {
			try {
				theDirClientList.mkdir();
			} catch (SecurityException se) {
			}
		}

		File fileClientList = new File(filePathClientList);
		if (!fileClientList.exists()) {
			try {
				fileClientList.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		PrintStream psClient = null;
		try {
			psClient = new PrintStream(fileClientList);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		String filePathConsultantList = this.outputDirectoryName + "ConsultantList" + ".txt";

		File theDirConsultantList = new File(this.outputDirectoryName);

		if (!theDirConsultantList.exists()) {
			try {
				theDirConsultantList.mkdir();
			} catch (SecurityException se) {
			}
		}

		File fileConsultantList = new File(filePathConsultantList);
		if (!fileConsultantList.exists()) {
			try {
				fileConsultantList.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		PrintStream psConsultant = null;
		try {
			psConsultant = new PrintStream(filePathConsultantList);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		synchronized (clientList) {
			for (final ClientAccount account : clientList) {
				log.info("ADDING....." + account);
				psClient.append(account.toString() + "\n");
			}
			psClient.close();

		}

		synchronized (consultantList) {
			for (final Consultant consultant : consultantList) {
				log.info("ADDING....." + consultant);

				psConsultant.append(consultant.toString() + "\n");

			}
			psConsultant.close();

		}

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}

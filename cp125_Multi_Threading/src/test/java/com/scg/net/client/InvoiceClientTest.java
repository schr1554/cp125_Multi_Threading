package com.scg.net.client;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.scg.domain.TimeCard;

public class InvoiceClientTest {

	@Test
	public void test() {

		List<TimeCard> timeCards = new ArrayList<TimeCard>();

		final InvoiceClient netClient = new InvoiceClient("127.0.0.1", 10888, timeCards);

		assertEquals(netClient, netClient);
	}

}

package org.zaccasoft.bigpoint;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TestImpl {
	
	private static Logger log = Logger.getLogger("my-gasstation");

	@Test
	public void testImpl() {
		assertTrue(true);
		log.debug("yeah, it logs");
		
	}
	
}

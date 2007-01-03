package com.diag.buckaroo.utility;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

public class TestLogger extends TestCase {
	
	static final Level levels[] = {
		Level.ALL,
		Level.SEVERE,
		Level.WARNING,
		Level.CONFIG,
		Level.INFO,
		Level.FINE,
		Level.FINER,
		Level.FINEST,
		Level.OFF
	};
	
	Logger logger;

	protected void setUp() throws Exception {
		super.setUp();
		logger = Logger.getLogger(TestLogger.class.getName());
		assertNotNull(logger);
	}

	protected void tearDown() throws Exception {
		Handler[] handlers = logger.getHandlers();
		for (Handler handler : handlers) {
			if (handler instanceof ConsoleHandler) {
				((ConsoleHandler)handler).flush();
				System.err.flush();
			}
		}
		super.tearDown();
	}

	public void testLogger() {
		for (Level set : levels) {
			assertEquals(Level.parse(set.toString()), set);
			logger.setLevel(set);
			Level get = logger.getLevel();
			assertEquals(set, get);
			for (Level log : levels) {
				logger.log(log, "logger:"
					+ " set=" + set.toString()
					+ " log=" + log.toString()
					+ " enabled=" + logger.isLoggable(log));
			}
		}
	}
}

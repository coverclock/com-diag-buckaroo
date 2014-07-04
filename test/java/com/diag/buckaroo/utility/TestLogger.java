/**
 * Copyright 2007-2013 Digital Aggregates Corporation, Colorado, USA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Name$
 *
 * $Id$
 */
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

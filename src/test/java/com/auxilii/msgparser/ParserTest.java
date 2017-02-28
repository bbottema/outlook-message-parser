/*
 * msgparser - http://auxilii.com/msgparser
 * Copyright (C) 2007  Roman Kurmanowytsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.auxilii.msgparser;

import java.io.File;
import java.io.FilenameFilter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

public class ParserTest extends TestCase {

	public void testParsing() {
		try {
			MsgParser msgp = new MsgParser();

			Handler[] handlers = Logger.getLogger("").getHandlers();
			for (int index = 0; index < handlers.length; index++) {
				handlers[index].setLevel(Level.INFO);
			}
			Logger logger = Logger.getLogger(MsgParser.class.getName());
			logger.setLevel(Level.INFO);
			
			Message msg = null;
			
			File testDir = new File("src/test/resources");
			File[] testFiles = testDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".msg");
				}
			});
			
			for (File testFile : testFiles) {
				msg = msgp.parseMsg(testFile);
				System.out.println("---------------------------------------");
				System.out.println("Parsed message:\n"+msg.toString());
				
			}
		
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
}

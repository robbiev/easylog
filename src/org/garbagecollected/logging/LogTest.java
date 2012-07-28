/*
 * Copyright (C) 2008 Robbie Vanbrabant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.garbagecollected.logging;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.garbagecollected.logging.Log.FormatOption.LEVEL;
import static org.garbagecollected.logging.Log.FormatOption.RUNTIME_PARAMETER;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.garbagecollected.logging.ILog.Level;
import org.garbagecollected.logging.Log.Format;
import org.junit.Test;

public class LogTest {
  @Test
  public void logLevelsAndFormatting() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    ILog log = new Log(new Format(getTestFormat(), LEVEL, RUNTIME_PARAMETER), out);
    log.enable(Level.VERY_MUCH);
    log.publish(Level.INSANE, "Hello, Log!");
    log.publish(Level.MUCH, "Hello, Log!");
    log.publish(Level.VERY_MUCH, "Hello, Log!");
    
    String[] actual = null;
    try {
      String newLine = System.getProperty("line.separator");
      actual = new String(out.toByteArray(),"UTF-8").split(newLine);
    } catch (UnsupportedEncodingException e) {
      fail("UTF-8 should be supported");
    }
    assertEquals(2, actual.length);
    assertEquals("     MUCH | Hello, Log!", actual[0]);
    assertEquals("VERY_MUCH | Hello, Log!", actual[1]);
  }
  
  @Test(expected=java.lang.IllegalArgumentException.class) 
  public void tooLittleArguments() {
    ILog logger = new Log(new Format(getTestFormat(), LEVEL, RUNTIME_PARAMETER));
    logger.enableAllLevels();
    logger.publish(Level.VERY_MUCH,null);
  }

  private static String getTestFormat() {
    int highestLength = 0;
    for(Level l : Level.values())
      if (l.toString().length() > highestLength) 
        highestLength = l.toString().length();
    int longestLevelName = highestLength;
    String format = String.format("%%%ss | %%s", longestLevelName);
    return format;
  }
}

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.AbstractList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** 
 * Simple logging system that shows how simple logging can
 * really be.
 * 
 * @author Robbie Vanbrabant
 */
public class Log implements ILog {
  /** Keeps track of which {@link Level}s are enabled. */
  private final Set<Level> enabledLogLevels = EnumSet.noneOf(Level.class);
  
  /** Specifies the format of a single line. */
  private final Format format;
  
  /** The {@link OutputStream} to write logging to. */
  private final OutputStream os;

  /**
   * Create a new Log that will use the given log format.
   * This controls which calls to 
   * {@link #publish(org.garbagecollected.logging.ILog.Level, Object...)} are valid.
   * Using this constructor the Log will write to System.out.
   * 
   * @param format how each logged line should look
   */
  public Log(Format format) {
    this(format, System.out);
  }
  
  /**
   * Create a new Log that will use the given log format.
   * This controls which calls to 
   * {@link #publish(org.garbagecollected.logging.ILog.Level, Object...)} are valid.
   * The given OutputStream does NOT get closed by this class.
   * 
   * @param format how each logged line should look
   * @param os the {@link OutputStream} to which log information will be written
   */
  public Log(Format format, OutputStream os) {
    precondition(format != null, "Format may not be null");
    precondition(os != null, "OutputStream may not be null");
    this.format = format;
    this.os = os;
  }

  /**
   * @see org.garbagecollected.logging.ILog#publish(org.garbagecollected.logging.Log.Level, java.lang.Object)
   */
  public void publish(Level level, Object msg, Object... msgs) {
    precondition(msg != null, "Message may not be null");
    precondition(msgs != null, "Messages may not be null");
    if (enabledLogLevels.contains(level)) {
      byte[] line;
      List<Object> messages = asList(msg, msgs);
      try {
        line = format.constructLine(level, messages).getBytes("UTF-8");
      } catch (UnsupportedEncodingException e1) {
        throw new RuntimeException(e1);
      }
      try {
        this.os.write(line, 0, line.length);
        this.os.flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  /**
   * @see org.garbagecollected.logging.ILog#enableAllLevels()
   */
  public void enableAllLevels() {
    enabledLogLevels.addAll(EnumSet.allOf(Level.class));
  }
  
  /**
   * @see org.garbagecollected.logging.ILog#enable(org.garbagecollected.logging.Log.Level)
   */
  public void enable(Level level) {
    enabledLogLevels.addAll(level.lowerAndCurrent());
  }

  /**
   * @see org.garbagecollected.logging.ILog#disableAllLevels()
   */
  public void disableAllLevels() {
    enabledLogLevels.clear();
  }
  
  /**
   * @see org.garbagecollected.logging.ILog#disable(org.garbagecollected.logging.Log.Level)
   */
  public void disable(Level level) {
    enabledLogLevels.removeAll(level.lowerAndCurrent());
  }
  
  private static void precondition(boolean condition, String msg) {
    if (!condition) throw new IllegalArgumentException(msg);
  }
  
  private List<Object> asList(final Object msg, final Object... msgs) {
    List<Object> messages = new AbstractList<Object>() {
      @Override public int size() {
        return msgs.length + 1;
      }
      @Override public Object get(int index) {
        return (index == 0) ? msg : msgs[index-1];
      }
    };
    return messages;
  }

  public enum FormatOption {
    /** Log Level. */
    LEVEL, 
    /** Thread ID. */
    THREAD_ID,
    /** Placeholder for a message provided at runtime. */
    RUNTIME_PARAMETER
  }

  public static class Format {
    private final String format;
    private final FormatOption[] lineStructure;
    private final Object[] formatArguments;
    private final Object[] formatArgumentsTemplate;
    
    public Format(String format, FormatOption... lineStructure) {
      precondition(format != null, "format may not be null");
      precondition(lineStructure.length>0, "Need at least one FormatOption");
      this.format = format + "%n"; // platform independent line break
      this.lineStructure = new FormatOption[lineStructure.length];
      // defensive copy
      System.arraycopy(lineStructure, 0, this.lineStructure, 0, lineStructure.length);
      // allocate array for generating a single line
      this.formatArguments = new Object[lineStructure.length];
      // allocate array used to reset the single line array
      this.formatArgumentsTemplate = new Object[lineStructure.length];
    }

    public String constructLine(Level level, List<Object> provided) {      
      for (int i = 0, index = 0; i < lineStructure.length; i++) {
        if (FormatOption.LEVEL == lineStructure[i]) {
          formatArguments[i] = level;
        } else if (FormatOption.THREAD_ID == lineStructure[i]) {
          formatArguments[i] = Thread.currentThread().getId();
        } else if (FormatOption.RUNTIME_PARAMETER == lineStructure[i]) {
          int position = index;
          if (position >= provided.size()) {
            throw new IllegalArgumentException("Expecting more arguments than "+provided.size());
          }
          formatArguments[i] = provided.get(position);
          index++;
        } else {
          throw new IllegalArgumentException("Unknown FormatOption: "+lineStructure[i]);
        }
      }
      try {
        return String.format(format, formatArguments);
      } finally {
        // reset the formatArguments array to have a clean slate for the next line
        System.arraycopy(formatArgumentsTemplate, 0, formatArguments, 0, formatArguments.length);
      }
    }
  }
}

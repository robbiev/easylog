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

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** 
 * Simple logging.
 */
public interface ILog {
  /** Publishes a message to the given log level. */
  void publish(Level level, Object msg, Object... msgs);
  
  /** Enable all log levels. */
  void enableAllLevels();
  /** Disable all log levels. */
  void disableAllLevels();
  /** Enable the given log level. */
  void enable(Level level);
  /** Disable the given log level. */
  void disable(Level level);
  
  public enum Level {
    LITTLE, NORMAL, MUCH, VERY_MUCH, INSANE;
    
    // I'm reluctant to use EnumSet.range(...) because it relies on ordinal()
    // Used for disabling/enabling levels.
    private static final Map<Level, Set<Level>> LOWER_AND_SELF;
    static {
      LOWER_AND_SELF = new EnumMap<Level, Set<Level>>(Level.class);
      LOWER_AND_SELF.put(LITTLE, EnumSet.of(LITTLE));
      LOWER_AND_SELF.put(NORMAL, EnumSet.of(LITTLE, NORMAL));
      LOWER_AND_SELF.put(MUCH, EnumSet.of(LITTLE, NORMAL, MUCH));
      LOWER_AND_SELF.put(VERY_MUCH, EnumSet.of(LITTLE, NORMAL, MUCH, VERY_MUCH));
      LOWER_AND_SELF.put(INSANE, EnumSet.allOf(Level.class));
    }
    
    Set<Level> lowerAndCurrent() {
      return LOWER_AND_SELF.get(this);
    }
  }
}
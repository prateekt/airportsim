//
//  Log.java
//  FAA_Control
//
//  Created by Riley Marsh on 10/30/07.
//  Copyright 2007 Riley Marsh. All rights reserved.
//

package airport;
import airport.objects.*;
import java.util.*;

public class Log {
	Date logTime;
	String logType;
	String logMessage;

	public Log(){	}

	public Log(Date logTime, String logType, String logMessage){
		this.logTime = logTime;
		this.logType = logType;
		this.logMessage = logMessage;
	}

	public Date getLogTime(){ return logTime; }
	public String getLogType(){ return logType; }
	public String getLogMessage(){ return logMessage; }

}

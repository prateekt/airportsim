package agents;
import java.util.HashMap;
import interfaces.Pilot;

/**
 * This is the data structure that is used for
 * command verification. When Pilots echo back a
 * command, the ATC agents store their echos in a
 * queue of Command(s), while the ATCs themselves
 * store their past history of Command(s)
 * @author Henry Yuen
 *
 */
public class Command {
	private Pilot pilot;
	private String command;
	private EchoType echoType;


	public Command(Pilot p,String c, EchoType echoType) {
		pilot = p;
		command = c;
		this.echoType = echoType;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Pilot getPilot() {
		return pilot;
	}

	public void setPilot(Pilot pilot) {
		this.pilot = pilot;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Command)) return false;
		Command c = (Command)o;
		if (pilot != c.getPilot()) return false;
		if (echoType != c.getEchoType()) return false;
		//if (command != c.getCommand()) return false;

		//here, to allow for small typographic errors,
		//we're going to see that the strings are approximately close
		//that is, about 70% of the characters match

		HashMap<Character,Integer> charFreqs = new HashMap<Character,Integer>();

		char original[] = command.toLowerCase().toCharArray();
		char echo[] = c.getCommand().toLowerCase().toCharArray();

		for (int i=0;i<command.length();i++) {
			char ch = original[i];
			int freq = 0;
			if (charFreqs.containsKey(ch)) {
				freq = charFreqs.get(ch);
			}
			charFreqs.put(ch,freq+1);
		}

		for (int i=0;i<c.getCommand().length();i++) {
			char ch = echo[i];
			int freq = 0;
			if (charFreqs.containsKey(ch)) {
				freq = charFreqs.get(ch);
			}
			charFreqs.put(ch,freq-1);
		}

		//count how many differences;
		int diff = 0;
		for (Character ch : charFreqs.keySet()) {
			if (charFreqs.get(ch) != 0) {
				diff += Math.abs(charFreqs.get(ch));
			}
		}

		float ratio = (float)diff/(float)command.length();
		if (ratio > 0.2f) {
			return false;
		}

		return true;
	}

	/**
	 * @return the echoType
	 */
	public EchoType getEchoType() {
		return echoType;
	}

	/**
	 * @param echoType the echoType to set
	 */
	public void setEchoType(EchoType echoType) {
		this.echoType = echoType;
	}

}

package speech;

import java.util.concurrent.Semaphore;

import agent.Agent;
/*
 * The SpeakerEngine interface is the abstraction by which one can implement text-to-speech
 * functionality for this project. The implementation of this component in the current
 * build is based off the FreeTTS Engine (http://freetts.sourceforge.net/docs/index.php) 
 * 
 * Just implement the given abstract methods and behavior of the SpeakerEngine Agent
 * (see FreeTTSSpeakerEngine as an example).
 */
public abstract class SpeakerEngine extends Agent {
	
	/*
	 * Flag for whether voice is on or not.
	 */
	protected boolean voiceOn;
	
	/*
	 * Tell speaker to speak something
	 * 
	 * @param text The message to speak
	 * @param voiceSemaphore The semaphore to synchronize with
	 * @param type The type of voice to use.
	 */
	public abstract void msgPleaseSpeak(String text, Semaphore voiceSemaphore, int type);
	
	/**
	 * @return the voiceOn
	 */
	public boolean isVoiceOn() {
		return voiceOn;
	}

	/**
	 * @param voiceOn the voiceOn to set
	 */
	public void setVoiceOn(boolean voiceOn) {
		this.voiceOn = voiceOn;
	}
	
	public abstract void deallocate();
	public abstract boolean hasMoreTasks();
	public abstract boolean isSpeaking();
}

package interfaces;


/*
 * Abstract speech recognition engine. An engine has a queue to which it puts
 * recognized strings on and a procedure for handing those strings.
 */
public abstract class SpeechRecognitionEngine implements Runnable {

	/*
	 * Flag for whether speech recognition capability is on or not.
	 */
	protected boolean recOn;
			
	/*
	 * Tell user user control agent that something has been said so
	 * that input be processed by GUI.
	 * 
	 * @param text What has been said
	 */
	public abstract void forwardToUserControlledAgent(String text);
	
	/**
	 * @return the voiceOn
	 */
	public boolean isOn() {
		return recOn;
	}
	
	/*
	 * A recognition engine needs to run.
	 */
	public abstract void run();

	/**
	 * @param voiceOn the voiceOn to set
	 */
	public void setRecognitionOn(boolean recOn) {
		this.recOn = recOn;
	}	
}

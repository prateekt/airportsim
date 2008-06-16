package speech;

import com.sun.speech.freetts.VoiceManager;
import java.util.*;
import java.util.concurrent.*;
import com.sun.speech.freetts.Voice;

/*
 * Agent that provides speech capabilities in the system. Every time an agent needs to speak,
 * they update the TraceDB object. The traceDB object messages this agent so the trace
 * can be spoken. The SpeakerAgent is a singleton.
 * 
 * @author Prateek Tandon
 * 
 */
public class FreeTTSSpeakerEngine extends SpeakerEngine {

	/*
	 * VoiceManager object from Free Text to Speech
	 */
	private VoiceManager voiceManager = VoiceManager.getInstance();
	
	
	/*
	 * Each agent has a different voice.
	 */
	private Voice pilotVoice, localControlVoice, clearanceVoice, gcVoice;
	
	/*
	 * Flag for type of voice. I chose not to use an enum because
	 * this basic data type flag needs to be communicated to other places
	 * in the program and an enum would be overhaul. 
	 */
	private static final int TYPE_PILOT = 0;
		
	/*
	 * Flag for type of voice. I chose not to use an enum because
	 * this basic data type flag needs to be communicated to other places
	 * in the program and an enum would be overhaul. 
	 */
	private static final int TYPE_LC = 1;

	/*
	 * Flag for type of voice. I chose not to use an enum because
	 * this basic data type flag needs to be communicated to other places
	 * in the program and an enum would be overhaul. 
	 */
	private static final int TYPE_GC = 2;
		
	/*
	 * Flag for type of voice. I chose not to use an enum because
	 * this basic data type flag needs to be communicated to other places
	 * in the program and an enum would be overhaul. 
	 */
	private static final int TYPE_CD = 3;
	
	
	/*
	 * The speak request is the action object for the SpeakerObject. Whenever the SpeakerAgent
	 * needs to activate speech, a SpeakRequest object is created.
	 */
	class SpeakRequest {
		private String text;
		private Semaphore voiceSemaphore;
		private int type;
		
		public SpeakRequest(String text, Semaphore voiceSemaphore, int type) {
			this.text = text;
			this.voiceSemaphore = voiceSemaphore;
			this.type = type;
		}

		/**
		 * @return the text
		 */
		public String getText() {
			return text;
		}

		/**
		 * @param text the text to set
		 */
		public void setText(String text) {
			this.text = text;
		}

		/**
		 * @return the voiceSemaphore
		 */
		public Semaphore getVoiceSemaphore() {
			return voiceSemaphore;
		}

		/**
		 * @param voiceSemaphore the voiceSemaphore to set
		 */
		public void setVoiceSemaphore(Semaphore voiceSemaphore) {
			this.voiceSemaphore = voiceSemaphore;
		}

		/**
		 * @return the type
		 */
		public int getType() {
			return type;
		}

		/**
		 * @param type the type to set
		 */
		public void setType(int type) {
			this.type = type;
		}
		
	}
		
	/*
	 * Speaker agent's queue of speaker requests.
	 */
	private Queue<SpeakRequest> speakRequests;
	
	/*
	 * Engine active or not
	 */
	private boolean speaking = false;	
	
	/*
	 * Constructor
	 */
	public FreeTTSSpeakerEngine()  {
		speakRequests = new LinkedBlockingQueue<SpeakRequest>();
		pilotVoice = voiceManager.getVoice("kevin16");
		pilotVoice.allocate();
		pilotVoice.setStyle("causual");
		pilotVoice.setVerbose(false);
		localControlVoice = voiceManager.getVoice("kevin");
		localControlVoice.setPitch(250);
		localControlVoice.setRate(150);
		localControlVoice.setVerbose(false);
		localControlVoice.setStyle("business");
		localControlVoice.allocate();	
		clearanceVoice = voiceManager.getVoice("kevin16");
		clearanceVoice.allocate();
		clearanceVoice.setStyle("business");
		clearanceVoice.setVerbose(false);
		gcVoice = voiceManager.getVoice("kevin");
		gcVoice.allocate();
		gcVoice.setStyle("causual");
		gcVoice.setVerbose(false);
		setVoiceOn(false);
	}
		
	/*
	 * Tell speaker agent to speak something
	 * 
	 * @param text The message to speak
	 * @param voiceSemaphore The semaphore to synchronize with
	 * @param type The type of voice to use.
	 */
	public void msgPleaseSpeak(String text, Semaphore voiceSemaphore, int type) {
		speakRequests.add(new SpeakRequest(text, voiceSemaphore, type));
		System.out.println(speakRequests.size());
		stateChanged();
	}
	
	/*
	 * Scheduler
	 */
	public boolean pickAndExecuteAnAction() {
		if(speakRequests.size() > 0) {
			speak(speakRequests.remove());
			return true;
		}
		return false;
	}
	
	/*
	 * Action executes a speak request.
	 * 
	 * @param r The speak request to execute
	 */
	private void speak(SpeakRequest r){
		if(voiceOn) {
			switch(r.getType()) {
				case TYPE_PILOT:
					pilotVoice.speak(r.getText());
					speaking = true;
					break;
				case TYPE_LC:
					localControlVoice.speak(r.getText());
					speaking = true;
					break;
				case TYPE_GC:
					gcVoice.speak(r.getText());
					speaking = true;
					break;
				case TYPE_CD:
					clearanceVoice.speak(r.getText());
					speaking = true;
					break;
			}
		}
		
		r.getVoiceSemaphore().release();
		speaking = false;
	}	
	
	/*
	 * Returns true if SpeakerEngine has more speaking tasks.
	 */
	public boolean hasMoreTasks() {
		synchronized(speakRequests) {
			return speakRequests.size()!=0;
		}
	}
	
	/*
	 * Returns whether engine is active or not.
	 */
	public boolean isSpeaking() {
		return speaking;
	}
	
	public void deallocate() {
		pilotVoice.deallocate();
		localControlVoice.deallocate();
		clearanceVoice.deallocate();
		gcVoice.deallocate();
	}

}

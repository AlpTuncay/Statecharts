package train.traincontroller;
import java.util.LinkedList;
import java.util.List;
import train.ITimer;

public class TrainControllerStatemachine implements ITrainControllerStatemachine {

	protected class SCInterfaceImpl implements SCInterface {
	
		private List<SCInterfaceListener> listeners = new LinkedList<SCInterfaceListener>();
		
		public List<SCInterfaceListener> getListeners() {
			return listeners;
		}
		private SCInterfaceOperationCallback operationCallback;
		
		public void setSCInterfaceOperationCallback(
				SCInterfaceOperationCallback operationCallback) {
			this.operationCallback = operationCallback;
		}
		private boolean close;
		
		public void raiseClose() {
			close = true;
		}
		
		private boolean open;
		
		public void raiseOpen() {
			open = true;
		}
		
		private boolean leave;
		
		public void raiseLeave() {
			leave = true;
		}
		
		private boolean enter;
		
		public void raiseEnter() {
			enter = true;
		}
		
		private boolean pause;
		
		public void raisePause() {
			pause = true;
		}
		
		private boolean continueEvent;
		
		public void raiseContinue() {
			continueEvent = true;
		}
		
		private boolean awake;
		
		public void raiseAwake() {
			awake = true;
		}
		
		private boolean yellow_light;
		
		public void raiseYellow_light() {
			yellow_light = true;
		}
		
		private boolean red_light;
		
		public void raiseRed_light() {
			red_light = true;
		}
		
		private boolean green_light;
		
		public void raiseGreen_light() {
			green_light = true;
		}
		
		private boolean update_acceleration;
		
		private double update_accelerationValue;
		
		public void raiseUpdate_acceleration(double value) {
			update_acceleration = true;
			update_accelerationValue = value;
		}
		
		protected double getUpdate_accelerationValue() {
			if (! update_acceleration ) 
				throw new IllegalStateException("Illegal event value access. Event Update_acceleration is not raised!");
			return update_accelerationValue;
		}
		
		private boolean closeDoors;
		
		public boolean isRaisedCloseDoors() {
			return closeDoors;
		}
		
		protected void raiseCloseDoors() {
			closeDoors = true;
			for (SCInterfaceListener listener : listeners) {
				listener.onCloseDoorsRaised();
			}
		}
		
		private boolean openDoors;
		
		public boolean isRaisedOpenDoors() {
			return openDoors;
		}
		
		protected void raiseOpenDoors() {
			openDoors = true;
			for (SCInterfaceListener listener : listeners) {
				listener.onOpenDoorsRaised();
			}
		}
		
		private boolean error;
		
		private String errorValue;
		
		public boolean isRaisedError() {
			return error;
		}
		
		protected void raiseError(String value) {
			error = true;
			errorValue = value;
			for (SCInterfaceListener listener : listeners) {
				listener.onErrorRaised(value);
			}
		}
		
		public String getErrorValue() {
			if (! error ) 
				throw new IllegalStateException("Illegal event value access. Event Error is not raised!");
			return errorValue;
		}
		
		private boolean warning;
		
		private String warningValue;
		
		public boolean isRaisedWarning() {
			return warning;
		}
		
		protected void raiseWarning(String value) {
			warning = true;
			warningValue = value;
			for (SCInterfaceListener listener : listeners) {
				listener.onWarningRaised(value);
			}
		}
		
		public String getWarningValue() {
			if (! warning ) 
				throw new IllegalStateException("Illegal event value access. Event Warning is not raised!");
			return warningValue;
		}
		
		private boolean clearWarning;
		
		public boolean isRaisedClearWarning() {
			return clearWarning;
		}
		
		protected void raiseClearWarning() {
			clearWarning = true;
			for (SCInterfaceListener listener : listeners) {
				listener.onClearWarningRaised();
			}
		}
		
		private double velocity;
		
		public double getVelocity() {
			return velocity;
		}
		
		public void setVelocity(double value) {
			this.velocity = value;
		}
		
		protected void clearEvents() {
			close = false;
			open = false;
			leave = false;
			enter = false;
			pause = false;
			continueEvent = false;
			awake = false;
			yellow_light = false;
			red_light = false;
			green_light = false;
			update_acceleration = false;
		}
		protected void clearOutEvents() {
		
		closeDoors = false;
		openDoors = false;
		error = false;
		warning = false;
		clearWarning = false;
		}
		
	}
	
	protected SCInterfaceImpl sCInterface;
	
	private boolean initialized = false;
	
	public enum State {
		main_Init,
		main_EmergencyBreak,
		main_SpeedUp,
		$NullState$
	};
	
	private final State[] stateVector = new State[1];
	
	private int nextStateIndex;
	
	private ITimer timer;
	
	private final boolean[] timeEvents = new boolean[2];
	private double acceleration;
	
	protected void setAcceleration(double value) {
		acceleration = value;
	}
	
	protected double getAcceleration() {
		return acceleration;
	}
	
	public TrainControllerStatemachine() {
		sCInterface = new SCInterfaceImpl();
	}
	
	public void init() {
		this.initialized = true;
		if (timer == null) {
			throw new IllegalStateException("timer not set.");
		}
		if (this.sCInterface.operationCallback == null) {
			throw new IllegalStateException("Operation callback for interface sCInterface must be set.");
		}
		
		for (int i = 0; i < 1; i++) {
			stateVector[i] = State.$NullState$;
		}
		clearEvents();
		clearOutEvents();
		sCInterface.setVelocity(0.0);
		
		setAcceleration(0.0);
	}
	
	public void enter() {
		if (!initialized) {
			throw new IllegalStateException(
					"The state machine needs to be initialized first by calling the init() function.");
		}
		if (timer == null) {
			throw new IllegalStateException("timer not set.");
		}
		entryAction();
		enterSequence_main_default();
	}
	
	public void exit() {
		exitSequence_main();
		exitAction();
	}
	
	/**
	 * @see IStatemachine#isActive()
	 */
	public boolean isActive() {
		return stateVector[0] != State.$NullState$;
	}
	
	/** 
	* Always returns 'false' since this state machine can never become final.
	*
	* @see IStatemachine#isFinal()
	*/
	public boolean isFinal() {
		return false;
	}
	/**
	* This method resets the incoming events (time events included).
	*/
	protected void clearEvents() {
		sCInterface.clearEvents();
		for (int i=0; i<timeEvents.length; i++) {
			timeEvents[i] = false;
		}
	}
	
	/**
	* This method resets the outgoing events.
	*/
	protected void clearOutEvents() {
		sCInterface.clearOutEvents();
	}
	
	/**
	* Returns true if the given state is currently active otherwise false.
	*/
	public boolean isStateActive(State state) {
	
		switch (state) {
		case main_Init:
			return stateVector[0] == State.main_Init;
		case main_EmergencyBreak:
			return stateVector[0] == State.main_EmergencyBreak;
		case main_SpeedUp:
			return stateVector[0] == State.main_SpeedUp;
		default:
			return false;
		}
	}
	
	/**
	* Set the {@link ITimer} for the state machine. It must be set
	* externally on a timed state machine before a run cycle can be correct
	* executed.
	* 
	* @param timer
	*/
	public void setTimer(ITimer timer) {
		this.timer = timer;
	}
	
	/**
	* Returns the currently used timer.
	* 
	* @return {@link ITimer}
	*/
	public ITimer getTimer() {
		return timer;
	}
	
	public void timeElapsed(int eventID) {
		timeEvents[eventID] = true;
	}
	
	public SCInterface getSCInterface() {
		return sCInterface;
	}
	
	public void raiseClose() {
		sCInterface.raiseClose();
	}
	
	public void raiseOpen() {
		sCInterface.raiseOpen();
	}
	
	public void raiseLeave() {
		sCInterface.raiseLeave();
	}
	
	public void raiseEnter() {
		sCInterface.raiseEnter();
	}
	
	public void raisePause() {
		sCInterface.raisePause();
	}
	
	public void raiseContinue() {
		sCInterface.raiseContinue();
	}
	
	public void raiseAwake() {
		sCInterface.raiseAwake();
	}
	
	public void raiseYellow_light() {
		sCInterface.raiseYellow_light();
	}
	
	public void raiseRed_light() {
		sCInterface.raiseRed_light();
	}
	
	public void raiseGreen_light() {
		sCInterface.raiseGreen_light();
	}
	
	public void raiseUpdate_acceleration(double value) {
		sCInterface.raiseUpdate_acceleration(value);
	}
	
	public boolean isRaisedCloseDoors() {
		return sCInterface.isRaisedCloseDoors();
	}
	
	public boolean isRaisedOpenDoors() {
		return sCInterface.isRaisedOpenDoors();
	}
	
	public boolean isRaisedError() {
		return sCInterface.isRaisedError();
	}
	
	public String getErrorValue() {
		return sCInterface.getErrorValue();
	}
	
	public boolean isRaisedWarning() {
		return sCInterface.isRaisedWarning();
	}
	
	public String getWarningValue() {
		return sCInterface.getWarningValue();
	}
	
	public boolean isRaisedClearWarning() {
		return sCInterface.isRaisedClearWarning();
	}
	
	public double getVelocity() {
		return sCInterface.getVelocity();
	}
	
	public void setVelocity(double value) {
		sCInterface.setVelocity(value);
	}
	
	/* Entry action for statechart 'TrainController'. */
	private void entryAction() {
		timer.setTimer(this, 1, 20, true);
	}
	
	/* Entry action for state 'Init'. */
	private void entryAction_main_Init() {
		sCInterface.raiseWarning("Init");
	}
	
	/* Entry action for state 'EmergencyBreak'. */
	private void entryAction_main_EmergencyBreak() {
		timer.setTimer(this, 0, 5 * 1000, false);
		
		setAcceleration(-1);
		
		sCInterface.raiseWarning("Emergency Break");
	}
	
	/* Entry action for state 'SpeedUp'. */
	private void entryAction_main_SpeedUp() {
		setAcceleration(sCInterface.getUpdate_accelerationValue());
		
		sCInterface.setVelocity(sCInterface.velocity>100 ? 100 : sCInterface.velocity);
		
		sCInterface.raiseWarning("Speeding up");
	}
	
	/* Exit action for state 'TrainController'. */
	private void exitAction() {
		timer.unsetTimer(this, 1);
	}
	
	/* Exit action for state 'EmergencyBreak'. */
	private void exitAction_main_EmergencyBreak() {
		timer.unsetTimer(this, 0);
	}
	
	/* 'default' enter sequence for state Init */
	private void enterSequence_main_Init_default() {
		entryAction_main_Init();
		nextStateIndex = 0;
		stateVector[0] = State.main_Init;
	}
	
	/* 'default' enter sequence for state EmergencyBreak */
	private void enterSequence_main_EmergencyBreak_default() {
		entryAction_main_EmergencyBreak();
		nextStateIndex = 0;
		stateVector[0] = State.main_EmergencyBreak;
	}
	
	/* 'default' enter sequence for state SpeedUp */
	private void enterSequence_main_SpeedUp_default() {
		entryAction_main_SpeedUp();
		nextStateIndex = 0;
		stateVector[0] = State.main_SpeedUp;
	}
	
	/* 'default' enter sequence for region main */
	private void enterSequence_main_default() {
		react_main__entry_Default();
	}
	
	/* Default exit sequence for state Init */
	private void exitSequence_main_Init() {
		nextStateIndex = 0;
		stateVector[0] = State.$NullState$;
	}
	
	/* Default exit sequence for state EmergencyBreak */
	private void exitSequence_main_EmergencyBreak() {
		nextStateIndex = 0;
		stateVector[0] = State.$NullState$;
		
		exitAction_main_EmergencyBreak();
	}
	
	/* Default exit sequence for state SpeedUp */
	private void exitSequence_main_SpeedUp() {
		nextStateIndex = 0;
		stateVector[0] = State.$NullState$;
	}
	
	/* Default exit sequence for region main */
	private void exitSequence_main() {
		switch (stateVector[0]) {
		case main_Init:
			exitSequence_main_Init();
			break;
		case main_EmergencyBreak:
			exitSequence_main_EmergencyBreak();
			break;
		case main_SpeedUp:
			exitSequence_main_SpeedUp();
			break;
		default:
			break;
		}
	}
	
	/* Default react sequence for initial entry  */
	private void react_main__entry_Default() {
		enterSequence_main_Init_default();
	}
	
	private boolean react(boolean try_transition) {
		if (timeEvents[1]) {
			sCInterface.setVelocity(sCInterface.getVelocity() + (acceleration / 2));
			
			sCInterface.setVelocity(sCInterface.velocity>0 ? sCInterface.velocity : 0);
			
			sCInterface.operationCallback.updateGUI();
		}
		return false;
	}
	
	private boolean main_Init_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (react(try_transition)==false) {
				if (sCInterface.update_acceleration) {
					exitSequence_main_Init();
					enterSequence_main_SpeedUp_default();
				} else {
					did_transition = false;
				}
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	private boolean main_EmergencyBreak_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (react(try_transition)==false) {
				if (timeEvents[0]) {
					exitSequence_main_EmergencyBreak();
					enterSequence_main_Init_default();
				} else {
					did_transition = false;
				}
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	private boolean main_SpeedUp_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (react(try_transition)==false) {
				if (sCInterface.red_light) {
					exitSequence_main_SpeedUp();
					enterSequence_main_EmergencyBreak_default();
				} else {
					if (sCInterface.update_acceleration) {
						exitSequence_main_SpeedUp();
						enterSequence_main_SpeedUp_default();
					} else {
						did_transition = false;
					}
				}
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	public void runCycle() {
		if (!initialized)
			throw new IllegalStateException(
					"The state machine needs to be initialized first by calling the init() function.");
		clearOutEvents();
		for (nextStateIndex = 0; nextStateIndex < stateVector.length; nextStateIndex++) {
			switch (stateVector[nextStateIndex]) {
			case main_Init:
				main_Init_react(true);
				break;
			case main_EmergencyBreak:
				main_EmergencyBreak_react(true);
				break;
			case main_SpeedUp:
				main_SpeedUp_react(true);
				break;
			default:
				// $NullState$
			}
		}
		clearEvents();
	}
}

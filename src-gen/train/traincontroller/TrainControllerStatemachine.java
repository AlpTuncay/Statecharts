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
		main_Movement,
		main_Movement_Move_Init,
		main_Movement_Move_SpeedUp,
		main_Movement_Move_Red,
		main_Movement_Move_MaxSpeed,
		main_Movement_Move_Yellow,
		main_Movement_Dead_Man_s_Button_CheckVelocity,
		main_Movement_Dead_Man_s_Button_Poll,
		main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt,
		main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed,
		main_Emergency,
		main_Emergency_Break_Cooldown,
		main_Emergency_Break_EmergencyBreak,
		main_Station,
		main_Station_Enter_Enter,
		$NullState$
	};
	
	private final State[] stateVector = new State[2];
	
	private int nextStateIndex;
	
	private ITimer timer;
	
	private final boolean[] timeEvents = new boolean[4];
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
		
		for (int i = 0; i < 2; i++) {
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
		return stateVector[0] != State.$NullState$||stateVector[1] != State.$NullState$;
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
		case main_Movement:
			return stateVector[0].ordinal() >= State.
					main_Movement.ordinal()&& stateVector[0].ordinal() <= State.main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed.ordinal();
		case main_Movement_Move_Init:
			return stateVector[0] == State.main_Movement_Move_Init;
		case main_Movement_Move_SpeedUp:
			return stateVector[0] == State.main_Movement_Move_SpeedUp;
		case main_Movement_Move_Red:
			return stateVector[0] == State.main_Movement_Move_Red;
		case main_Movement_Move_MaxSpeed:
			return stateVector[0] == State.main_Movement_Move_MaxSpeed;
		case main_Movement_Move_Yellow:
			return stateVector[0] == State.main_Movement_Move_Yellow;
		case main_Movement_Dead_Man_s_Button_CheckVelocity:
			return stateVector[1] == State.main_Movement_Dead_Man_s_Button_CheckVelocity;
		case main_Movement_Dead_Man_s_Button_Poll:
			return stateVector[1].ordinal() >= State.
					main_Movement_Dead_Man_s_Button_Poll.ordinal()&& stateVector[1].ordinal() <= State.main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed.ordinal();
		case main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt:
			return stateVector[1] == State.main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt;
		case main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed:
			return stateVector[1] == State.main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed;
		case main_Emergency:
			return stateVector[0].ordinal() >= State.
					main_Emergency.ordinal()&& stateVector[0].ordinal() <= State.main_Emergency_Break_EmergencyBreak.ordinal();
		case main_Emergency_Break_Cooldown:
			return stateVector[0] == State.main_Emergency_Break_Cooldown;
		case main_Emergency_Break_EmergencyBreak:
			return stateVector[0] == State.main_Emergency_Break_EmergencyBreak;
		case main_Station:
			return stateVector[0].ordinal() >= State.
					main_Station.ordinal()&& stateVector[0].ordinal() <= State.main_Station_Enter_Enter.ordinal();
		case main_Station_Enter_Enter:
			return stateVector[0] == State.main_Station_Enter_Enter;
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
		timer.setTimer(this, 3, 20, true);
	}
	
	/* Entry action for state 'SpeedUp'. */
	private void entryAction_main_Movement_Move_SpeedUp() {
		setAcceleration(sCInterface.getUpdate_accelerationValue());
	}
	
	/* Entry action for state 'Red'. */
	private void entryAction_main_Movement_Move_Red() {
		sCInterface.raiseWarning("Passing Red");
	}
	
	/* Entry action for state 'MaxSpeed'. */
	private void entryAction_main_Movement_Move_MaxSpeed() {
		sCInterface.setVelocity(sCInterface.velocity>100 ? 100 : sCInterface.velocity);
		
		setAcceleration(0);
		
		sCInterface.raiseWarning("Max speed reached");
	}
	
	/* Entry action for state 'Yellow'. */
	private void entryAction_main_Movement_Move_Yellow() {
		sCInterface.raiseWarning("Passing yellow.");
	}
	
	/* Entry action for state 'Poll'. */
	private void entryAction_main_Movement_Dead_Man_s_Button_Poll() {
		timer.setTimer(this, 0, 30 * 1000, false);
	}
	
	/* Entry action for state 'Prompt'. */
	private void entryAction_main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt() {
		timer.setTimer(this, 1, 5 * 1000, false);
		
		sCInterface.raiseWarning("Poll");
	}
	
	/* Entry action for state 'Pressed'. */
	private void entryAction_main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed() {
		sCInterface.raiseClearWarning();
	}
	
	/* Entry action for state 'Cooldown'. */
	private void entryAction_main_Emergency_Break_Cooldown() {
		timer.setTimer(this, 2, 5 * 1000, false);
		
		sCInterface.raiseWarning("Cooldown");
	}
	
	/* Entry action for state 'EmergencyBreak'. */
	private void entryAction_main_Emergency_Break_EmergencyBreak() {
		setAcceleration(-1);
		
		sCInterface.raiseError("Emergency Break");
	}
	
	/* Entry action for state 'Enter'. */
	private void entryAction_main_Station_Enter_Enter() {
		sCInterface.raiseWarning("Near station");
	}
	
	/* Exit action for state 'TrainController'. */
	private void exitAction() {
		timer.unsetTimer(this, 3);
	}
	
	/* Exit action for state 'Poll'. */
	private void exitAction_main_Movement_Dead_Man_s_Button_Poll() {
		timer.unsetTimer(this, 0);
	}
	
	/* Exit action for state 'Prompt'. */
	private void exitAction_main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt() {
		timer.unsetTimer(this, 1);
	}
	
	/* Exit action for state 'Emergency'. */
	private void exitAction_main_Emergency() {
		sCInterface.raiseClearWarning();
	}
	
	/* Exit action for state 'Cooldown'. */
	private void exitAction_main_Emergency_Break_Cooldown() {
		timer.unsetTimer(this, 2);
	}
	
	/* 'default' enter sequence for state Movement */
	private void enterSequence_main_Movement_default() {
		enterSequence_main_Movement_Move_default();
		enterSequence_main_Movement_Dead_Man_s_Button_default();
	}
	
	/* 'default' enter sequence for state Init */
	private void enterSequence_main_Movement_Move_Init_default() {
		nextStateIndex = 0;
		stateVector[0] = State.main_Movement_Move_Init;
	}
	
	/* 'default' enter sequence for state SpeedUp */
	private void enterSequence_main_Movement_Move_SpeedUp_default() {
		entryAction_main_Movement_Move_SpeedUp();
		nextStateIndex = 0;
		stateVector[0] = State.main_Movement_Move_SpeedUp;
	}
	
	/* 'default' enter sequence for state Red */
	private void enterSequence_main_Movement_Move_Red_default() {
		entryAction_main_Movement_Move_Red();
		nextStateIndex = 0;
		stateVector[0] = State.main_Movement_Move_Red;
	}
	
	/* 'default' enter sequence for state MaxSpeed */
	private void enterSequence_main_Movement_Move_MaxSpeed_default() {
		entryAction_main_Movement_Move_MaxSpeed();
		nextStateIndex = 0;
		stateVector[0] = State.main_Movement_Move_MaxSpeed;
	}
	
	/* 'default' enter sequence for state Yellow */
	private void enterSequence_main_Movement_Move_Yellow_default() {
		entryAction_main_Movement_Move_Yellow();
		nextStateIndex = 0;
		stateVector[0] = State.main_Movement_Move_Yellow;
	}
	
	/* 'default' enter sequence for state CheckVelocity */
	private void enterSequence_main_Movement_Dead_Man_s_Button_CheckVelocity_default() {
		nextStateIndex = 1;
		stateVector[1] = State.main_Movement_Dead_Man_s_Button_CheckVelocity;
	}
	
	/* 'default' enter sequence for state Poll */
	private void enterSequence_main_Movement_Dead_Man_s_Button_Poll_default() {
		entryAction_main_Movement_Dead_Man_s_Button_Poll();
		enterSequence_main_Movement_Dead_Man_s_Button_Poll_Poll_default();
	}
	
	/* 'default' enter sequence for state Prompt */
	private void enterSequence_main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt_default() {
		entryAction_main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt();
		nextStateIndex = 1;
		stateVector[1] = State.main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt;
	}
	
	/* 'default' enter sequence for state Pressed */
	private void enterSequence_main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed_default() {
		entryAction_main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed();
		nextStateIndex = 1;
		stateVector[1] = State.main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed;
	}
	
	/* 'default' enter sequence for state Emergency */
	private void enterSequence_main_Emergency_default() {
		enterSequence_main_Emergency_Break_default();
	}
	
	/* 'default' enter sequence for state Cooldown */
	private void enterSequence_main_Emergency_Break_Cooldown_default() {
		entryAction_main_Emergency_Break_Cooldown();
		nextStateIndex = 0;
		stateVector[0] = State.main_Emergency_Break_Cooldown;
	}
	
	/* 'default' enter sequence for state EmergencyBreak */
	private void enterSequence_main_Emergency_Break_EmergencyBreak_default() {
		entryAction_main_Emergency_Break_EmergencyBreak();
		nextStateIndex = 0;
		stateVector[0] = State.main_Emergency_Break_EmergencyBreak;
	}
	
	/* 'default' enter sequence for state Station */
	private void enterSequence_main_Station_default() {
		enterSequence_main_Station_Enter_default();
	}
	
	/* 'default' enter sequence for state Enter */
	private void enterSequence_main_Station_Enter_Enter_default() {
		entryAction_main_Station_Enter_Enter();
		nextStateIndex = 0;
		stateVector[0] = State.main_Station_Enter_Enter;
	}
	
	/* 'default' enter sequence for region main */
	private void enterSequence_main_default() {
		react_main__entry_Default();
	}
	
	/* 'default' enter sequence for region Move */
	private void enterSequence_main_Movement_Move_default() {
		react_main_Movement_Move__entry_Default();
	}
	
	/* 'default' enter sequence for region Dead Man's Button */
	private void enterSequence_main_Movement_Dead_Man_s_Button_default() {
		react_main_Movement_Dead_Man_s_Button__entry_Default();
	}
	
	/* 'default' enter sequence for region Poll */
	private void enterSequence_main_Movement_Dead_Man_s_Button_Poll_Poll_default() {
		react_main_Movement_Dead_Man_s_Button_Poll_Poll__entry_Default();
	}
	
	/* 'default' enter sequence for region Break */
	private void enterSequence_main_Emergency_Break_default() {
		react_main_Emergency_Break__entry_Default();
	}
	
	/* 'default' enter sequence for region Enter */
	private void enterSequence_main_Station_Enter_default() {
		react_main_Station_Enter__entry_Default();
	}
	
	/* Default exit sequence for state Movement */
	private void exitSequence_main_Movement() {
		exitSequence_main_Movement_Move();
		exitSequence_main_Movement_Dead_Man_s_Button();
	}
	
	/* Default exit sequence for state Init */
	private void exitSequence_main_Movement_Move_Init() {
		nextStateIndex = 0;
		stateVector[0] = State.$NullState$;
	}
	
	/* Default exit sequence for state SpeedUp */
	private void exitSequence_main_Movement_Move_SpeedUp() {
		nextStateIndex = 0;
		stateVector[0] = State.$NullState$;
	}
	
	/* Default exit sequence for state Red */
	private void exitSequence_main_Movement_Move_Red() {
		nextStateIndex = 0;
		stateVector[0] = State.$NullState$;
	}
	
	/* Default exit sequence for state MaxSpeed */
	private void exitSequence_main_Movement_Move_MaxSpeed() {
		nextStateIndex = 0;
		stateVector[0] = State.$NullState$;
	}
	
	/* Default exit sequence for state Yellow */
	private void exitSequence_main_Movement_Move_Yellow() {
		nextStateIndex = 0;
		stateVector[0] = State.$NullState$;
	}
	
	/* Default exit sequence for state CheckVelocity */
	private void exitSequence_main_Movement_Dead_Man_s_Button_CheckVelocity() {
		nextStateIndex = 1;
		stateVector[1] = State.$NullState$;
	}
	
	/* Default exit sequence for state Poll */
	private void exitSequence_main_Movement_Dead_Man_s_Button_Poll() {
		exitSequence_main_Movement_Dead_Man_s_Button_Poll_Poll();
		exitAction_main_Movement_Dead_Man_s_Button_Poll();
	}
	
	/* Default exit sequence for state Prompt */
	private void exitSequence_main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt() {
		nextStateIndex = 1;
		stateVector[1] = State.$NullState$;
		
		exitAction_main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt();
	}
	
	/* Default exit sequence for state Pressed */
	private void exitSequence_main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed() {
		nextStateIndex = 1;
		stateVector[1] = State.$NullState$;
	}
	
	/* Default exit sequence for state Emergency */
	private void exitSequence_main_Emergency() {
		exitSequence_main_Emergency_Break();
		exitAction_main_Emergency();
	}
	
	/* Default exit sequence for state Cooldown */
	private void exitSequence_main_Emergency_Break_Cooldown() {
		nextStateIndex = 0;
		stateVector[0] = State.$NullState$;
		
		exitAction_main_Emergency_Break_Cooldown();
	}
	
	/* Default exit sequence for state EmergencyBreak */
	private void exitSequence_main_Emergency_Break_EmergencyBreak() {
		nextStateIndex = 0;
		stateVector[0] = State.$NullState$;
	}
	
	/* Default exit sequence for state Station */
	private void exitSequence_main_Station() {
		exitSequence_main_Station_Enter();
	}
	
	/* Default exit sequence for state Enter */
	private void exitSequence_main_Station_Enter_Enter() {
		nextStateIndex = 0;
		stateVector[0] = State.$NullState$;
	}
	
	/* Default exit sequence for region main */
	private void exitSequence_main() {
		switch (stateVector[0]) {
		case main_Movement_Move_Init:
			exitSequence_main_Movement_Move_Init();
			break;
		case main_Movement_Move_SpeedUp:
			exitSequence_main_Movement_Move_SpeedUp();
			break;
		case main_Movement_Move_Red:
			exitSequence_main_Movement_Move_Red();
			break;
		case main_Movement_Move_MaxSpeed:
			exitSequence_main_Movement_Move_MaxSpeed();
			break;
		case main_Movement_Move_Yellow:
			exitSequence_main_Movement_Move_Yellow();
			break;
		case main_Emergency_Break_Cooldown:
			exitSequence_main_Emergency_Break_Cooldown();
			exitAction_main_Emergency();
			break;
		case main_Emergency_Break_EmergencyBreak:
			exitSequence_main_Emergency_Break_EmergencyBreak();
			exitAction_main_Emergency();
			break;
		case main_Station_Enter_Enter:
			exitSequence_main_Station_Enter_Enter();
			break;
		default:
			break;
		}
		
		switch (stateVector[1]) {
		case main_Movement_Dead_Man_s_Button_CheckVelocity:
			exitSequence_main_Movement_Dead_Man_s_Button_CheckVelocity();
			break;
		case main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt:
			exitSequence_main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt();
			exitAction_main_Movement_Dead_Man_s_Button_Poll();
			break;
		case main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed:
			exitSequence_main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed();
			exitAction_main_Movement_Dead_Man_s_Button_Poll();
			break;
		default:
			break;
		}
	}
	
	/* Default exit sequence for region Move */
	private void exitSequence_main_Movement_Move() {
		switch (stateVector[0]) {
		case main_Movement_Move_Init:
			exitSequence_main_Movement_Move_Init();
			break;
		case main_Movement_Move_SpeedUp:
			exitSequence_main_Movement_Move_SpeedUp();
			break;
		case main_Movement_Move_Red:
			exitSequence_main_Movement_Move_Red();
			break;
		case main_Movement_Move_MaxSpeed:
			exitSequence_main_Movement_Move_MaxSpeed();
			break;
		case main_Movement_Move_Yellow:
			exitSequence_main_Movement_Move_Yellow();
			break;
		default:
			break;
		}
	}
	
	/* Default exit sequence for region Dead Man's Button */
	private void exitSequence_main_Movement_Dead_Man_s_Button() {
		switch (stateVector[1]) {
		case main_Movement_Dead_Man_s_Button_CheckVelocity:
			exitSequence_main_Movement_Dead_Man_s_Button_CheckVelocity();
			break;
		case main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt:
			exitSequence_main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt();
			exitAction_main_Movement_Dead_Man_s_Button_Poll();
			break;
		case main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed:
			exitSequence_main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed();
			exitAction_main_Movement_Dead_Man_s_Button_Poll();
			break;
		default:
			break;
		}
	}
	
	/* Default exit sequence for region Poll */
	private void exitSequence_main_Movement_Dead_Man_s_Button_Poll_Poll() {
		switch (stateVector[1]) {
		case main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt:
			exitSequence_main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt();
			break;
		case main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed:
			exitSequence_main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed();
			break;
		default:
			break;
		}
	}
	
	/* Default exit sequence for region Break */
	private void exitSequence_main_Emergency_Break() {
		switch (stateVector[0]) {
		case main_Emergency_Break_Cooldown:
			exitSequence_main_Emergency_Break_Cooldown();
			break;
		case main_Emergency_Break_EmergencyBreak:
			exitSequence_main_Emergency_Break_EmergencyBreak();
			break;
		default:
			break;
		}
	}
	
	/* Default exit sequence for region Enter */
	private void exitSequence_main_Station_Enter() {
		switch (stateVector[0]) {
		case main_Station_Enter_Enter:
			exitSequence_main_Station_Enter_Enter();
			break;
		default:
			break;
		}
	}
	
	/* Default react sequence for initial entry  */
	private void react_main_Movement_Move__entry_Default() {
		enterSequence_main_Movement_Move_Init_default();
	}
	
	/* Default react sequence for initial entry  */
	private void react_main_Movement_Dead_Man_s_Button__entry_Default() {
		enterSequence_main_Movement_Dead_Man_s_Button_CheckVelocity_default();
	}
	
	/* Default react sequence for initial entry  */
	private void react_main_Movement_Dead_Man_s_Button_Poll_Poll__entry_Default() {
		enterSequence_main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt_default();
	}
	
	/* Default react sequence for initial entry  */
	private void react_main__entry_Default() {
		enterSequence_main_Movement_default();
	}
	
	/* Default react sequence for initial entry  */
	private void react_main_Emergency_Break__entry_Default() {
		enterSequence_main_Emergency_Break_EmergencyBreak_default();
	}
	
	/* Default react sequence for initial entry  */
	private void react_main_Station_Enter__entry_Default() {
		enterSequence_main_Station_Enter_Enter_default();
	}
	
	private boolean react(boolean try_transition) {
		if (timeEvents[3]) {
			sCInterface.setVelocity(sCInterface.getVelocity() + (acceleration / 2));
			
			sCInterface.setVelocity(sCInterface.velocity>0 ? sCInterface.velocity : 0);
			
			sCInterface.operationCallback.updateGUI();
		}
		return false;
	}
	
	private boolean main_Movement_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (react(try_transition)==false) {
				if (sCInterface.enter) {
					exitSequence_main_Movement();
					enterSequence_main_Station_default();
				} else {
					did_transition = false;
				}
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	private boolean main_Movement_Move_Init_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (main_Movement_react(try_transition)==false) {
				if (sCInterface.update_acceleration) {
					exitSequence_main_Movement_Move_Init();
					enterSequence_main_Movement_Move_SpeedUp_default();
				} else {
					did_transition = false;
				}
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	private boolean main_Movement_Move_SpeedUp_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (main_Movement_react(try_transition)==false) {
				if (sCInterface.red_light) {
					exitSequence_main_Movement_Move_SpeedUp();
					enterSequence_main_Movement_Move_Red_default();
				} else {
					if (sCInterface.update_acceleration) {
						exitSequence_main_Movement_Move_SpeedUp();
						enterSequence_main_Movement_Move_SpeedUp_default();
					} else {
						if (sCInterface.getVelocity()>=100) {
							exitSequence_main_Movement_Move_SpeedUp();
							enterSequence_main_Movement_Move_MaxSpeed_default();
						} else {
							if (sCInterface.yellow_light) {
								exitSequence_main_Movement_Move_SpeedUp();
								enterSequence_main_Movement_Move_Yellow_default();
							} else {
								did_transition = false;
							}
						}
					}
				}
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	private boolean main_Movement_Move_Red_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (main_Movement_react(try_transition)==false) {
				if (sCInterface.getVelocity()>0) {
					exitSequence_main_Movement();
					enterSequence_main_Emergency_default();
				} else {
					did_transition = false;
				}
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	private boolean main_Movement_Move_MaxSpeed_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (main_Movement_react(try_transition)==false) {
				if (sCInterface.update_acceleration) {
					exitSequence_main_Movement_Move_MaxSpeed();
					enterSequence_main_Movement_Move_SpeedUp_default();
				} else {
					if (sCInterface.red_light) {
						exitSequence_main_Movement_Move_MaxSpeed();
						enterSequence_main_Movement_Move_Red_default();
					} else {
						if (sCInterface.yellow_light) {
							exitSequence_main_Movement_Move_MaxSpeed();
							enterSequence_main_Movement_Move_Yellow_default();
						} else {
							did_transition = false;
						}
					}
				}
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	private boolean main_Movement_Move_Yellow_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (main_Movement_react(try_transition)==false) {
				if (sCInterface.getVelocity()>50) {
					exitSequence_main_Movement();
					enterSequence_main_Emergency_default();
				} else {
					did_transition = false;
				}
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	private boolean main_Movement_Dead_Man_s_Button_CheckVelocity_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (sCInterface.getVelocity()>0) {
				exitSequence_main_Movement_Dead_Man_s_Button_CheckVelocity();
				enterSequence_main_Movement_Dead_Man_s_Button_Poll_default();
			} else {
				did_transition = false;
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	private boolean main_Movement_Dead_Man_s_Button_Poll_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (timeEvents[0]) {
				exitSequence_main_Movement_Dead_Man_s_Button_Poll();
				enterSequence_main_Movement_Dead_Man_s_Button_Poll_default();
			} else {
				did_transition = false;
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	private boolean main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (main_Movement_Dead_Man_s_Button_Poll_react(try_transition)==false) {
				if (sCInterface.awake) {
					exitSequence_main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt();
					enterSequence_main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed_default();
				} else {
					if (timeEvents[1]) {
						exitSequence_main_Movement();
						enterSequence_main_Emergency_default();
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
	
	private boolean main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (main_Movement_Dead_Man_s_Button_Poll_react(try_transition)==false) {
				did_transition = false;
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	private boolean main_Emergency_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (react(try_transition)==false) {
				did_transition = false;
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	private boolean main_Emergency_Break_Cooldown_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (main_Emergency_react(try_transition)==false) {
				if (timeEvents[2]) {
					exitSequence_main_Emergency();
					enterSequence_main_Movement_default();
				} else {
					did_transition = false;
				}
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	private boolean main_Emergency_Break_EmergencyBreak_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (main_Emergency_react(try_transition)==false) {
				if (sCInterface.getVelocity()==0) {
					exitSequence_main_Emergency_Break_EmergencyBreak();
					enterSequence_main_Emergency_Break_Cooldown_default();
				} else {
					did_transition = false;
				}
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	private boolean main_Station_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (react(try_transition)==false) {
				if (sCInterface.leave) {
					exitSequence_main_Station();
					enterSequence_main_Movement_default();
				} else {
					did_transition = false;
				}
			}
		}
		if (did_transition==false) {
		}
		return did_transition;
	}
	
	private boolean main_Station_Enter_Enter_react(boolean try_transition) {
		boolean did_transition = try_transition;
		
		if (try_transition) {
			if (main_Station_react(try_transition)==false) {
				if (sCInterface.getVelocity()>20) {
					exitSequence_main_Station();
					enterSequence_main_Emergency_default();
				} else {
					did_transition = false;
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
			case main_Movement_Move_Init:
				main_Movement_Move_Init_react(true);
				break;
			case main_Movement_Move_SpeedUp:
				main_Movement_Move_SpeedUp_react(true);
				break;
			case main_Movement_Move_Red:
				main_Movement_Move_Red_react(true);
				break;
			case main_Movement_Move_MaxSpeed:
				main_Movement_Move_MaxSpeed_react(true);
				break;
			case main_Movement_Move_Yellow:
				main_Movement_Move_Yellow_react(true);
				break;
			case main_Movement_Dead_Man_s_Button_CheckVelocity:
				main_Movement_Dead_Man_s_Button_CheckVelocity_react(true);
				break;
			case main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt:
				main_Movement_Dead_Man_s_Button_Poll_Poll_Prompt_react(true);
				break;
			case main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed:
				main_Movement_Dead_Man_s_Button_Poll_Poll_Pressed_react(true);
				break;
			case main_Emergency_Break_Cooldown:
				main_Emergency_Break_Cooldown_react(true);
				break;
			case main_Emergency_Break_EmergencyBreak:
				main_Emergency_Break_EmergencyBreak_react(true);
				break;
			case main_Station_Enter_Enter:
				main_Station_Enter_Enter_react(true);
				break;
			default:
				// $NullState$
			}
		}
		clearEvents();
	}
}

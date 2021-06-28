package projects.invite.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import lombok.Getter;
import lombok.Setter;
import projects.invite.enums.State;
import projects.invite.nodes.messages.AYCAnswer;
import projects.invite.nodes.messages.AYCoord;
import projects.invite.nodes.messages.AYThere;
import projects.invite.nodes.messages.AYThere_answer;
import projects.invite.nodes.messages.Accept;
import projects.invite.nodes.messages.AcceptAnswer;
import projects.invite.nodes.messages.Invitation;
import projects.invite.nodes.messages.Ready;
import projects.invite.nodes.messages.ReadyAnswer;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

@Getter
@Setter
public class InviteNode extends Node {

	
	// conjunto dos membros do proprio grupo
	private ArrayList<Node> upSet;
	private ArrayList<Node> up;
	private ArrayList<Node> others;
	
	
	// identificao do grupo (par [CoordID,count])
	private Node CoordinatorGroup;
	private Node oldCoordinatorGroup;
	private int CoordinatorCount = 0;
	private int groupId = 0;
	
	
	private Logging logging = Logging.getLogger();

	// const
	private int coinChancePositive = 100;
	private double timeOut = 50;
	private double waitConst = 50;
	// Momento da ultima mensagem
	private double timeAYCoord;
	private double timerToMerge = 0;
	private double timeStartAYThere = 0;
	
	
	// Tipo do estado: 0 - 'Normal' | 1 - 'Election' | 2 'Reorganizing'
	// private int state = 0;
	private State state = State.NORMAL;
	
	// AnswerAYCoord
	private int timeAnswerAYCoord = 4;
	private boolean waitingAnswerAYCoord = false;
	
	// AnswerInvitation
	private double timeOutAnswerInvitation = 0;
	private int waitingAnswerInvitation = 0;
	private double timeMerge = 0;
	
	// AnswerReorganizing
	private int waitingAnswerReorganizing = 0;
	private double timeOutAnswerReorganizing = 0;

	private Random randomGenerator;
	private boolean log_on = true;

	// Stats
	private boolean isFirstStableStep;
	private int messages = 0;
	private int rounds = 0;
	private static int instableGlobal = 0;
	private static int instableLocal = 0;
	private static boolean hasOneInstable = false;

	//#region Node
	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message message = inbox.next();
			if (message instanceof AYCoord) {
				log("Node: " + this.getID() + " Received MSG AYCoord by " + ((AYCoord) message).sender.getID());
				this.answerAYCoord((AYCoord) message);
			}

			if (message instanceof AYCAnswer) {
				log("Node: " + this.getID() + " Received MSG AYC_answer by " + ((AYCAnswer) message).sender.getID());
				this.processAYCAnswer((AYCAnswer) message);
			}
			if (message instanceof Invitation) {
				log("Node: " + this.getID() + " Received MSG Invitation by " + ((Invitation) message).sender.getID());
				this.answerInvitation((Invitation) message);
			}
			if (message instanceof Accept) {
				log("Node: " + this.getID() + " Received MSG Accept by " + ((Accept) message).sender.getID());
				this.answerAccept((Accept) message);
			}
			if (message instanceof AcceptAnswer) {
				log("Node: " + this.getID() + " Received MSG Accept_answer by " + ((AcceptAnswer) message).getSender().getID());
				this.processAcceptAnswer((AcceptAnswer) message);
			}

			if (message instanceof AYThere) {
				log("Node: " + this.getID() + " Received MSG AYThere by " + ((AYThere) message).sender.getID());
				this.answerAYThere((AYThere) message);
			}
			if (message instanceof AYThere_answer) {
				log("Node: " + this.getID() + " Received MSG AYThere_answer by " + ((AYThere_answer) message).sender.getID());
				this.processAYThereAnswer((AYThere_answer) message);
			}
			if (message instanceof Ready) {
				log("Node: " + this.getID() + " Received MSG Ready by " + ((Ready) message).sender.getID());
				this.answerReady((Ready) message);
			}
			if (message instanceof ReadyAnswer) {
				log("Node: " + this.getID() + " Received MSG Ready_answer by " + ((ReadyAnswer) message).sender.getID());
				this.processReadyAnswer((ReadyAnswer) message);
			}

		}
	}

	@Override
	public void init() {
		this.up = new ArrayList<Node>();
		this.upSet = new ArrayList<Node>();
		this.others = new ArrayList<Node>();
		this.CoordinatorGroup = this;
		this.CoordinatorCount = 1;
		this.setColor(Color.RED);
		this.randomGenerator = new Random();
		this.isFirstStableStep = true;
		this.messages = 0;
		this.rounds = 0;
		this.waitingAnswerAYCoord = false;
		log(String.format("##### New Round #####"));
	}

	@Override
	public void preStep() {
		this.checkMembers();
		this.checkCoord();
		this.checkTimerToMerge();
		this.timeOutAYCoord();
		this.timeOutAYThere();
		this.timeOutMerge();
		this.timeOutReorganizing();
		this.timeOutAnswerInvitation();
	}

	@Override
	public void postStep() {
		this.messages += Global.getNumberOfMessagesInThisRound();
		this.rounds++;
		// REPORT
		if ((this.getID() % Tools.getNodeList().size() == 0)) {
			if (hasOneInstable) {
				instableLocal++;
				hasOneInstable = false;
			}
			if ((Global.getCurrentTime() % 100 == 0)) {
				instableGlobal = 0;
				instableLocal = 0;
			}
		}
	}

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		this.drawAsDisk(g, pt, highlight, this.getDrawingSizeInPixels());
		this.drawNodeAsDiskWithText(g, pt, highlight, String.format("%1$s | %2$s", this.getID(), this.getCoordinatorGroup().getID()), 20, Color.WHITE);
		if (this.IamCoordinator()) {
			this.setColor(Color.RED);
		} else {
			this.setColor(Color.BLACK);
		}
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}

	private boolean IamCoordinator() {
		if (this.CoordinatorGroup == null) {
			this.CoordinatorGroup = this;
			return true;
		}
		return this.getID() == this.CoordinatorGroup.getID();
	}

	//#endregion

	//#region Merge

	private void checkTimerToMerge() {
		if ((this.state == State.NORMAL) && (this.timerToMerge == Global.getCurrentTime())) {
			this.merge();
		}
	}

	private void merge() {
		log("Start merge " + this.getID());
		if ((this.IamCoordinator()) && (this.state == State.NORMAL)) {
			this.state = State.ELECTION;
			this.CoordinatorCount++;
			this.timerToMerge = 0;
			this.upSet = this.up;
			this.up = new ArrayList<Node>();
			this.waitingAnswerInvitation = 0;
			Invitation message = new Invitation(this, this.CoordinatorCount);
			for (Node no : this.others) {
				this.waitingAnswerInvitation++;
				this.send(message, no);
			}
			for (Node no : this.upSet) {
				this.waitingAnswerInvitation++;
				this.send(message, no);
			}
			this.timeOutAnswerInvitation = Global.getCurrentTime();
			this.timeMerge = Global.getCurrentTime();
		}
	}

	//#endregion

	//#region Invitation

	private void answerInvitation(Invitation message) {
		if (this.state == State.NORMAL) {
			this.oldCoordinatorGroup = this.CoordinatorGroup;
			this.upSet = this.up;
			this.state = State.ELECTION;
			this.CoordinatorGroup = message.sender;
			this.CoordinatorCount = message.CoordinatorCount;
			if (this.oldCoordinatorGroup == this) {
				for (Node no : this.upSet) {
					this.send(message, no);
				}
			}
			Accept accept = new Accept(this, this.CoordinatorCount);
			this.send(accept, message.sender);
			this.timeMerge = Global.getCurrentTime();
		}
	}

	//#endregion

	//#region Answers

	private void answerAYCoord(AYCoord aycoord) {
		AYCAnswer ayc_answer = new AYCAnswer(this, this.CoordinatorGroup);
		this.send(ayc_answer, aycoord.sender);
	}

	private void processAYCAnswer(AYCAnswer message) {
		if (message.coord.getID() != this.getID()) {
			log("AYC_answer by " + message.sender.getID() + " said your coord is " + message.coord);
			this.others.add(message.sender);
		}
	}

	private void answerAccept(Accept message) {
		AcceptAnswer accept_answer;
		if ((this.state == State.ELECTION) && (this.IamCoordinator()) && (message.CoordinatorCount == this.CoordinatorCount)) {
			this.up.add(message.sender);
			accept_answer = new AcceptAnswer(this, true);
		} else {
			accept_answer = new AcceptAnswer(this, false);
		}
		this.send(accept_answer, message.sender);
		this.waitingAnswerInvitation--;
		if (this.waitingAnswerInvitation == 0) {
			this.Reorganizing();
		}

	}

	private void answerReady(Ready message) {
		ReadyAnswer accept;
		if ((message.CoordinatorCount == this.CoordinatorCount) && (this.state == State.ELECTION)) {
			accept = new ReadyAnswer(this, this.CoordinatorCount, true);
		} else {
			accept = new ReadyAnswer(this, this.CoordinatorCount, false);
		}
		this.state = State.NORMAL;
		this.send(accept, message.sender);
	}

	private void answerAYThere(AYThere message) {
		AYThere_answer ayt_answer;
		if (this.IamCoordinator()) {
			if ((message.CoordinatorCount == this.CoordinatorCount) && (this.up.contains(message.sender))) {
				ayt_answer = new AYThere_answer(this, true, Math.max(this.CoordinatorCount, message.CoordinatorCount));
			} else {
				ayt_answer = new AYThere_answer(this, false, Math.max(this.CoordinatorCount, message.CoordinatorCount));
			}
			this.send(ayt_answer, message.sender);
		}
	}


	private void Reorganizing() {
		log("Start Reorganizing " + this.getID());
		if ((this.IamCoordinator()) && (this.state == State.ELECTION)) {
			this.state = State.REORGANIZING;
			this.waitingAnswerReorganizing = 0;
			this.timeOutAnswerReorganizing = Global.getCurrentTime();
			for (Node no : this.up) {
				Ready ready = new Ready(this, this.CoordinatorCount);
				this.waitingAnswerReorganizing++;
				this.send(ready, no);
			}
		}
	}

	private void processAcceptAnswer(AcceptAnswer message) {
		this.state = State.ELECTION;
		this.others.clear();
		this.timerToMerge = 0;
		this.timeMerge = 0;
	}

	private void processAYThereAnswer(AYThere_answer message) {
		if (!IamCoordinator()) {
			if (message.answer) {
				this.timeStartAYThere = 0;
			}
		}
	}

	private void processReadyAnswer(ReadyAnswer message) {
		if ((message.accept) && (message.CoordinatorCount == this.CoordinatorCount)) {
			this.waitingAnswerReorganizing--;
		}
		if (this.waitingAnswerReorganizing == 0) {
			this.state = State.NORMAL;
			this.waitingAnswerAYCoord = false;
			this.timeOutAnswerReorganizing = 0;
		}
	}

	//#endregion


	//#region Time
	private void checkMembers() {
		if ((Global.getCurrentTime() % this.waitConst == 0) && this.flipTheCoin()) {
			if ((this.IamCoordinator()) && (!this.waitingAnswerAYCoord) && (this.state == State.NORMAL)) {
				this.others = new ArrayList<Node>();
				AYCoord ayCoord = new AYCoord(this);
				this.broadcast(ayCoord);
				if (this.getOutgoingConnections().size() > 0) {
					this.waitingAnswerAYCoord = true;
				}
				this.timeAYCoord = Global.getCurrentTime();
			}
		}
	}

	private void checkCoord() {
		if ((this.rounds % this.waitConst == 0) && this.flipTheCoin()) {
			if ((this.timeStartAYThere == 0) && (this.state == State.NORMAL) && (!this.IamCoordinator())) {
				AYThere aythere = new AYThere(this, this.CoordinatorCount);
				this.send(aythere, this.CoordinatorGroup);
				this.timeStartAYThere = Global.getCurrentTime();
				log("AYThere from " + this.CoordinatorGroup.getID());
			}
		}
	}

	private void timeOutAYThere() {
		if (this.timeStartAYThere > 0) {
			double temp = Global.getCurrentTime() - this.timeStartAYThere;
			if ((this.state == State.NORMAL) && (temp > this.timeOut)) {
				this.timeStartAYThere = 0;
				log("Time out timeOutAYThere " + this.getID());
				this.recovery(" timeOutAYThere coord " + this.CoordinatorGroup);
			}
		}

	}



	private void recovery(String motive) {
		log("Recorevy " + this.getID() + " MOTIVO " + motive);
		this.CoordinatorCount++;
		this.oldCoordinatorGroup = this.CoordinatorGroup;
		this.CoordinatorGroup = this;
		this.state = State.NORMAL;
		this.timeAYCoord = 0;
		this.timerToMerge = 0;
		this.timeStartAYThere = 0;
		this.waitingAnswerAYCoord = false;
		this.timeOutAnswerInvitation = 0;
		this.waitingAnswerInvitation = 0;
		this.timeMerge = 0;
		this.up = new ArrayList<Node>();
		this.upSet = new ArrayList<Node>();
		this.others = new ArrayList<Node>();
	}


	private void timeOutMerge() {
		if (this.state == State.ELECTION) {
			double temp = Global.getCurrentTime() - this.timeMerge;
			if (temp > this.timeOut) {
				this.timeMerge = 0;
				log("timeOutAcceptInvitation " + this.getID() + " time " + temp);
				this.recovery("timeOutMerge");
			}
		}
	}

	private void timeOutReorganizing() {
		if (this.state == State.REORGANIZING) {
			double temp = Global.getCurrentTime() - this.timeOutAnswerReorganizing;
			if (temp > this.timeOut) {
				this.timeOutAnswerReorganizing = 0;
				this.recovery("timeOutReorganizing " + this.getID() + " time " + temp);
			}
		}
	}

	private void timeOutAYCoord() {
		if ((this.state == State.NORMAL) && (this.waitingAnswerAYCoord)) {
			double temp = Global.getCurrentTime() - this.timeAYCoord;
			if ((temp > this.timeAnswerAYCoord) && (this.timerToMerge < Global.getCurrentTime())) {
				if (this.others.size() > 0) {
					this.waitingAnswerAYCoord = false;
					this.timerToMerge = Global.getCurrentTime() + (Tools.getNodeList().size() * 10 + (10 - this.getID() * 10));
					log("SetUP timerToMerge " + this.getID() + " STATUS " + this.state + " timerToMerge " + this.timerToMerge);
				} else {
					this.waitingAnswerAYCoord = false;
				}
			}
		}
	}

	private void timeOutAnswerInvitation() {
		double temp = Global.getCurrentTime() - this.timeOutAnswerInvitation;
		if ((this.waitingAnswerInvitation > 0) && (temp > this.timeOut)) {
			log("Time out timeOutAnswerInvitation by getID() " + this.getID() + " SIZE " + this.waitingAnswerInvitation);
			this.state = State.NORMAL;
			this.waitingAnswerInvitation = 0;
			this.timeOutAnswerInvitation = 0;
			log("Time out timeOutAnswerInvitation " + this.getID());
		}
	}

	//#endregion

	//#region Utils

	private void log(String message) {
		if (IamCoordinator())
			this.logging.logln("STEP " + ((int) (Math.round(Global.getCurrentTime()))) + "- Node  " + this.getID() + ": " + message);
		else
			this.logging.logln("STEP " + ((int) (Math.round(Global.getCurrentTime()))) + "- Coord " + this.getID() + ": " + message);
	}

	private boolean flipTheCoin() {
		int num_randomico = randomGenerator.nextInt(100);
		if (coinChancePositive > num_randomico) {
			return true;
		} else {
			return false;
		}
	}

	private String getNameFile() {
		String prefixo = "0_";
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy-'at'-hh-mm-ss-SSS-a");
		Date today = new Date();
		return prefixo + ft.format(today) + "_report.csv";
	}

	//#endregion

}

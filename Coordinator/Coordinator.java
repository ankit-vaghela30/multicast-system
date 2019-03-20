import java.net.*;
import java.io.*;
import java.util.*;
import java.time.temporal.ChronoUnit;

class ParticipantInstance extends Thread {
private static final String INVALID_CMD_MESSAGE = "Invalid command.";
private static final String UNEXPECTED_ERROR = "Unexpected error occured";
private static final String WAITING_MSG = "Waiting for Connection...";
private Socket portSocket;
private long thresholdTime;
private DataOutputStream dos;
private DataInputStream dis;
private Socket messageSocket;
private DataOutputStream dos_message;
private DataInputStream dis_message;
private int participatorId;
private boolean isMessageThread = false;

//map which holds active-inactive value for participator
public long getThresholdTime(){
	return this.thresholdTime;
}
private static Map<Integer, Boolean> multicastGroupMap = new HashMap<Integer, Boolean>();
public static Map<Integer, Boolean> getMulticastGroupMap(){
	return multicastGroupMap;
}
public static void addParticipant(int participantId, boolean active){
	multicastGroupMap.put(participantId, active);
}
public static void modifyParticipant(int participantId, boolean flag){
	multicastGroupMap.put(participantId, flag);
}
public static void removeParticipant(int participantId){
	multicastGroupMap.remove(participantId);
}
public static Boolean getParticipant(int participantId){
	return multicastGroupMap.get(participantId);
}
private static Map<Integer, String> multicastMessageMap = new HashMap<Integer, String>();
public static Map<Integer, String> getMulticastMessageMap(){
	return multicastMessageMap;
}
public static void setParticipantMessage(int participantId, String message){
	multicastMessageMap.put(participantId, message);
}
public static void removeParticipantMessage(int participantId){
	multicastMessageMap.remove(participantId);
}
public static String getParticipantMessage(int participantId){
	return multicastMessageMap.get(participantId);
}
private static Map<Integer, Map<Calendar,String> > persistantPartiMap = new HashMap<Integer, Map<Calendar,String> >();
public static Map<Integer, Map<Calendar,String> > getPersistantPartiMap(){
	return persistantPartiMap;
}
public static void setPersistantPartiMap(Map<Integer, Map<Calendar,String> > m){
	persistantPartiMap = m;
}

public ParticipantInstance(Socket portSocket, long thresholdTime){
	try{

																System.out.println("In ParticipantInstance constructor");
																this.portSocket = portSocket;
																this.thresholdTime = thresholdTime;

								}catch(Exception e) {
																e.printStackTrace();
								}
}
public ParticipantInstance(DataOutputStream dos_message, int participatorId, long thresholdTime){
								try{
																this.isMessageThread = true;
																this.dos_message = dos_message;
																this.participatorId = participatorId;
																this.thresholdTime = thresholdTime;
								}catch(Exception e) {
																e.printStackTrace();
								}
}

public ParticipantInstance(){

}

//Method to register a participator
public void register(int participatorId, String IpAddress, int messagePortNumber){
								try{
																this.participatorId = participatorId;
																this.messageSocket = new Socket(IpAddress, messagePortNumber);
																this.dis_message = new DataInputStream(this.messageSocket.getInputStream());
																this.dos_message = new DataOutputStream(this.messageSocket.getOutputStream());
																new ParticipantInstance().addParticipant(participatorId, true);
								} catch(Exception e) {
																e.printStackTrace();
								}
}

//Method to deregister a participator
public void deregister(int participatorId){
								try{
																new ParticipantInstance().removeParticipant(participatorId);
																Map<Integer, Map<Calendar,String> > tempPersistantPartiMap = new ParticipantInstance().getPersistantPartiMap();
																tempPersistantPartiMap.remove(participatorId);
								}
								catch(Exception e) {
																e.printStackTrace();
								}
}

//Method to disconnect or for participator to go temporarily offline
public void disconnect(int participatorId){
								new ParticipantInstance().modifyParticipant(participatorId, false);
								//this.disconnectFlag = true;

								//Map<Integer, Calendar> tempPartiCalendarMap = new ParticipantInstance().getPartiCalendarMap();
								//Calendar cal = Calendar.getInstance();
								//cal.setTime(new Date());
								//tempPartiCalendarMap.put(participatorId, cal);
								//new ParticipantInstance().setPartiCalendarMap(tempPartiCalendarMap);
}

//Method to multicast the message to all participants
public void multicastSend(String message){
								try{
																Map<Integer, Boolean> tempMulticastGroupMap = new ParticipantInstance().getMulticastGroupMap();
																for(Integer participatorId : tempMulticastGroupMap.keySet()) {

																								if(tempMulticastGroupMap.get(participatorId)) {
																																new ParticipantInstance().setParticipantMessage(participatorId, message);
																								}else{
																																//Map<Integer, Calendar> tempPartiCalendarMap = new ParticipantInstance().getPartiCalendarMap();
																																//Calendar startCal = tempPartiCalendarMap.get(participatorId);
																																Calendar nowCal = Calendar.getInstance();
																																nowCal.setTime(new Date());
																																//long secondsBetween = ChronoUnit.SECONDS.between(startCal.toInstant(), nowCal.toInstant());
																																//if(secondsBetween <= this.getThresholdTime()) {
																																Map<Integer, Map<Calendar,String> > tempPersistantPartiMap = new ParticipantInstance().getPersistantPartiMap();
																																Map<Calendar, String> tempMessageMap;
																																if(!(null ==  tempPersistantPartiMap.get(participatorId))) {
																																								tempMessageMap = tempPersistantPartiMap.get(participatorId);
																																}else{
																																								tempMessageMap = new HashMap<Calendar, String>();
																																}
																																tempMessageMap.put(nowCal, message);
																																tempPersistantPartiMap.put(participatorId, tempMessageMap);
																																new ParticipantInstance().setPersistantPartiMap(tempPersistantPartiMap);
																																//}
																								}
																}
								}catch(Exception e) {
																e.printStackTrace();
								}
}

//Method to reconnect or for participator to come online
public void reconnect(int participatorId, String IpAddress, int messagePortNumber){
								try{
																this.messageSocket = new Socket(IpAddress, messagePortNumber);
																this.dis_message = new DataInputStream(this.messageSocket.getInputStream());
																this.dos_message = new DataOutputStream(this.messageSocket.getOutputStream());
																new ParticipantInstance().modifyParticipant(participatorId, true);
								}catch(Exception e) {
																e.printStackTrace();
								}
}

public void run(){
								try{
																boolean temp = true;

																if(this.isMessageThread) {

																								System.out.println("In the message thread");
																								while(temp) {
																																if((null == new ParticipantInstance().getParticipant(this.participatorId))) {
																																								temp = false;;
																																}
																																else if(!new ParticipantInstance().getParticipant(this.participatorId)) {
																																								temp = false;
																																}else{
																																								//for(Integer id : new ParticipantInstance().getMulticastMessageMap().keySet()) {
																																								//							System.out.println("xxxxxxx-----------"+new ParticipantInstance().getParticipantMessage(id));
																																								//}
																																								//System.out.println("xxxxxxx-----------"+new ParticipantInstance().getParticipantMessage(this.participatorId));
																																								//System.out.println("xxxxxxx-----------"+new ParticipantInstance().getParticipantMessage(Integer.valueOf(this.participatorId)));
																																								//System.out.println("Me gaya andar"+new ParticipantInstance().getParticipantMessage(this.participatorId));
																																								if(!(null == new ParticipantInstance().getParticipantMessage(this.participatorId))) {
																																																System.out.println("Me gaya andar"+new ParticipantInstance().getParticipantMessage(this.participatorId));
																																																this.dos_message.writeUTF(new ParticipantInstance().getParticipantMessage(this.participatorId));
																																																new ParticipantInstance().removeParticipantMessage(this.participatorId);
																																								}
																																								Map<Integer, Map<Calendar,String> > tempPersistantPartiMap = new ParticipantInstance().getPersistantPartiMap();
																																								if(!(null == tempPersistantPartiMap.get(this.participatorId))) {
																																																System.out.println("Me gaya persistance ke andar");
																																																Map<Calendar, String> tempMessageMap = tempPersistantPartiMap.get(participatorId);
																																																for(Calendar cal : tempMessageMap.keySet()) {
																																																								Calendar nowCal = Calendar.getInstance();
																																																								nowCal.setTime(new Date());
																																																								long secondsBetween = ChronoUnit.SECONDS.between(cal.toInstant(), nowCal.toInstant());
																																																								System.out.println("Time difference is "+secondsBetween+" and td is "+this.getThresholdTime());
																																																								if(secondsBetween <= this.getThresholdTime()) {
																																																																System.out.println("Aur message bheja"+tempMessageMap.get(cal));
																																																																this.dos_message.writeUTF(tempMessageMap.get(cal));
																																																								}
																																																}
																																																tempPersistantPartiMap.remove(this.participatorId);
																																								}
																																								new ParticipantInstance().setPersistantPartiMap(tempPersistantPartiMap);
																																}
																								}
																}else{
																								String message = "Chat started!";
																								System.out.println("Connected port "+this.portSocket);
																								dos=new DataOutputStream(this.portSocket.getOutputStream()); //send message to the Client
																								dis=new DataInputStream(this.portSocket.getInputStream()); //get input from the client
																								String command = "";

																								while(message!="exit") {
																																System.out.println("Coordinator while loop");
																																command = this.dis.readUTF();
																																System.out.println("Command called: " +command);
																																if(command != null && command.split(" ")[0].equalsIgnoreCase("register")) {
																																								register(Integer.parseInt(command.split(" ")[1]), command.split(" ")[2], Integer.parseInt(command.split(" ")[3]));
																																								dos.writeUTF("connection made");
																																								ParticipantInstance messageThread = new ParticipantInstance(this.dos_message, this.participatorId, this.thresholdTime);
																																								messageThread.start();
																																}
																																else if(command != null && command.contains("msend")) {
																																								multicastSend(command.split(" ")[1]);
																																}
																																else if(command != null && command.contains("disconnect")) {
																																								disconnect(this.participatorId);
																																								dos.writeUTF("ok");
																																}
																																else if(command != null && command.contains("reconnect")) {
																																								reconnect(Integer.parseInt(command.split(" ")[1]),command.split(" ")[2], Integer.parseInt(command.split(" ")[3]));
																																								dos.writeUTF("connection made again");
																																								ParticipantInstance messageThread = new ParticipantInstance(this.dos_message, this.participatorId, this.thresholdTime);
																																								messageThread.start();
																																}
																																else if(command != null && command.contains("deregister")) {
																																								deregister(this.participatorId);
																																								dos.writeUTF("ok");
																																}
																																else{
																																								dos.writeUTF(INVALID_CMD_MESSAGE);
																																}

																								}
																								System.out.println("Coordinator has stopped running");

																}
								}
								catch(Exception e) {
																e.printStackTrace();
								}
}
}
/**
   This class handles multiple client connections and spawns off new thread for each client
 */
class Coordinator extends Thread {

private ServerSocket portServer;
private int portNumber;
private long thresholdTime;
private static final String UNEXPECTED_ERROR = "Unexpected error occured";

public Coordinator(int portNumber, long thresholdTime) {
								this.portNumber = portNumber;
								this.thresholdTime = thresholdTime;
}
//

public void run() {
								try{
																this.portServer = new ServerSocket(portNumber);

																while(true) {
																								Socket portParticipantSocket = null;
																								try {
																																System.out.println("Server will start to wait" + this.portServer);
																																portParticipantSocket = this.portServer.accept();
																								} catch(Exception e) {
																																System.out.println("Error Connecting to the server");
																																e.printStackTrace();
																								}
																								System.out.println("Participant connected port"+portParticipantSocket);
																								ParticipantInstance participant = new ParticipantInstance(portParticipantSocket, thresholdTime);
																								participant.start();
																}
								}catch(Exception e) {
																e.printStackTrace();
								}
}
//------------------main method-------------------
public static void main(String args[]) throws Exception {
								try{
																File file = new File(args[0]);
																Scanner sc = new Scanner(file);
																int portNumber = Integer.parseInt(sc.nextLine());
																long thresholdTime = Long.parseLong(sc.nextLine());
																Coordinator participatorsManager = new Coordinator(portNumber, thresholdTime);
																participatorsManager.start();
								} catch(Exception e) {
																System.out.println(UNEXPECTED_ERROR+": "+e);
								}
}
}

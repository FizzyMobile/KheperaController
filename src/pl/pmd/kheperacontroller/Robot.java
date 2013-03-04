package pl.pmd.kheperacontroller;

import java.io.IOException;
import java.net.UnknownHostException;

public class Robot {
	private RobotConnection inRobotConnection;
	
	public Robot(){
		inRobotConnection = new RobotConnection();
	}
	
	public void connect(String argIP, int argPort) throws UnknownHostException, IOException{
		inRobotConnection.connect(argIP, argPort);
	}
	
	public void disconnect(){
		inRobotConnection.disconnect();
	}
	
	public boolean isConnected(){
		return inRobotConnection.isConnected();
	}
	
	public void move(int argLeft, int argRight){
		String tmpCommand = "$SPEED," + Integer.toString(argLeft) + "," + Integer.toString(argRight) + "\n";
		inRobotConnection.send(tmpCommand);
	}
	
	public void stop(){
		String tmpCommand = "$SPEED,0,0\n";
		inRobotConnection.send(tmpCommand);
	}
}

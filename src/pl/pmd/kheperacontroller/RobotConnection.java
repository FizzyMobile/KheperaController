package pl.pmd.kheperacontroller;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class RobotConnection {
	Socket inSocket = null;
 
    private int write(String argText){
    	try {
    		PrintWriter tmpBufferedWriter = new PrintWriter(new OutputStreamWriter(inSocket.getOutputStream()));
    		tmpBufferedWriter.print(argText);
    		tmpBufferedWriter.flush();
    		return argText.length();
        }
    	catch (IOException e) {
            e.printStackTrace();
        }
    	return 0;
    }
    
    public void connect(String argIP, int argPort) throws UnknownHostException, IOException{
    	inSocket = new Socket(argIP, argPort);
    }
    
    public void disconnect(){
    	try {
    		inSocket.close();
        }
    	catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean isConnected(){
    	return (inSocket == null)? false : inSocket.isConnected();
    }

    public void send(String argText){
        if(isConnected()){
        	write(argText);
        }
    }
}

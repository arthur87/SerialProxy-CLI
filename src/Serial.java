import gnu.io.*;
import java.io.*;
import java.util.*;

public class Serial implements SerialPortEventListener {
	private SerialPort port;
	
	public int portRate = 9600;
	public int portParity = SerialPort.PARITY_NONE;
	public int portDataBits = 8;
	public int portStopBits = SerialPort.STOPBITS_1;
	
	private InputStream input;
	private OutputStream output;
	
	public Serial()
	{
	}
	public boolean openPort(String portName) {
		try {
			CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);
			CommPort commPort = portId.open("IOProxy" + new Random().nextInt(100), 2000);
			port = (SerialPort)commPort;
			input = port.getInputStream();
			output = port.getOutputStream();
			port.setSerialPortParams(portRate, portDataBits, portStopBits, portParity);
			port.addEventListener(this);
			port.notifyOnDataAvailable(true);
		}catch(Exception e) {
			return false;
		}
		return true;
	}
	public void closePort() {
		try {
			if(input != null) input.close();
			if(output != null) output.close();
		}catch(Exception e) {}
		input = null;
		output = null;
		
		try {
			if(port != null) {
				port.removeEventListener();
				port.close();
			}
		}catch(Exception e){}
		port = null;
	}
	byte buffer[] = new byte[32768];
	int bufferIndex;
	int bufferLast;
	int bufferSize = 1;
	boolean bufferUntil;
	int bufferUntilByte;
	synchronized public void serialEvent(SerialPortEvent event) {
		if(event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				while(input.available() > 0) {
					synchronized(buffer) {
						if(bufferLast == buffer.length) {
							byte temp[] = new byte[bufferLast << 1];
							System.arraycopy(buffer, 0, temp, 0, bufferLast);
							buffer = temp;
						}
						buffer[bufferLast++] = (byte)input.read();
					}
				}
			}catch(IOException e) {
			}
		}
	}
	
	public int available()
	{
		return (bufferLast - bufferIndex);
	}
	public void clear()
	{
		bufferLast = 0;
		bufferIndex = 0;
	}
	public int read()
	{
		if(bufferIndex == bufferLast) return -1;
		synchronized(buffer){
			int outgoing = buffer[bufferIndex++] & 0xff;
			if(bufferIndex == bufferLast) {
				bufferIndex = 0;
				bufferLast = 0;
			}
			return outgoing;
		}
	}
	public int last()
	{
		if(bufferIndex == bufferLast) return -1;
		synchronized(buffer){
			int outgoing = buffer[bufferLast-1];
			bufferIndex = 0;
			bufferLast = 0;
			return outgoing;
		}
	}
	public char readChar()
	{
		if(bufferIndex == bufferLast) return (char)(-1);
		return (char)read();
	}
	public char lastChar()
	{
		if(bufferIndex == bufferLast) return (char)(-1);
		return (char)last();
	}
	public byte[] readBytes()
	{
		if(bufferIndex == bufferLast) return null;
		synchronized(buffer) {
			int length = bufferLast - bufferIndex;
			byte outgoing[] = new byte[length];
			System.arraycopy(buffer, bufferIndex, outgoing, 0, length);
			bufferIndex = 0;
			bufferLast = 0;
			return outgoing;
		}
	}
	public int readBytes(byte outgoing[])
	{
		if(bufferIndex == bufferLast) return 0;
		synchronized(buffer){
			int length = bufferLast - bufferIndex;
			if(length > outgoing.length) length = outgoing.length;
			System.arraycopy(buffer, bufferIndex, outgoing, 0, length);
			bufferIndex += length;
			if(bufferIndex == bufferLast){
				bufferIndex = 0;
				bufferLast = 0;
			}
			return length;
		}
	}
	
	public byte[] readBytesUntil(int interesting)
	{
		if(bufferIndex == bufferLast) return null;
		byte what = (byte)interesting;
		synchronized(buffer){
			int found = -1;
			for(int k = bufferIndex; k < bufferLast; k++) {
				if(buffer[k] == what) {
					found = k;
					break;
				}
			}
			if(found == -1) return null;
			int length = found - bufferIndex + 1;
			byte outgoing[] = new byte[length];
			System.arraycopy(buffer, bufferIndex, outgoing, 0, length);
			
			bufferIndex += length;
			if(bufferIndex == bufferLast){
				bufferIndex = 0;
				bufferLast = 0;
			}
			return outgoing;
		}
	}

	public int readBytesUntil(int interesting, byte outgoing[])
	{
		if(bufferIndex == bufferLast) return 0;
		byte what = (byte)interesting;
		synchronized(buffer) {
			int found = -1;
			for(int k = bufferIndex; k < bufferLast; k++){
				if(buffer[k] == what){
					found = k;
					break;
				}
			}
			if(found == 0) return 0;
			int length = found - bufferIndex + 1;
			if(length > outgoing.length) return -1;
			System.arraycopy(buffer, bufferIndex, outgoing, 0, length);
			
			bufferIndex += length;
			if(bufferIndex == bufferLast) {
				bufferIndex = 0;
				bufferLast = 0;
			}
			return length;
		}
	}
	
	public String readString()
	{
		if(bufferIndex == bufferLast) return null;
		return new String(readBytes());
	}
	public String readStringUntil(int interesting)
	{
		byte b[] = readBytesUntil(interesting);
		if(b == null) return null;
		return new String(b);
	}
	
	
	public void write(int what)
	{
		try{
			output.write(what & 0xff);
			output.flush();
		}catch(Exception e) {			
		}
	}

	public void write(byte bytes[])
	{
		try{
			output.write(bytes);
			output.flush();
		}catch(Exception e) {			
		}
	}
	public void write(String s)
	{
		try {
			output.write(s.getBytes());
			output.flush();
		}catch(Exception e) {
		}
	}

}
import java.util.Enumeration;

import gnu.io.CommPortIdentifier;

public class Main {
	private Server server;
	private Serial serial;
	public Main(String[] args)
	{
		Runtime.getRuntime().addShutdownHook(new Shutdown());
		server = new Server(this, Integer.valueOf(args[0]));
		server.start();
		serial = new Serial();
		serial.portRate = Integer.valueOf(args[1]);
		if(serial.openPort(args[2])){
			while(true) {
				if(serial.available() > 0){
					if(args[3].toUpperCase().equals("READ")) this.sendToClient(serial.read());
					if(args[3].toUpperCase().equals("READ_CHAR")) this.sendToClient(serial.readChar());
				}
			}
		}
	}
	public static void main(String[] args)
	{
		if(args.length == 4) {
			new Main(args);
		}else {
			usage();
		}
	}
	public void sendToClient(String s) {
		server.sendToAllClient(s);
	}
	public void sendToClient(int n) {
		server.sendToAllClient(String.valueOf(n));
	}
	public void sendToClient(char c) {
		server.sendToAllClient(String.valueOf(c));
	}
	public void sendToSerial(String s) {
		try {
			serial.write(s);
		}catch(Exception e) {}
	}
	
	public void printLogMessage(String s) {
		System.out.println(s);
	}

	private static String BR = System.getProperty("line.separator");
	private static String APP_NAME = "SerialProxy-CLI Preview2 (20110430)";
	private static void usage()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(APP_NAME + BR);
		sb.append("Usage: SerialProxy-CLI <serverPort> <serialBoundRate> <serialPort> <readType>" + BR);
		sb.append(" <serialBoundRate> 300, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 34800, 57600, 115200" + BR);
		sb.append(" <readType> READ, READ_CHAR" + BR);
		sb.append(BR);
		sb.append("Exsamples" + BR);
		sb.append(" SerialProxy-CLI 9000 57600 /dev/tty.usbdevice READ" + BR);
		sb.append(" SerialProxy-CLI 9000 57600 COM1 READ" + BR);
		System.out.println(sb.toString());
		String serialPortList = getSerialPort();
		System.out.println(BR);
		System.out.println("Serial Port(s)");
		System.out.println(serialPortList);
	}
	private static String  getSerialPort()
	{
		StringBuffer sb = new StringBuffer();
		try {
			Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
			while(portList.hasMoreElements()) {
				CommPortIdentifier portId = (CommPortIdentifier)portList.nextElement();
				if(portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					String portName = portId.getName();
					if(portName.startsWith("/dev/tty") || portName.startsWith("COM")) {
						sb.append(" " + portName + BR);
					}
				}
			}
		}catch(Exception e) {
		}
		return sb.toString();
	}
	class Shutdown extends Thread {
		@Override
		public void run() {
			if(serial != null) {
				serial.closePort();
			}
			if(server != null) {
				server.stopServer();
			}
		}
	}
}

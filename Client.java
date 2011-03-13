import java.applet.*;
import java.awt.*;
import java.lang.*;
import java.net.*;
import java.io.*;
import java.util.*;


public class Client extends Applet implements Runnable {
  TextArea outputT;
  TextField inputT;
  TextField nickT;
  Button connectB;
  Button disconnB;
  Button condisB;
  Button clearB;
  Button usrCntB;
  Label usrCntL;
  Label statusL;
  Connection c;
  Thread thread;
  String host;
  int port;
	int timeout = 100;
	String sysStr = new String("");

  public void init() {
    // host = getParameter("host");
		host = "localhost";
    // port = (new Integer(getParameter("port"))).intValue();
		port = 7000;

    setLayout(new BorderLayout());

    nickT  = new TextField(16);
		nickT.setText("nick");
    inputT  = new TextField(44);

    outputT = new TextArea(20, 60);
    outputT.setEditable(false);

		// Font font = new Font("Serif", Font.ITALIC, 20);
		// outputT.setFont(font);
		outputT.setForeground(Color.blue);
		outputT.setFont(new Font("monospaced", Font.PLAIN, 14));

		usrCntL  = new Label("Users: -");
    connectB = new Button("Connect");
    disconnB = new Button("Disconnect");
    condisB  = new Button("Connect"); // !
    clearB   = new Button("Clear");
    usrCntB  = new Button("User count");

		statusL  = new Label("Disconected");

		Panel txtBoxes = new Panel();
		txtBoxes.add(nickT);
		txtBoxes.add(inputT);

    add("North",txtBoxes);
    add("Center",outputT);

    Panel buttons = new Panel();
    buttons.add(usrCntL);
    buttons.add(connectB);
    buttons.add(disconnB);
    buttons.add(condisB);
    buttons.add(clearB);
    buttons.add(statusL);
    buttons.add(usrCntB);
    add("South", buttons);

    c = new Connection();
  }


	public int usersCount(){
		int count=0;

		c.write("/userscount");
		try {
			thread.sleep(100);
		} catch (Exception e) {}

		// String str1 = new String("");
		// String str2 = new String("");
		// BufferedReader reader = new BufferedReader(
		// 	new StringReader(outputT.getText()));
		// try {
		// 	while ((str2 = reader.readLine()) != null) {
		// 		str1 = str2;
		// 	}
		// } catch(IOException e) {
		// 	e.printStackTrace();
		// }

		// return (new Integer(str1)).intValue();
		String x = sysStr.substring(3, sysStr.length()-1);
		if (sysStr!=""){
			System.out.println(sysStr);
			System.out.println(x);
			return (new Integer(x)).intValue();
		}
		else
			return -1;

		// posalji "/usercount"
		// primljeno u int
		// vrati
	}


  public void start() {
    thread = new Thread(this);
    thread.start();
  }

  public void stop() {
    thread.stop();
    thread = null;
  }


  public void disconn() {
    if (!c.connected()) {
      outputT.appendText("Not connected.\n");
			// condisB.setText("Connect");
    } else {
      c.close();
      outputT.appendText("Disconnected.\n");
			statusL.setText("Disconected");
			// condisB.setText("Connect");
      stop();
    }
  }


  public void connect() {
    if (c.connected()) {
      outputT.appendText("Already connected!\n");
      return;
    }

    outputT.appendText("Attempting to connect...\n");
    c.connect(host, port);

    if (c.connected()) {
      c.write(nickT.getText());
      outputT.appendText("Connection established.\n");
			statusL.setText("Conected");
			// condisB.setText("Disconnect");
      start();
    } else {
      outputT.appendText("Unable to connect.\n");
			statusL.setText("Failed to connect");
    }
  }


  // Allow it to be run as an application.
  public static void main(String args[]) {
        Frame f = new Frame("Client");
        Client client = new Client();

        client.init();

        f.add("Center", client);
        f.pack();
        f.show();
  }


  // run() is used to read the socket and print anything sent to us.
  // Note: this routine uses `canRead' so it will not block in the read(),
  //       (not necessary for this simple example. ie. blocking would be ok.)
  public void run() {
    while(c.connected()) {
      if (c.canRead()) {         // If there is data pending on the connection,
        String s = c.read();     // print it out.
        if (s.length() > 0)
					if (!s.startsWith("#")) {
						sysStr = "";
          	outputT.appendText(s);
					} else {
						sysStr = s;
					}
          // outputT.appendText("READ: "+s);
      }
      try { thread.sleep(timeout); } // Pause briefly.
      catch (Exception e) { }
    }
  }


  // Handle any button clicks, or an enter line of text.
  public boolean handleEvent(Event e) {
    switch(e.id) {
      case Event.ACTION_EVENT:
        if (e.target == inputT) {
          if (c.connected()) {
            c.write((String)e.arg);
						outputT.appendText(
							"<"+nickT.getText()+"> "+
							(String)e.arg+"\n"
						);
          } else {
            outputT.appendText("Not connected.\n");
          }
          inputT.setText("");
          return true;
        } else if (e.target == connectB) {
          connect();
        } else if (e.target == disconnB) {
          disconn();
				} else if (e.target == condisB) {
					if (c.connected()){
						disconn();
					} else {
						connect();
					}
        } else if (e.target == clearB) {
					outputT.setText("");
				} else if (e.target == nickT) {
					connect();
					inputT.requestFocusInWindow();
				} else if (e.target == usrCntB) {
					usrCntL.setText("Users: "+usersCount());
				}
    }
    return false;
  }

}


class Connection {
  final int MAXLENGTH = 1024;
  Socket socket;
  PrintStream out;
  DataInputStream in;

  Connection() { }


  public boolean connected() {
    return (socket!=null);
  }


  public boolean connect(String h, int p) {
    try { socket = new Socket(h, p); }
    catch (Exception e)
	{ System.err.println("Socket open: "+e); }
    if (socket==null)
      return false;
    try {
      in  = new DataInputStream(socket.getInputStream());
      out = new PrintStream(socket.getOutputStream());
    } catch (Exception e)
	{ System.err.println("Socket getInput: "+e); }
    return true;
  }


  public void close() {
    try {
      socket.close();
    } catch (Exception e)
	{ System.err.println("Socket close: "+e); }
    socket=null;
  }


  // Send data to the socket
  public void write(String s) {
    out.println(s);
  }


  // Is there data available?
  public boolean canRead() {
    try { return in.available()!=0; }
    catch (Exception e)
	{ System.err.println("Socket available: "+e); socket=null;}
    return false;
  }


  // Read some data.  Will block if nothing available.
  public String read() {
    String s;
    byte b[] = new byte[MAXLENGTH];

    int num = 0;
    try { num = in.read(b, 0, MAXLENGTH); }
    catch (Exception e)
	{ System.err.println("Socket read: "+e); socket=null;}
    if (num>0)
      s = new String(b, 0, 0, num);
    else
      s = "";
    return s;
  }

}

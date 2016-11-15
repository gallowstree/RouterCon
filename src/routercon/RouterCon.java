package routercon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

public class RouterCon 
{
    private LogFrame logFrame;
    private RouterListener listener;
    
    public RouterCon(LogFrame logFrame)
    {
        this.logFrame = logFrame;
    }

    void attach(String ip) 
    {
        this.listener = new RouterListener(ip, this);
        new Thread(this.listener).start();        
    }
    
    void receivedMessage(List<String> lines)
    {
        String firstLine = lines.remove(0);
        
        if (firstLine.equals("SP"))
            receivedShortestPaths(lines);
        else if (firstLine.endsWith("DV"))
            receivedDistanceVector(lines);
        else if (firstLine.equals("N"))
            receivedNeighbors(lines);
    }

    private void receivedShortestPaths(List<String> lines) 
    {
        Object[] columnNames = {"To", "Via", "Cost"};        
        DefaultTableModel m = new DefaultTableModel(new Object[0][0], columnNames);        
        
        for (String l : lines)
        {
            String[] pieces = l.split(":|;");
            String[] data = { pieces[1].trim(), pieces[3].trim(), pieces[5].trim() };                        
            m.addRow(data);
        }
        
        this.logFrame.tblPaths.setModel(m);
    }

    private void receivedDistanceVector(List<String> lines) {
        
    }

    private void receivedNeighbors(List<String> lines) {

    }
        
}

class RouterListener implements Runnable
{
    RouterCon console;
    public static String ip;

    RouterListener(String ip, RouterCon con) {
        RouterListener.ip = ip;
        this.console = con;
    }

    @Override
    public void run() 
    {
        try 
        {
            ServerSocket sock = new ServerSocket();
            sock.bind(new InetSocketAddress(ip,1984));
            while (true)
            {
                Socket conn = sock.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                ArrayList<String> lines = new ArrayList();
                
                for (String line = in.readLine(); line != null; line = in.readLine())
                {
                    System.out.println(line);
                    lines.add(line);
                }
                
                console.receivedMessage(lines);
                
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

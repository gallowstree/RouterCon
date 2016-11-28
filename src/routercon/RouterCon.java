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
import javax.swing.table.TableModel;

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
        new Thread(new KeepAliveMonitor()).start();
    }
    
    void receivedMessage(List<String> lines)
    {
        String firstLine = lines.remove(0);
        
        if (firstLine.equals("SP"))
            receivedShortestPaths(lines);
        else if (firstLine.equals("DV"))
            receivedDistanceVector(lines);
        else if (firstLine.equals("N"))
            receivedNeighbors(lines);
        else if (firstLine.equals("KA"))
            receivedKeepAlive(lines);
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

    private void receivedDistanceVector(List<String> lines) 
    {                        
        for (String l : lines)
        {
            this.logFrame.txtDVs.append(l);
        }
        this.logFrame.txtDVs.append("\n");
    }
    
    private void receivedKeepAlive(List<String> lines)
    {
        TableModel m = this.logFrame.tblNeighbors.getModel();
        for (String l : lines)
        {
            if (l.contains("From:") && m.getRowCount() >  0)
            {
                String from = l.split(":")[1];
                
                int row = -1;
                
                for (int i = 0; row < m.getRowCount(); i++)
                {
                    if (from.equals(m.getValueAt(i, 0)))
                    {
                        row = i;
                        break;
                    }
                }
                if (row == -1)
                    continue;
                
                m.setValueAt(0, row, 3);
            }
        }
    }

    private void receivedNeighbors(List<String> lines) 
    {
        Object[] columnNames = {"Name", "Cost", "IP" ,"Last Keep-Alive"};
        DefaultTableModel m = new DefaultTableModel(new Object[0][0], columnNames);
        
        for (String l : lines)
        {
            if (!l.contains(":")) 
                continue;
            
            String[] pieces = l.split(":");
            String[] data = { pieces[0].trim(), pieces[2].trim(), pieces[1].trim(), "N/A" };                        
            m.addRow(data);
        }
        
        this.logFrame.tblNeighbors.setModel(m);
    }
    
    class KeepAliveMonitor implements Runnable
    {
        @Override
        public void run() 
        {
            while (true)
            {
                try 
                {
                    Thread.sleep(1000);
                    TableModel m = logFrame.tblNeighbors.getModel();
                    int row;
                
                    for (row = 0; row < m.getRowCount(); row++)
                    {
                       if (m.getColumnCount() < 4)
                           continue;
                       
                       Object val = m.getValueAt(row, 3);
                       
                       if (val.equals("N/A"))
                           continue;
                                                                     
                       m.setValueAt(((int)val) + 1, row, 3);
                    }
                } catch (Exception ex) 
                {
                    ex.printStackTrace();
                }
            }
        }        
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

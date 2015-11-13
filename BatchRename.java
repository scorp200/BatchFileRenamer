import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DogeWolf on 9/8/2015.
 */
public class BatchRename extends JFrame
{
    private static final int WIDTH = 700;
    private static final int HEIGHT = 340;

    public static void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        catch (Exception e)
        {
        }
        JFrame frame = new BatchRename();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        frame.setTitle("Batch File Rename");
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private JCheckBox ignore;

    private JButton apply;
    private JButton open;
    private JButton exit;
    private JButton minimize;
    private JButton numeric;

    private JTextList fileList;

    private File[] files;
    private ArrayList<String> fileNames;

    private Listener listener = new Listener();
    private String path = "";
    private String[] ext;

    private DropTarget target;
    private FileDropTargetListener dropTargetListener;

    private JLabel message;

    public BatchRename()
    {
        setLayout(null);
        setUndecorated(true);

        exit = new JButton("X");
        exit.setSize(40, 20);
        exit.setLocation(WIDTH - exit.getWidth() - 4, 2);
        exit.addActionListener(listener);
        exit.setFocusable(false);

        minimize = new JButton("_");
        minimize.setSize(exit.getSize());
        minimize.setLocation(WIDTH - minimize.getWidth() - 8 - minimize.getWidth(), 2);
        minimize.addActionListener(listener);
        minimize.setFocusable(false);

        open = new JButton();
        open.setName("Open");
        open.setSize(28, 20);
        open.setLocation(36, 2);
        open.addActionListener(listener);
        open.setIcon(UIManager.getIcon("Tree.openIcon"));
        open.setFocusable(false);

        apply = new JButton();
        apply.setName("Apply");
        apply.setSize(open.getSize());
        apply.setLocation(4, 2);
        apply.addActionListener(listener);
        apply.setIcon(new ImageIcon(getClass().getResource("saveButton.png")));
        apply.setFocusable(false);

        ignore = new JCheckBox("Ignore file Extension?");
        ignore.setLocation(100, 2);
        ignore.setSize(130, 20);
        ignore.addActionListener(listener);
        ignore.setFocusable(false);

        numeric = new JButton();
        numeric.setName("Numeric");
        numeric.setSize(open.getSize());
        numeric.setLocation(68, 2);
        numeric.addActionListener(listener);
        numeric.setIcon(new ImageIcon(getClass().getResource("numericList.png")));
        numeric.setFocusable(false);

        fileList = new JTextList(null, false);
        fileList.setSize(692, HEIGHT - 28);
        fileList.setLocation(4, 24);
        fileList.getDropTarget().setActive(false);

        message = new JLabel("Drag and Drop a folder here");
        message.setForeground(Color.GRAY);
        message.setFont(new Font(null, Font.PLAIN, 20));

        JScrollPane fileScroll = new JScrollPane(fileList);
        fileScroll.setSize(fileList.getSize());
        fileScroll.setLocation(fileList.getLocation());


        fileList.setTextAlignment(StyleConstants.ALIGN_CENTER);
        fileList.setText("\n\n\n\n\n\n");
        fileList.setCaretPosition(fileList.getText().length());
        fileList.insertComponent(message);

        dropTargetListener = new FileDropTargetListener();
        target = new DropTarget(fileScroll, dropTargetListener);

        add(fileScroll);
        add(open);
        add(exit);
        add(minimize);
        add(apply);
        add(numeric);
        add(ignore);

        requestFocus();
    }

    private ArrayList<String> filesToString(File[] files)
    {
        ArrayList<String> names = new ArrayList<>();
        for (File e : files)
        {
            names.add(e.getName());
        }
        return names;
    }

    private void loadFiles(File[] files)
    {
        this.files = files;
        if (fileNames == null)
            fileNames = new ArrayList<>();
        else
        {
            fileList.getCheckBox(0).removeActionListener(listener);
            fileNames.clear();
        }
        fileNames = filesToString(files);
        fileList.newList(fileNames);
        fileList.getCheckBox(0).addActionListener(listener);
        ignore.setSelected(false);

        ext = new String[files.length];
        for (int i = 0; i < ext.length; i++)
        {
            String temp = fileNames.get(i);
            int index = temp.lastIndexOf(".");
            if (index >= 0)
                ext[i] = temp.substring(index, temp.length());
            else
                ext[i] = "";
        }
    }

    private void processApply(ArrayList<String> names) throws IOException
    {
        for (int i = 0; i < files.length; i++)
        {
            File f = files[i];
            Path oldName = FileSystems.getDefault().getPath(f.getAbsolutePath());
            Files.move(oldName, oldName.resolveSibling("temp" + i));
            oldName = FileSystems.getDefault().getPath(path + "\\" + "temp" + i);
            if (ignore.isSelected())
                Files.move(oldName, oldName.resolveSibling(names.get(i) + ext[i]));
            else
                Files.move(oldName, oldName.resolveSibling(names.get(i)));
        }
        loadFiles(new File(path).listFiles());
        JOptionPane.showMessageDialog(this, "Your files were renamed");
    }

    private void removeExtension()
    {

        for (int i = 0; i < fileNames.size(); i++)
        {
            if (ext[i].equals(""))
                continue;
            String temp = fileList.getText(i);
            if (ignore.isSelected())
                fileList.setText(i, temp.substring(0, temp.lastIndexOf(".")));
            else if (!ignore.isSelected())
                fileList.setText(i, temp + ext[i]);
        }
    }

    class Listener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if (e.getActionCommand().equals("X"))
                System.exit(0);
            if (e.getActionCommand().equals("_"))
                setState(Frame.ICONIFIED);
            if (e.getActionCommand().equals(""))
            {
                String name = ((JButton) e.getSource()).getName();
                if (name.equals("Open"))
                {
                    JFileChooser fileChooser = new JFileChooser(path);
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fileChooser.showOpenDialog(getParent());
                    if (fileChooser.getSelectedFile() == null)
                        return;
                    path = fileChooser.getSelectedFile().getPath();
                    loadFiles(fileChooser.getSelectedFile().listFiles());
                }
                else if (name.equals("Apply"))
                {
                    int n = JOptionPane.showConfirmDialog(getParent(), "Would you like to rename the Files? this cannot be undone.", "Apply file rename", JOptionPane.YES_NO_OPTION);
                    if (n == 0)
                    {
                        try
                        {
                            processApply(fileList.getList());
                        }
                        catch (IOException e1)
                        {
                            showError(e1);
                        }
                    }
                }
                else if (name.equals("Numeric"))
                {
                    int start;
                    int startPos = fileList.getSelectionStart();
                    int endPos = fileList.getSelectionEnd();
                    try
                    {
                        start = Integer.parseInt(fileList.getSelectedText());
                    }
                    catch (NumberFormatException e1)
                    {
                        start = -1;
                    }
                    if (start >= 0)
                    {
                        for (int i = 0; i < fileNames.size(); i++)
                        {
                            fileList.replace(i,""+start+i,startPos,endPos);
                        }
                    }
                    System.out.println(start);
                }
            }
            if (files == null)
                return;
            else if (e.getActionCommand().equals("Ignore file Extension?"))
            {
                removeExtension();
            }
            else if (e.getActionCommand().equals("checkAll"))
            {
                for (int i = 0; i < fileList.length(); i++)
                {
                    fileList.getCheckBox(i + 1).setSelected(fileList.getCheckBox(0).isSelected());
                }
            }
        }
    }

    class FileDropTargetListener implements DropTargetListener
    {
        @Override
        public void dragEnter(DropTargetDragEvent event)
        {
            Transferable transferable = event.getTransferable();
            File files;
            try
            {
                List list = (List) transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
                if (list.size() > 1)
                {
                    event.rejectDrag();
                    return;
                }
                files = new File(list.get(0).toString());
                if (files.isDirectory())
                {
                    event.acceptDrag(DnDConstants.ACTION_COPY);
                }
                else
                    event.rejectDrag();
            }
            catch (UnsupportedFlavorException | IOException e)
            {
                showError(e);
            }


        }

        @Override
        public void dragOver(DropTargetDragEvent event)
        {

        }

        @Override
        public void dropActionChanged(DropTargetDragEvent event)
        {

        }

        @Override
        public void dragExit(DropTargetEvent event)
        {

        }

        @Override
        public void drop(DropTargetDropEvent event)
        {
            fileList.setTextAlignment(StyleConstants.ALIGN_LEFT);
            event.acceptDrop(DnDConstants.ACTION_COPY);
            ;
            Transferable transferable = event.getTransferable();
            try
            {
                List list = (List) transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
                path = list.get(0).toString();
                loadFiles(new File(list.get(0).toString()).listFiles());
            }
            catch (UnsupportedFlavorException | IOException e)
            {
                showError(e);
            }
            event.dropComplete(true);
        }
    }

    public void showError(Exception e)
    {
        JOptionPane.showMessageDialog(getParent(), e.toString());
    }


}

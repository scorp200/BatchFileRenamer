import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
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
class BatchRename extends JFrame
{
    private static final int WIDTH = 700;
    private static final int HEIGHT = 340;

    public static void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        catch (Exception ignored)
        {
        }
        JFrame frame = new BatchRename();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("Batch File Rename");
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private int posX;
    private int posY;
    private JTextList fileList;
    private File[] files;
    private String path = "";
    private JLabel title;

    public BatchRename()
    {
        setLayout(null);
        setUndecorated(true);
        addMouseListener(new MouseListener()
                         {
                             @Override
                             public void mouseClicked(MouseEvent e)
                             {
                             }

                             @Override
                             public void mousePressed(MouseEvent e)
                             {
                                 posX = e.getX();
                                 posY = e.getY();
                             }

                             @Override
                             public void mouseReleased(MouseEvent e)
                             {
                             }

                             @Override
                             public void mouseEntered(MouseEvent e)
                             {
                             }

                             @Override
                             public void mouseExited(MouseEvent e)
                             {
                             }
                         }
        );
        addMouseMotionListener(new MouseMotionListener()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                setLocation(e.getXOnScreen() - posX, e.getYOnScreen() - posY);
            }

            @Override
            public void mouseMoved(MouseEvent e)
            {

            }
        });
        JButton exit = new JButton("X");
        exit.setSize(40, 20);
        exit.setLocation(WIDTH - exit.getWidth() - 4, 2);
        Listener listener = new Listener();
        exit.addActionListener(listener);
        exit.setFocusable(false);

        JButton minimize = new JButton("_");
        minimize.setSize(exit.getSize());
        minimize.setLocation(WIDTH - minimize.getWidth() - 8 - minimize.getWidth(), 2);
        minimize.addActionListener(listener);
        minimize.setFocusable(false);

        JButton open = new JButton();
        open.setName("Open");
        open.setSize(28, 20);
        open.setLocation(36, 2);
        open.addActionListener(listener);
        open.setIcon(UIManager.getIcon("Tree.openIcon"));
        open.setFocusable(false);

        JButton apply = new JButton();
        apply.setName("Apply");
        apply.setSize(open.getSize());
        apply.setLocation(4, 2);
        apply.addActionListener(listener);
        apply.setIcon(new ImageIcon(getClass().getResource("saveButton.png")));
        apply.setFocusable(false);

        JButton numeric = new JButton();
        numeric.setName("Numeric");
        numeric.setSize(open.getSize());
        numeric.setLocation(68, 2);
        numeric.addActionListener(listener);
        numeric.setIcon(new ImageIcon(getClass().getResource("numericList.png")));
        numeric.setFocusable(false);

        title = new JLabel("Open a folder");
        title.setSize(title.getText().length()*6,20);
        title.setLocation(WIDTH / 2 - title.getWidth() / 2, 2);

        fileList = new JTextList(listener);
        fileList.setSize(692, HEIGHT - 28);
        fileList.setLocation(4, 24);
        fileList.getDropTarget().setActive(false);

        JScrollPane fileScroll = new JScrollPane(fileList);
        fileScroll.setSize(fileList.getSize());
        fileScroll.setLocation(fileList.getLocation());


        fileList.setTextAlignment(StyleConstants.ALIGN_CENTER);
        fileList.setText("\n\n\n\n\nDrag and Drop a folder here");
        fileList.setForeground(Color.GRAY);
        fileList.setFont(new Font(null, Font.PLAIN, 20));

        FileDropTargetListener dropTargetListener = new FileDropTargetListener();
        DropTarget target = new DropTarget(fileScroll, dropTargetListener);

        add(fileScroll);
        add(open);
        add(exit);
        add(minimize);
        add(apply);
        add(numeric);
        add(title);

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
        try
        {
            fileList.refreshList(filesToString(files));
        }
        catch (BadLocationException e)
        {
            showError(e);
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
            Files.move(oldName, oldName.resolveSibling(names.get(i)));
        }
        loadFiles(new File(path).listFiles());
        JOptionPane.showMessageDialog(this, "Your files were renamed");
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
                switch (name)
                {
                    case "Open":
                        JFileChooser fileChooser = new JFileChooser(path);
                        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        fileChooser.showOpenDialog(getParent());
                        if (fileChooser.getSelectedFile() == null)
                            return;
                        path = fileChooser.getSelectedFile().getPath();
                        loadFiles(fileChooser.getSelectedFile().listFiles());
                        break;
                    case "Apply":
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
                        break;
                    case "Numeric":
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
                            for (int i = 0; i < fileList.length(); i++)
                            {
                                fileList.replace(i, (((start + i) >= 10) ? "" : "0") + (start + i), startPos, endPos);
                            }
                        }
                        break;
                }
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
            Transferable transferable = event.getTransferable();
            try
            {
                List list = (List) transferable.getTransferData(transferable.getTransferDataFlavors()[0]);

                path = list.get(0).toString();
                title.setText(path);
                title.setSize(title.getText().length()*6,20);
                title.setLocation(WIDTH / 2 - title.getWidth() / 2, 2);
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

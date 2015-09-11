import org.ini4j.Ini;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Created by DogeWolf on 9/8/2015.
 */
public class batchRename extends JFrame
{
    public static void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        catch (Exception e)
        {
        }
        JFrame frame = new batchRename();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        frame.setTitle("Batch File Rename");
        frame.setVisible(true);
        frame.setSize(700, 340);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
    }

    private JButton prefix;
    private JButton suffix;
    private JCheckBox ignore;
    private JCheckBox replaceAll;
    private JButton replace;
    private JButton undo;
    private JButton apply;
    private JButton open;

    private JTextList fileList;

    private File[] files;
    private ArrayList<ArrayList<String>> fileNames;
    private int version = -1;

    private Listener listener = new Listener();
    private String path;
    private String ext;

    public batchRename()
    {
        setLayout(null);

        replace = new JButton("replace");
        replace.setSize(85, 33);
        replace.setLocation(172, 2);
        replace.addActionListener(listener);

        prefix = new JButton("prefix");
        prefix.setSize(replace.getSize());
        prefix.setLocation(2, 2);
        prefix.addActionListener(listener);

        suffix = new JButton("suffix");
        suffix.setSize(replace.getSize());
        suffix.setLocation(87, 2);
        suffix.addActionListener(listener);

        ignore = new JCheckBox("Ignore file Extension?");
        ignore.setLocation(256, 2);
        ignore.setSize(130, 15);
        ignore.addActionListener(listener);

        replaceAll = new JCheckBox("Replace all?");
        replaceAll.setLocation(256, 18);
        replaceAll.setSize(100, 15);

        apply = new JButton("apply");
        apply.setSize(replace.getSize());
        apply.setLocation(608, 2);
        apply.addActionListener(listener);

        undo = new JButton("undo");
        undo.setSize(replace.getSize());
        undo.setLocation(521, 2);
        undo.addActionListener(listener);

        open = new JButton("open");
        open.setSize(replace.getSize());
        open.setLocation(434, 2);
        open.addActionListener(listener);

        add(prefix);
        add(suffix);
        add(ignore);
        add(replaceAll);
        add(replace);
        add(undo);
        add(apply);
        add(open);

        fileList = new JTextList(null, false);
        fileList.setSize(690, 276);
        fileList.setLocation(2, 35);


        JScrollPane fileScroll = new JScrollPane(fileList);
        fileScroll.setSize(fileList.getSize());
        fileScroll.setLocation(fileList.getLocation());
        add(fileScroll);
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
            fileNames.forEach(ArrayList<String>::clear);
            fileNames.clear();
        }
        fileNames.add(filesToString(files));
        fileList.newList(fileNames.get(0));
        fileList.getCheckBox(0).addActionListener(listener);
        ignore.setSelected(false);
        replaceAll.setSelected(false);
        version = 0;
    }

    private ArrayList<String> processReplace(ArrayList<String> names, String replace, String with, int selectionStart, int selectionEnd, boolean replaceAll, boolean ignoreSelection)
    {
        ArrayList<String> newNames = new ArrayList<>();
        String fixed = "";
        String ext = "";
        int start = selectionStart;
        int end = selectionEnd;
        for (String s : names)
        {
            if (fileList.getCheckBox(names.indexOf(s) + 1).isSelected() || ignoreSelection)
            {
                if (selectionEnd == -1)
                    end = s.length();
                if (selectionStart == -1)
                    start = (end - replace.length());

                if (replaceAll)
                    fixed = s.replace(replace, with);
                else
                    fixed = new StringBuilder(s).replace(start, end, with).toString();

                newNames.add(fixed + ext);
            }
            else
                newNames.add(s);
        }
        return newNames;
    }

    private void processApply(ArrayList<String> names) throws IOException
    {
        for (int i = 0; i < files.length; i++)
        {
            File f = files[i];
            Path oldName = FileSystems.getDefault().getPath(f.getAbsolutePath());
            Files.move(oldName, oldName.resolveSibling("temp" + i));
            oldName = FileSystems.getDefault().getPath(path + "\\" + "temp" + i);
            Files.move(oldName, oldName.resolveSibling(names.get(i) + ext));
        }
        loadFiles(new File(path).listFiles());
        JOptionPane.showMessageDialog(this, "Your files were renamed");
    }

    private void removeExtension()
    {
        if (ignore.isSelected())
        {
            String temp = fileNames.get(version).get(0);
            ext = temp.substring(temp.lastIndexOf("."), temp.length());
            fileNames.add(processReplace(fileNames.remove(fileNames.size() - 1), ext, "", -1, -1, false, true));
            version = fileNames.size() - 1;
            fileList.newList(fileNames.get(version));
        }
        else if (!ignore.isSelected())
        {
            String temp = fileNames.get(version).get(0);
            fileNames.add(processReplace(fileNames.remove(fileNames.size() - 1), "", ext, -1, -1, false, true));
            version = fileNames.size() - 1;
            fileList.newList(fileNames.get(version));
            ext = "";
        }
    }

    class Listener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if (e.getActionCommand().equals("open"))
            {
                JFileChooser fileChooser = new JFileChooser("M:\\Documents\\Java\\BatchRename");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.showOpenDialog(getParent());
                if (fileChooser.getSelectedFile() == null)
                    return;
                path = fileChooser.getSelectedFile().getPath();
                loadFiles(fileChooser.getSelectedFile().listFiles());
            }
            if (files == null)
                return;
            if (e.getActionCommand().equals("prefix"))
            {
                if (fileList.getSelectedText() == null)
                    return;
                fileNames.add(processReplace(fileNames.get(version), "", "", 0, fileList.getSelectionEnd(), false, false));
                version = fileNames.size() - 1;
                fileList.newList(fileNames.get(version));
            }
            else if (e.getActionCommand().equals("suffix"))
            {
                if (fileList.getSelectedText() == null)
                    return;
                fileNames.add(processReplace(fileNames.get(version), fileList.getSelectedText(), "", -1, -1, false, false));
                version = fileNames.size() - 1;
                fileList.newList(fileNames.get(version));
            }
            else if (e.getActionCommand().equals("replace"))
            {
                if (fileList.getSelectedText() == null)
                    return;
                int start = fileList.getSelectionStart();
                int end = fileList.getSelectionEnd();
                String selection = fileList.getSelectedText();
                String re = JOptionPane.showInputDialog("Replace |" + selection + "| with:");
                if (re == null)
                    return;
                fileNames.add(processReplace(fileNames.get(version), selection, re, start, end, replaceAll.isSelected(), false));
                version = fileNames.size() - 1;
                fileList.newList((fileNames.get(version)));
            }
            else if (e.getActionCommand().equals("Ignore file Extension?"))
            {
                removeExtension();
            }
            else if (e.getActionCommand().equals("undo"))
            {
                if (version <= 0)
                    return;
                fileNames.remove(version);
                version = fileNames.size() - 1;
                fileList.newList(fileNames.get(version));
            }
            else if (e.getActionCommand().equals("apply"))
            {
                int n = JOptionPane.showConfirmDialog(getParent(), "Would you like to rename the Files? this cannot be undone.", "Apply file rename", JOptionPane.YES_NO_OPTION);
                if (n == 0)
                {
                    try
                    {
                        processApply(fileNames.get(version));
                    }
                    catch (IOException e1)
                    {
                        e1.printStackTrace();
                    }
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
}

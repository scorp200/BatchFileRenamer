import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

/**
 * Created by abca2 on 9/9/2015.
 */
public class JTextList extends JTextPane
{
    private ArrayList<String> list;
    private StyledDocument doc;
    private ArrayList<JCheckBox> check;
    private ArrayList<JTextArea> text;
    private Listener listener;

    public JTextList(ArrayList<String> list, Boolean editable)
    {
        Font font = new Font(Font.SANS_SERIF, 0, 15);
        setFont(font);
        setEditable(editable);
        doc = this.getStyledDocument();
        check = new ArrayList<>();
        text = new ArrayList<>();
        listener = new Listener();
    }

    public int length()
    {
        return list.size();
    }

    public String getElementAt(int index)
    {
        return list.get(index);
    }

    public void add(String element)
    {
        list.add(element);
    }

    public void remove(String element)
    {
        list.remove(element);
    }

    public String get(int index)
    {
        return list.get(index);
    }

    public JCheckBox getCheckBox(int index)
    {
        try
        {
            return check.get(index);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public int getSelectionStart()
    {
        for (JTextArea a : text)
        {
            if (a.getCaret().isSelectionVisible())
                return a.getSelectionStart();
        }
        return Integer.parseInt(null);
    }

    @Override
    public int getSelectionEnd()
    {
        for (JTextArea a : text)
        {
            if (a.getCaret().isSelectionVisible())
                return a.getSelectionEnd();
        }
        return Integer.parseInt(null);
    }

    @Override
    public String getSelectedText()
    {
        for (JTextArea a : text)
        {
            if (a.getCaret().isSelectionVisible())
            {
                return a.getSelectedText();
            }
        }
        return null;
    }

    private void refreshList() throws BadLocationException
    {
        doc.remove(0, doc.getLength());
        if (check.size() - 1 != list.size())
            check.clear();
        if (text.size() != list.size())
        {
            for (JTextArea a : text)
                a.removeFocusListener(listener);
            text.clear();
        }

        for (int i = 0; i < list.size() + 1; i++)
        {
            //Check Boxes
            if (check.size() - 1 != list.size())
            {
                if (i == 0)
                    check.add(new JCheckBox("checkAll"));
                else
                    check.add(new JCheckBox());
                check.get(i).setSelected(true);
                check.get(i).setAlignmentY(0.78f);
            }
            insertComponent(check.get(i));
            //TextAreas
            if (i != 0)
            {
                if (text.size() != list.size())
                {
                    text.add(new JTextArea());
                    text.get(i - 1).setCaret(new CustomCaret());
                    text.get(i - 1).addFocusListener(listener);
                    text.get(i - 1).setAlignmentY(0.78f);
                    text.get(i - 1).setEditable(false);
                    text.get(i - 1).getDropTarget().setActive(false);
                   // text.get(i-1).setForeground(Color.RED);
                }
                insertComponent(text.get(i - 1));
                text.get(i - 1).setText(list.get(i - 1));
            }
            doc.insertString(doc.getLength(), "\n", null);
        }
        setCaretPosition(0);
    }

    public void newList(ArrayList<String> list)
    {
        this.list = list;
        try
        {
            refreshList();
        }
        catch (BadLocationException e)
        {
            JOptionPane.showMessageDialog(getParent(), e.toString());
        }
    }

    public void setTextAlignment(int alignment)
    {
        SimpleAttributeSet set = new SimpleAttributeSet();
        StyleConstants.setAlignment(set, alignment);
        doc.setParagraphAttributes(0, doc.getLength(), set, false);
    }

    class Listener implements FocusListener
    {

        @Override
        public void focusGained(FocusEvent e)
        {
            JTextArea temp = (JTextArea) e.getComponent();
            for (JTextArea a : text)
            {

                if (temp.equals(a))
                    continue;
                CustomCaret caret = (CustomCaret) a.getCaret();
                caret.deselect();

            }
        }

        @Override
        public void focusLost(FocusEvent e)
        {

        }
    }
}

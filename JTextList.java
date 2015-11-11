import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
    private KeyboardListener keyListener;
    private DocumentFilter documentListen;

    public JTextList(ArrayList<String> list, Boolean editable)
    {
        Font font = new Font(Font.SANS_SERIF, 0, 15);
        setFont(font);
        setEditable(editable);
        doc = this.getStyledDocument();
        check = new ArrayList<>();
        text = new ArrayList<>();
        listener = new Listener();
        documentListen = new Documentfilter();
        keyListener = new KeyboardListener();
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
                    text.get(i - 1).getDocument().putProperty("filterNewlines", true);
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

    class Documentfilter extends DocumentFilter
    {
        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException
        {

            String temp;
            for(JTextArea txt : text)
            {
                if(txt.getDocument().equals(fb.getDocument()))
                    continue;
                temp = new StringBuilder(txt.getText()).replace(offset,offset+length,"").toString();
                txt.setText(temp);
            }
            super.remove(fb, offset, length);
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
        {
            String temp;
            for(JTextArea txt : text)
            {
                if(txt.getDocument().equals(fb.getDocument()))
                    continue;
                temp = new StringBuilder(txt.getText()).insert(offset,string).toString();
                txt.setText(temp);
            }
            System.out.println(fb);
            super.insertString(fb, offset, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String with, AttributeSet attrs) throws BadLocationException
        {
            String temp;
            for(JTextArea txt : text)
            {
                if(txt.getDocument().equals(fb.getDocument()))
                    continue;
                temp = new StringBuilder(txt.getText()).replace(offset,offset+length,with).toString();
                txt.setText(temp);
            }
            super.replace(fb, offset, length, with, attrs);
        }
    }

    class KeyboardListener implements KeyListener
    {

        @Override
        public void keyTyped(KeyEvent e)
        {

        }

        @Override
        public void keyPressed(KeyEvent e)
        {

        }

        @Override
        public void keyReleased(KeyEvent e)
        {

        }
    }

    class Listener implements FocusListener
    {

        @Override
        public void focusGained(FocusEvent e)
        {
            JTextArea temp = (JTextArea) e.getComponent();
            temp.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            temp.setEditable(true);
            temp.getCaret().setVisible(true);
            ((AbstractDocument) temp.getDocument()).setDocumentFilter(documentListen);
            for (JTextArea a : text)
            {
                if (temp.equals(a))
                    continue;
                a.setBorder(null);
                a.setEditable(false);
                ((AbstractDocument) a.getDocument()).setDocumentFilter(null);
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

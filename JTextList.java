import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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
    private DocumentFilter documentFilter;
    private ArrayList<ArrayList<String>> version;

    public JTextList(ArrayList<String> list, Boolean editable)
    {
        getActionMap().put("undo", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("test");
            }
        });
        getInputMap().put(KeyStroke.getKeyStroke("control Z"), "undo");
        Font font = new Font(Font.SANS_SERIF, 0, 15);
        setFont(font);
        setEditable(editable);
        doc = this.getStyledDocument();
        check = new ArrayList<>();
        text = new ArrayList<>();
        version = new ArrayList<>();
        listener = new Listener();
        documentFilter = new Documentfilter();
    }

    public int length()
    {
        return list.size();
    }

    public ArrayList<String> getList()
    {
        for (int i = 0; i < list.size(); i++)
        {
            list.set(i, text.get(i).getText());
        }
        return list;

    }

    public void replace(int index, String string, int startPos, int endPos)
    {
        requestFocus();
        text.get(index).setText(new StringBuilder(text.get(index).getText()).replace(startPos,endPos,string).toString());
    }

    public void setText(int index,String string)
    {
        requestFocus();
        text.get(index).setText(string);
    }

    public String getText(int index)
    {
        return text.get(index).getText();
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
            {
                a.removeFocusListener(listener);
                ((AbstractDocument) a.getDocument()).setDocumentFilter(null);
            }
            text.clear();
            version.clear();
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
                check.get(i).setBackground(null);
                check.get(i).setFocusable(false);
            }
            insertComponent(check.get(i));
            //TextAreas
            if (i != 0)
            {
                if (text.size() != list.size())
                {
                    JTextArea temp = new JTextArea();
                    text.add(temp);
                    version.add(new ArrayList<>());
                    temp.setCaret(new CustomCaret());
                    temp.addFocusListener(listener);
                    temp.setAlignmentY(0.78f);
                    temp.setEditable(false);
                    temp.getDropTarget().setActive(false);
                    temp.getDocument().putProperty("filterNewlines", true);
                    temp.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    temp.setBorder(null);
                }
                insertComponent(text.get(i - 1));
                text.get(i - 1).setText(list.get(i - 1));
                version.get(i - 1).clear();
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
            for (int i = 0; i < text.size(); i++)
            {
                if (!check.get(i + 1).isSelected())
                    continue;
                JTextArea txt = text.get(i);
                version.get(i).add(txt.getText());
                if (txt.getDocument().equals(fb.getDocument()))
                    continue;
                temp = new StringBuilder(txt.getText()).replace(offset, offset + length, "").toString();

                txt.setText(temp);
            }
            super.remove(fb, offset, length);
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
        {
            String temp;
            for (int i = 0; i < text.size(); i++)
            {
                if (!check.get(i + 1).isSelected())
                    continue;
                JTextArea txt = text.get(i);
                version.get(i).add(txt.getText());
                if (txt.getDocument().equals(fb.getDocument()))
                    continue;

                temp = new StringBuilder(txt.getText()).insert(offset, string).toString();

                txt.setText(temp);
            }
            System.out.println(fb);
            super.insertString(fb, offset, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String with, AttributeSet attrs) throws BadLocationException
        {
            String temp;
            for (int i = 0; i < text.size(); i++)
            {
                if (!check.get(i + 1).isSelected())
                    continue;
                JTextArea txt = text.get(i);
                version.get(i).add(txt.getText());
                if (txt.getDocument().equals(fb.getDocument()))
                    continue;
                temp = new StringBuilder(txt.getText()).replace(offset, offset + length, with).toString();

                txt.setText(temp);
            }
            super.replace(fb, offset, length, with, attrs);
        }
    }

    class Listener implements FocusListener
    {

        @Override
        public void focusGained(FocusEvent e)
        {
            JTextArea temp = (JTextArea) e.getComponent();
            int index = text.indexOf(temp) + 1;

            temp.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            temp.setAlignmentY(0.78f);
            temp.setEditable(true);
            temp.getCaret().setVisible(true);
            temp.getActionMap().put("undo", new AbstractAction()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    System.out.println("wot");
                    ((AbstractDocument) temp.getDocument()).setDocumentFilter(null);
                    for (int i = 0; i < text.size(); i++)
                    {
                        if (version.get(i).size() > 0)
                        {
                            JTextArea tempText = text.get(i);
                            ArrayList<String> tempVersion = version.get(i);
                            int caretPos = tempText.getCaretPosition() - (tempText.getText().length() - tempVersion.get(tempVersion.size() - 1).length());
                            tempText.setText(tempVersion.remove(tempVersion.size() - 1));
                            if (tempText.hasFocus())
                                tempText.setCaretPosition(caretPos);
                        }
                    }
                    ((AbstractDocument) temp.getDocument()).setDocumentFilter(documentFilter);
                }
            });
            temp.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "undo");

            if (!check.get(0).isSelected() && !check.get(index).isSelected())
            {
                for (int i = 0; i < check.size(); i++)
                {
                    if (index == i)
                        continue;
                    check.get(i).setSelected(false);
                }
            }
            check.get(index).setSelected(true);
            ((AbstractDocument) temp.getDocument()).setDocumentFilter(documentFilter);
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
            JTextArea temp = (JTextArea) e.getComponent();
            temp.setSelectionEnd(0);
            temp.setBorder(null);
            temp.setEditable(false);
            temp.getActionMap().remove(KeyStroke.getKeyStroke("control Z"));
            ((AbstractDocument) temp.getDocument()).setDocumentFilter(null);
        }
    }
}

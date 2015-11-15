import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

/**
 * Created by abca2 on 9/9/2015.
 */
class JTextList extends JTextPane
{
    private StyledDocument doc;
    private ArrayList<JCheckBox> check;
    private ArrayList<JTextArea> nameList;
    private ArrayList<JTextArea> ext;
    private Listener listener;
    private DocumentFilter documentFilter;
    private ArrayList<ArrayList<String>> version;
    private ActionListener actionListener;

    public JTextList(ActionListener actionListener)
    {
        Font font = new Font(Font.SANS_SERIF, 0, 15);
        setFont(font);
        setEditable(false);
        this.actionListener = actionListener;
        doc = this.getStyledDocument();
        check = new ArrayList<>();
        nameList = new ArrayList<>();
        ext = new ArrayList<>();
        version = new ArrayList<>();
        listener = new Listener();
        documentFilter = new CustomFilter();
    }

    public int length()
    {
        return nameList.size();
    }

    public void replace(int index, String string, int startPos, int endPos)
    {
        requestFocus();
        if(Math.abs(startPos-endPos)<2)
            return;
        nameList.get(index).setText(new StringBuilder(nameList.get(index).getText()).replace(startPos, endPos, string).toString());
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
        for (JTextArea a : nameList)
        {
            if (a.getCaret().isSelectionVisible())
                return a.getSelectionStart();
        }
        return -1;
    }

    @Override
    public int getSelectionEnd()
    {
        for (JTextArea a : nameList)
        {
            if (a.getCaret().isSelectionVisible())
                return a.getSelectionEnd();
        }
        return -1;
    }

    @Override
    public String getSelectedText()
    {
        for (JTextArea a : nameList)
        {
            if (a.getCaret().isSelectionVisible())
            {
                return a.getSelectedText();
            }
        }
        return null;
    }

    public ArrayList<String> getList()
    {
        ArrayList<String> modifiedList = new ArrayList<>();
        for (int i = 0; i < nameList.size(); i++)
            modifiedList.add(nameList.get(i).getText() + ext.get(i).getText());
        return modifiedList;
    }

    public void refreshList(ArrayList<String> newList) throws BadLocationException
    {
        //Clear lists if different size==============================================
        doc.remove(0, doc.getLength());
        if (newList.size() != nameList.size())
        {
            if (check.size() > 0)
                check.get(0).removeActionListener(actionListener);
            for (JTextArea a : nameList)
            {
                a.removeFocusListener(listener);
                a.getActionMap().clear();
                ((AbstractDocument) a.getDocument()).setDocumentFilter(null);
            }
            for (JTextArea a : ext)
                a.removeFocusListener(listener);
            check.clear();
            nameList.clear();
            ext.clear();
        }
        //Add check boxes and file names=============================================
        for (int i = 0; i < newList.size() + 1; i++)
        {
            //Check Boxes============================================================
            if (check.size() - 1 != newList.size())
            {
                if (i == 0)
                {
                    check.add(new JCheckBox("checkAll"));
                    check.get(i).addActionListener(actionListener);
                }
                else
                    check.add(new JCheckBox());
                check.get(i).setSelected(true);
                check.get(i).setAlignmentY(0.78f);
                check.get(i).setBackground(null);
                check.get(i).setFocusable(false);
            }
            insertComponent(check.get(i));
            //TextAreas==============================================================
            if (i != 0)
            {


                //File name
                if (nameList.size() != newList.size())
                {
                    JTextArea tempName = new JTextArea();
                    nameList.add(tempName);
                    version.add(new ArrayList<>());
                    tempName.setCaret(new CustomCaret());
                    tempName.addFocusListener(listener);
                    tempName.setAlignmentY(0.78f);
                    tempName.setEditable(false);
                    tempName.getDropTarget().setActive(false);
                    tempName.getDocument().putProperty("filterNewlines", true);
                    tempName.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    tempName.setBorder(null);
                }
                insertComponent(nameList.get(i - 1));
                //Extension
                if (ext.size() != newList.size())
                {
                    JTextArea tempExt = new JTextArea();
                    ext.add(tempExt);
                    tempExt.addFocusListener(listener);
                    tempExt.setAlignmentY(0.78f);
                    tempExt.setEditable(false);
                    tempExt.getDropTarget().setActive(false);
                    tempExt.getDocument().putProperty("filterNewlines", true);
                    tempExt.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    tempExt.setBorder(null);
                }
                insertComponent(ext.get(i - 1));
                String temp = newList.get(i - 1);
                int index = temp.lastIndexOf(".");
                nameList.get(i - 1).setText((index >= 0) ? temp.substring(0, index) : temp);
                ext.get(i - 1).setText((index >= 0) ? temp.substring(index, temp.length()) : "");
                version.get(i - 1).clear();
            }
            doc.insertString(doc.getLength(), "\n", null);
        }
        setCaretPosition(0);
    }

    public void setTextAlignment(int alignment)
    {
        SimpleAttributeSet set = new SimpleAttributeSet();
        StyleConstants.setAlignment(set, alignment);
        doc.setParagraphAttributes(0, doc.getLength(), set, false);
    }

    class CustomFilter extends DocumentFilter
    {
        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException
        {

            String temp;
            for (int i = 0; i < nameList.size(); i++)
            {
                if (!check.get(i + 1).isSelected())
                    continue;
                JTextArea txt = nameList.get(i);
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
            for (int i = 0; i < nameList.size(); i++)
            {
                if (!check.get(i + 1).isSelected())
                    continue;
                JTextArea txt = nameList.get(i);
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
            for (int i = 0; i < nameList.size(); i++)
            {
                if (!check.get(i + 1).isSelected())
                    continue;
                JTextArea txt = nameList.get(i);
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
            int index = nameList.indexOf(temp) + 1;
            temp.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            temp.setEditable(true);
            temp.getCaret().setVisible(true);

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

            if (ext.indexOf(e.getComponent()) > -1)
                return;
            temp.getActionMap().put("undo", new AbstractAction()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ((AbstractDocument) temp.getDocument()).setDocumentFilter(null);
                    for (int i = 0; i < nameList.size(); i++)
                    {
                        if (version.get(i).size() > 0)
                        {
                            JTextArea tempText = nameList.get(i);
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
            ((AbstractDocument) temp.getDocument()).setDocumentFilter(documentFilter);
        }

        @Override
        public void focusLost(FocusEvent e)
        {
            JTextArea temp = (JTextArea) e.getComponent();
            ((CustomCaret) temp.getCaret()).deselect();
            temp.setSelectionEnd(0);
            temp.setBorder(null);
            temp.setEditable(false);
            temp.getActionMap().clear();
            ((AbstractDocument) temp.getDocument()).setDocumentFilter(null);
        }
    }
}

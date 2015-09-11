import javax.swing.text.DefaultCaret;

/**
 * Created by abca2 on 9/11/2015.
 */
public class customCaret extends DefaultCaret
{
    @Override
    public void setSelectionVisible(boolean vis)
    {
        super.setSelectionVisible(true);
    }
    public void deselect(boolean vis)
    {
        super.setSelectionVisible(vis);
    }
}

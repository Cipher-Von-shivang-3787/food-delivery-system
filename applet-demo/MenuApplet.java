import ui.MenuPanel;
import javax.swing.JApplet;

public class MenuApplet extends JApplet {
    @Override
    public void init() {
        add(new MenuPanel());   // same panel a JFrame could host
    }
}
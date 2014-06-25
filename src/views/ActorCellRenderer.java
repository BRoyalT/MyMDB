package views;

import models.Actor;
import java.awt.Color;
import java.awt.Component;
import javax.swing.*;

public class ActorCellRenderer implements ListCellRenderer {
    
    @Override
    public Component getListCellRendererComponent(JList list, Object obj,
            int ind, boolean isSelected, boolean hasFocus) {
        
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        panel.setBackground(Color.white);
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        
        Actor actor = (Actor) obj;
        
        JLabel name = new JLabel(actor.getName());
        
        panel.add(name);
        
        if (isSelected) {
            panel.setBackground(Color.decode("#cc9933"));
            name.setForeground(Color.white);
        }
        
        return panel;
    }
}

package views;

import java.awt.Color;
import java.awt.Component;
import javax.swing.*;

public class MovieCellRenderer implements ListCellRenderer {
    
    public java.util.Set<models.Movie> actorMovies = null;
    
    @Override
    public Component getListCellRendererComponent(JList list, Object obj,
            int ind, boolean isSelected, boolean hasFocus) {
        JLabel label = new JLabel();
        
        models.Movie movie = (models.Movie) obj;
        Color fg = list.getForeground();
        Color bg = list.getBackground();
        if (isSelected) {
            fg = list.getSelectionForeground();
            bg = list.getSelectionBackground();
        }
        label.setText( "(" + movie.getYear() + ")" + "   " + movie.getTitle());
        
        // set style based on containment in actorMovies
        if (actorMovies != null && actorMovies.contains(movie)) {
            fg = Color.red;
        }
        
        // make label height larger
        label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        label.setBackground(bg);
        label.setForeground(fg);
        label.setOpaque(true);
        
        return label;
    }
    
}

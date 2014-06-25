package mymdb;

/**
 * Ricardo Neiderer
 * CSC 417
 * Project 1
 */
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;

import models.*;
import views.*;

public class MyMDB {
    
    private final TheFrame frame = new TheFrame();
    
    // listModel objects
    private final DefaultListModel actorsModel = new DefaultListModel();
    private final DefaultListModel moviesModel = new DefaultListModel();
    private String order = new String("1 order by title");
    
    // renderer objects
    private final MovieCellRenderer movieRenderer = new MovieCellRenderer();
    private final ActorCellRenderer actorRenderer = new ActorCellRenderer();
    
    // dialogs
    private final AddMDialog addMovieDialog = new AddMDialog(frame, true);
    private final AddADialog addActorDialog = new AddADialog(frame, true);
    private final EditDescriptionDialog editDescriptionDialog = new EditDescriptionDialog(frame, true);
    
    // constructor
    public MyMDB() throws Exception {
        ORM.init(DBProps.getProps());
        
        String subproto = ORM.getUrl().split(":")[1];
        frame.setTitle(getClass().getSimpleName() + " - " + subproto);
        frame.setSize(450, 300);
        
        frame.setActorsModel(actorsModel);
        frame.setMoviesModel(moviesModel);
        
        loadListModel(actorsModel, ORM.findAll(Actor.class, "1 order by name"));
        loadListModel(moviesModel, ORM.findAll(Movie.class, getOrder()));
        
        frame.setMovieRenderer(movieRenderer);
        frame.setActorRenderer(actorRenderer);
        
        /**
         * List Selection Listeners=============================================
         */
        frame.addMoviesListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (evt.getValueIsAdjusting()) {
                    return;
                }
                Movie movie = frame.getSelectedMovie();
                if (movie == null) {
                    return;
                }
                try {
                    frame.setInfoText(getMovieInfo(movie));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
            }
        });
        frame.addActorsListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (evt.getValueIsAdjusting()) {
                    return;
                }
                Actor actor = frame.getSelectedActor();
                try {
                    if (actor == null) {
                        movieRenderer.actorMovies = null;
                    } else {
                        movieRenderer.actorMovies = ORM.getJoined(actor, Movie.class);
                    }
                    frame.repaint();
                    frame.setInfoActorText(getActorInfo(actor));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
            }
        });
        
        // Exception
        class MyException extends Exception {
            MyException(String message) { super(message); }
        }
        
        /**
         * Button action listeners==============================================
         */
        frame.addJoinActorActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Movie movie = frame.getSelectedMovie();
                Actor actor = frame.getSelectedActor();
                try {
                    if (movie == null || actor == null) {
                        throw new MyException("select actor and movie");
                    }
                    if (movie.getYear() < actor.getBirthYear()) {
                        throw new MyException("movie was made before actor was born");
                    }
                    if ((movie.getYear() - actor.getBirthYear()) >= 100) {
                        throw new MyException("actor is too old have acted in that movie");
                    }
                    if (!ORM.addJoin(actor, movie)) {
                        throw new MyException("actor is already listed to be in that movie");
                    }
                    ORM.save(movie);
                    frame.setInfoText(getMovieInfo(movie));
                    movieRenderer.actorMovies = ORM.getJoined(actor, Movie.class);
                    frame.repaint();
                }
                catch (MyException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
            }
        });
        
        frame.addUnjoinActorActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Movie movie = frame.getSelectedMovie();
                Actor actor = frame.getSelectedActor();
                try {
                    if (movie == null || actor == null) {
                        throw new MyException("select actor and user");
                    }
                    if (!ORM.removeJoin(actor, movie)) {
                        throw new MyException("actor is not listed to be in that movie");
                    }
                    ORM.save(movie);
                    frame.setInfoText(getMovieInfo(movie));
                    movieRenderer.actorMovies = ORM.getJoined(actor, Movie.class);
                    frame.repaint();
                }
                catch (MyException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
            }
        });
        
        /**
         * Menu options listeners===============================================
         */
        frame.addAddMovieActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                java.awt.Point pos = frame.getLocation();
                addMovieDialog.setLocation(pos.x + 10, pos.y + 10);
                addMovieDialog.setTitleText("");
                addMovieDialog.setYearText("");
                addMovieDialog.setVisible(true);
            }
        });
        
        frame.addAddActorActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                java.awt.Point pos = frame.getLocation();
                addActorDialog.setLocation(pos.x + 10, pos.y + 10);
                addActorDialog.setNameText("");
                addActorDialog.setBirthYearText("");
                addActorDialog.setVisible(true);
            }
        });
        
        frame.addEditDescriptionActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Movie movie = frame.getSelectedMovie();
                if (movie == null) {
                    JOptionPane.showMessageDialog(frame, "select a movie");
                    return;
                }
                java.awt.Point pos = frame.getLocation();
                editDescriptionDialog.setLocation(pos.x + 10, pos.y + 10);
                editDescriptionDialog.setTitleText(movie.getTitle());
                editDescriptionDialog.setDescriptionText("" + movie.getDescription());
                editDescriptionDialog.setVisible(true);
            }
        });
        
        /**
         * Dialog listeners=====================================================
         */
        addMovieDialog.addAddActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String title = addMovieDialog.getTitleText().trim();
                String movieyear = addMovieDialog.getYearText().trim();
                try {
                    if (title.length() < 3) {
                        throw new MyException("title must have length at least 3");
                    }
                    int year = Integer.parseInt(movieyear);
                    if (year < 0) {
                        throw new MyException("year cannot be negative");
                    }
                    if (year < 1900) {
                        throw new MyException("no movies were made at this time period");
                    }
                    if (year > 2014) {
                        throw new MyException("you can't add a movie from the future");
                    }
                    Movie movie = new Movie(title, year, "");
                    ORM.save(movie);
                    loadListModel(moviesModel,ORM.findAll(Movie.class, getOrder()));
                    frame.setInfoText("");
                    addMovieDialog.setVisible(false);
                }
                catch (MyException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "bad year");
                }
                catch (java.sql.SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "duplicate title");
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                    ex.printStackTrace();
                }
                
            }
        });
        
        addActorDialog.addAddActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String name = addActorDialog.getNameText().trim();
                String abirthyear = addActorDialog.getBirthYearText().trim();
                try {
                    if (name.length() < 3) {
                        throw new MyException("name must have length at least 3");
                    }
                    int birthyear = Integer.parseInt(abirthyear);
                    if (birthyear < 1800) {
                        throw new MyException("no actors existed at this time");
                    }
                    if (birthyear > 2014) {
                        throw new MyException("come on now. you don't know anyone from the future");
                    }
                    Actor actor = new Actor(name, birthyear);
                    ORM.save(actor);
                    loadListModel(actorsModel,ORM.findAll(Actor.class, "1 order by name"));
                    frame.setInfoText("");
                    addActorDialog.setVisible(false);
                }
                catch (MyException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "bad age");
                }
                catch (java.sql.SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "duplicate actor");
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
        
        editDescriptionDialog.addChangeActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    String description = editDescriptionDialog.getDescriptionText().trim();
                    Movie movie = frame.getSelectedMovie();
                    movie.setDescription(description);
                    ORM.save(movie);
                    frame.setInfoText(getMovieInfo(movie));
                    editDescriptionDialog.setVisible(false);
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
        
        addMovieDialog.addCancelActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                addMovieDialog.setVisible(false);
            }
        });
        addActorDialog.addCancelActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                addActorDialog.setVisible(false);
            }
        });
        editDescriptionDialog.addCancelActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                editDescriptionDialog.setVisible(false);
            }
        });
        
        addMovieDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addActorDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        addMovieDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                if (confirm("OK to cancel addition?")) {
                    addMovieDialog.setVisible(false);
                }
            }
        });
        addActorDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                if (confirm("OK to cancel addition")) {
                    addActorDialog.setVisible(false);
                }
            }
        });
        
        frame.addRemoveActorActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Actor actor = frame.getSelectedActor();
                try {
                    if (actor == null) {
                        throw new MyException("select an actor");
                    }
                    Set<Movie> movies = ORM.getJoined(actor, Movie.class);
                    for (Movie m: movies) {
                        ORM.removeJoin(actor, m);
                    }
                    if (!confirm("Are you sure?")) {
                        return;
                    }
                    ORM.remove(actor);
                    loadListModel(actorsModel, ORM.findAll(Actor.class));
                } catch (MyException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
        frame.addRemoveMovieActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Movie movie = frame.getSelectedMovie();
                try {
                    if (movie == null) {
                        throw new MyException("select a movie");
                    }
                    Set<Actor> actors = ORM.getJoined(movie, Actor.class);
                    for (Actor a: actors) {
                        ORM.removeJoin(a, movie);
                    }
                    if (!confirm("Are you sure?")) {
                        return;
                    }
                    ORM.remove(movie);
                    loadListModel(moviesModel, ORM.findAll(Movie.class));
                } catch (MyException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
        
    } // end MyMDB constructor
    
    /**
     * Helper functions
     */
    private boolean confirm(Object message) {
                int response = JOptionPane.showOptionDialog(
                frame, message, null, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE, null,
                new String[]{"yes", "no"}, "no");
        return response == JOptionPane.YES_OPTION;
    }
    
    private String getMovieInfo(Movie movie) throws Exception {
        Set<Actor> actors = ORM.getJoined(movie, Actor.class);
        Object[] actornames = actors.toArray();
        for (int i = 0; i < actornames.length; ++i) {
            actornames[i] = ((Actor) actornames[i]).getName();
        }
        String info = "";
        info += movie.getDescription() + "\n";
        info += "ACTORS: " + Arrays.toString(actornames) + "\n";
        return info;
    }
    
    private String getActorInfo(Actor actor) throws Exception {
        String info = "";
        info += "NAME: " + actor.getName() + "\n";
        info += "BIRTHYEAR: " + actor.getBirthYear() + "\n";
        return info;
    }
    
    private String getOrder() {
        return order;
    }
    private void setOrder(String theorder) {
        order = theorder;
    }
    
    // loadListModel helper function
    private <E> void loadListModel(
    DefaultListModel model, Collection<E> coll) {
        model.clear();
        for (E elt: coll) {
            model.addElement(elt);
        }
    }

    
    @SuppressWarnings("CallToThreadDumpStack")
    public static void main(String[] args) {
        try {
            MyMDB app = new MyMDB();
            app.frame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }
    
}

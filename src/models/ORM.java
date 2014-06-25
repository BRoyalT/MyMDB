package models;

import java.sql.*;
import java.util.*;

public class ORM {

  private static java.sql.Connection cx; // single connection

  private static String url;
  private static String username;
  private static String password;

  public static String getUrl() {
    return url;
  }

  public static void init(Properties props) throws Exception {
    url = props.getProperty("url");
    username = props.getProperty("username");
    password = props.getProperty("password");
    String driver = props.getProperty("driver");
    if (driver != null) {
      Class.forName(driver); // load driver if necessary
    }
  }

  static Connection connection() throws Exception {
    if (url == null) {
      throw new Exception("ORM not initialized");
    }
    if (cx == null || cx.isClosed()) {
      System.out.println("new connection");
      cx = DriverManager.getConnection(url, username, password);
    }
    return cx;
  }

  public static int save(Model m) throws Exception {
    if (m.getId() == 0) {
      m.insert();
    } else {
      m.update();
    }
    return m.getId();
  }

  public static Model load(Class C, int id) throws Exception {
    String table = (String) C.getField("table").get(null);

    cx = connection();
    String sql = String.format("select * from %s where id=?", table);
    //System.out.println(sql);
    PreparedStatement st = cx.prepareStatement(sql);
    st.setInt(1, id);
    ResultSet rs = st.executeQuery();
    if (!rs.next()) {
      return null;
    }
    Model m = (Model) C.newInstance();
    m.load(rs);
    return m;
  }

  public static boolean remove(Model m) throws Exception {
    if (m.getId() == 0) {
      return false;
    }
    String table = (String) m.getClass().getField("table").get(null);

    cx = connection();
    String sql = String.format("delete from %s where id=?", table);
    //System.out.println(sql);
    PreparedStatement st = cx.prepareStatement(sql);
    st.setInt(1, m.getId());
    int affected_rows = st.executeUpdate();
    return affected_rows > 0;
  }

  public static Model findOne(Class C, String extra, List values) throws Exception {
    if (extra == null) {
      extra = "";
    } else {
      extra = extra.trim();
      if (!(extra.isEmpty())) {
        extra = "where " + extra;
      }
    }
    String table = (String) C.getField("table").get(null);

    cx = connection();
    String sql = String.format("select * from %s %s", table, extra);
    //System.out.println(sql);
    PreparedStatement st = cx.prepareStatement(sql);
    if (values != null) {
      int pos = 1;
      for (Object value : values) {
        st.setObject(pos++, value);
      }
    }
    ResultSet rs = st.executeQuery();
    if (!rs.next()) {
      return null;
    }
    Model m = (Model) C.newInstance();
    m.load(rs);
    return m;
  }

  public static Set<Model> findAll(Class C, String extra, List inserts) throws Exception {
    String table = (String) C.getField("table").get(null);

    if (extra == null) {
      extra = "";
    } else {
      extra = extra.trim();
      if (!(extra.isEmpty())) {
        extra = "where " + extra;
      }
    }
    cx = connection();
    String sql = String.format("select * from %s %s", table, extra);
    //System.out.println(sql);
    PreparedStatement st = cx.prepareStatement(sql);

    if (inserts != null && !inserts.isEmpty()) {
      int pos = 1;
      for (Object value : inserts) {
        st.setObject(pos++, value);
      }
    }

    ResultSet rs = st.executeQuery();
    Set<Model> L = new LinkedHashSet<>();
    while (rs.next()) {
      Model m = (Model) C.newInstance();
      m.load(rs);
      L.add(m);
    }
    return L;
  }

  public static Set<Model> findAll(Class C, String extra) throws Exception {
    return findAll(C, extra, null);
  }

  public static Set<Model> findAll(Class C) throws Exception {
    return findAll(C, null, null);
  }

  static int getMaxId(String table) throws Exception {
    String sql = String.format("select max(id) from %s", table);
    cx = connection();
    Statement st = cx.createStatement();
    ResultSet rs = st.executeQuery(sql);
    rs.next();
    return rs.getInt(1);
  }
  
  public static Set getJoined(Model from, Class toClass) throws Exception {    
    String from_table 
            = (String) from.getClass().getField("table").get(null);

    String to_table = (String) toClass.getField("table").get(null);

    String sql = String.format("select %s.*\n"
            + "from movie join actor_movie join actor\n"
            + "where movie.id = actor_movie.movie_id\n"
            + "and actor.id = actor_movie.actor_id\n"
            + "and %s.id = ?", to_table, from_table
    );
    //System.out.println(sql);
    PreparedStatement st = cx.prepareStatement(sql);
    st.setInt(1, from.getId());

    ResultSet rs = st.executeQuery();
    Set<Model> L = new LinkedHashSet<>();
    while (rs.next()) {
      Model to = (Model) toClass.newInstance();
      to.load(rs);
      L.add(to);
    }
    return L;
  }
  
  public static boolean addJoin(Actor actor, Movie movie) throws Exception {
    cx = connection();
    String sql = "select * from actor_movie where movie_id=? and actor_id=?";
    //System.out.println(sql);
    PreparedStatement st = cx.prepareStatement(sql);
    st.setInt(1, movie.getId());
    st.setInt(2, actor.getId());
    ResultSet rs = st.executeQuery();
    if (rs.next()) {  // join entry already exists
      return false;
    }

    // add it to the table
    sql = "insert into actor_movie (movie_id,actor_id) values(?,?)";
    //System.out.println(sql);
    st = cx.prepareStatement(sql);
    st.setInt(1, movie.getId());
    st.setInt(2, actor.getId());
    st.executeUpdate();
    return true;
  }
  
  /** removeJoin: 
   *  attempt to remove joined record
   */
  public static boolean removeJoin(Actor actor, Movie movie) throws Exception {
    cx = connection();
    String sql = "delete from actor_movie where movie_id=? and actor_id=?";
    //System.out.println(sql);
    PreparedStatement st = cx.prepareStatement(sql);
    st.setInt(1, movie.getId());
    st.setInt(2, actor.getId());
    int affected_rows = st.executeUpdate(); // = 1 if successful delete
    return affected_rows > 0;               // = 0 if not
  }
}

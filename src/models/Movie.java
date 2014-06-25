
package models;

import java.sql.*;

public final class Movie extends Model {
    
  public static final String table = "movie";
    
  private int id = 0;
  private String title;
  private int year;
  private String description;

  // must have an empty constructor defined
  public Movie() { }

  public Movie(String title, int year, String description) {
    this.title = title;
    this.year = year;
    this.description = description;
  }

  @Override
  public int getId() { return id;}
  public String getTitle() { return title; }
  public int getYear() { return year; }
  public String getDescription() { return description; }

  public void setTitle(String title) { this.title = title; }
  public void setYear(int year) { this.year = year; }
  public void setDescription(String description) { this.description = description; }

  @Override
  void load(ResultSet rs) throws Exception {
    id = rs.getInt("id");
    title = rs.getString("title");
    year = rs.getInt("year");
    description = rs.getString("description");
  }
  
  @Override
  void insert() throws Exception {
    Connection cx = ORM.connection();
    String sql = String.format(
      "insert into %s (title,year,description) values (?,?,?)", table);
    PreparedStatement st = cx.prepareStatement(sql);
    int i = 0;
    st.setString(++i, title);
    st.setInt(++i, year);
    st.setString(++i, description);
    st.executeUpdate();
    id = ORM.getMaxId(table);
  }

  @Override
  void update() throws Exception {
    Connection cx = ORM.connection();
    String sql = String.format(
      "update %s set title=?,year=?,description=? where id=?", table);
    PreparedStatement st = cx.prepareStatement(sql);
    int i = 0;
    st.setString(++i, title);
    st.setInt(++i, year);
    st.setString(++i, description);
    st.setInt(++i, id);
    st.executeUpdate();
  }
    
}

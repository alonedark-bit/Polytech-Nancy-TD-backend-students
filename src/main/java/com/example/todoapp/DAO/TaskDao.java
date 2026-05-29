package com.example.todoapp.DAO;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.example.todoapp.Task;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * Data Access Object for {@link Task} model.
 */
public class TaskDao {

    private Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:todo.db"; 
        return DriverManager.getConnection(url);
    }

    public TaskDao() {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS taks(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, description TEXT, done BOOLEAN);");
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    {
        save(new Task(1, "Réviser DS de maths", "Séries numériques et probabilités.", false));
        save(new Task(2, "Valider mon PIVE", "PIVE Club Poker.", true));
        save(new Task(3, "Choisir mon parcours de 4A", "SIR ou SIA ?", false));
    }

    /**
     * Persist {@link Task} model.
     * @param task task to save.
     * @return task model.
     */
    public Task save(Task task) {
        try {
            Connection conn = getConnection();
            PreparedStatement pstmt =conn.prepareStatement("INSERT INTO tasks (title, description, done) VALUES (?,?,?)");
            pstmt.setString((1), task.title());
            pstmt.setString((2), task.description());
            pstmt.setBoolean((3), task.done());
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return task;
    }

    /**
     * Retrieve {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     * @return {@link Task} model wrapped by Optional.
     */
    public Optional<Task> findById(int id) {
        try {
            Connection conn = getConnection();
            PreparedStatement pstmt=conn.prepareStatement("SELECT * FROM tasks WHERE id=?");
            pstmt.setInt(1,id);
            ResultSet rs= pstmt.executeQuery();

            if (rs.next()){
                Task task = new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getBoolean("done"));
            pstmt.close();
            conn.close();
            return Optional.of(task);
            }
            pstmt.close();
            conn.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Task> findAll(boolean todoOnly) {
        List<Task> result = new ArrayList<>();
        String query = todoOnly ? "SELECT * FROM tasks WHERE id=0" : "SELECT * FROM tasks";

        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Task task = new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getBoolean("done"));
                result.add(task);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean update(int id, Task updatedTask) {
        try {
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement("UPDATE tasks SET title = ?, description = ? , done = ? WHERE id = ?");
            pstmt.setString(1, updatedTask.title());
            pstmt.setString(2, updatedTask.description());
            pstmt.setBoolean(3, updatedTask.done());
            pstmt.setInt(4, id);

            int rows = pstmt.executeUpdate();
            pstmt.close();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteById(int id) {
        try {
            Connection conn = getConnection();
            PreparedStatement pstmt =conn.prepareStatement("DELETE FROM tasks WHERE id=?");
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            pstmt.close();
            conn.close();
            return rows > 0; 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

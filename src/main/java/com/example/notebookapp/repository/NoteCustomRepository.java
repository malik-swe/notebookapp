package com.example.notebookapp.repository;

import com.example.notebookapp.model.Note;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class NoteCustomRepository {

    private final JdbcTemplate jdbcTemplate;

    public NoteCustomRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Example of using prepared statement with JdbcTemplate
     * This prevents SQL injection by using parameterized queries
     */
    public List<Note> findNotesByUserIdAndTitleContaining(Long userId, String keyword) {
        String sql = "SELECT * FROM notes WHERE user_id = ? AND title ILIKE ? ORDER BY id DESC";

        // JdbcTemplate automatically uses prepared statements with ? placeholders
        return jdbcTemplate.query(
                sql,
                new Object[]{userId, "%" + keyword + "%"},
                new NoteRowMapper()
        );
    }

    private static class NoteRowMapper implements RowMapper<Note> {
        @Override
        public Note mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Note(
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getLong("user_id")
            );
        }
    }
}
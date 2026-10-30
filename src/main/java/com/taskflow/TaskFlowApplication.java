package com.taskflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import com.taskflow.model.TaskCategory;
import com.taskflow.model.DashboardProfile;
import com.taskflow.repository.TaskCategoryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

@SpringBootApplication
public class TaskFlowApplication {
    @Bean
    CommandLineRunner seedCategories(TaskCategoryRepository categories, JdbcTemplate jdbc) {
        return args -> {
            if (categories.count() == 0) {
                categories.saveAll(List.of(
                        new TaskCategory("Study", "Assignment"), new TaskCategory("Study", "Exam preparation"), new TaskCategory("Study", "Project"),
                        new TaskCategory("Work", "Meeting"), new TaskCategory("Work", "Follow-up"), new TaskCategory("Work", "Deliverable"),
                        new TaskCategory("Business", "Operations"), new TaskCategory("Business", "Finance"), new TaskCategory("Business", "Client"),
                        new TaskCategory("Teaching", "Lesson planning"), new TaskCategory("Teaching", "Grading"), new TaskCategory("Teaching", "Communication"),
                        new TaskCategory("Personal", "Errand"), new TaskCategory("Personal", "Health"), new TaskCategory("Personal", "Home")
                ));
            }
            migrateLegacyTasks(jdbc);
        };
    }

    private void migrateLegacyTasks(JdbcTemplate jdbc) throws Exception {
        Set<String> tables = jdbc.execute((ConnectionCallback<Set<String>>) connection -> {
            Set<String> found = new HashSet<>();
            try (var result = connection.getMetaData().getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (result.next()) found.add(result.getString("TABLE_NAME").toLowerCase());
            }
            return found;
        });
        if (!tables.contains("tasks") || tables.contains("tasks_legacy")) return;

        Set<String> columns = jdbc.execute((ConnectionCallback<Set<String>>) connection -> {
            Set<String> found = new HashSet<>();
            try (var result = connection.getMetaData().getColumns(connection.getCatalog(), null, "tasks", "%")) {
                while (result.next()) found.add(result.getString("COLUMN_NAME").toLowerCase());
            }
            return found;
        });
        String category = columns.contains("category_id") ? "category_id" : "NULL AS category_id";
        String profile = columns.contains("dashboard_profile") ? "dashboard_profile" : "'PERSONAL' AS dashboard_profile";
        var rows = jdbc.queryForList("SELECT id,title,description,priority,status,deadline,created_at,updated_at,"
                + category + "," + profile + " FROM tasks");
        for (Map<String, Object> row : rows) {
            String value = String.valueOf(row.get("dashboard_profile"));
            DashboardProfile dashboardProfile;
            try { dashboardProfile = DashboardProfile.valueOf(value); }
            catch (RuntimeException ignored) { dashboardProfile = DashboardProfile.PERSONAL; }
            String target = switch (dashboardProfile) {
                case PERSONAL -> "tasks_personal"; case STUDENT -> "tasks_student";
                case OFFICE_WORKER -> "tasks_office_worker"; case FREELANCER -> "tasks_freelancer";
                case BUSINESS_OWNER -> "tasks_business_owner"; case TEACHER -> "tasks_teacher";
            };
            jdbc.update("INSERT IGNORE INTO " + target + " (id,title,description,priority,status,deadline,created_at,updated_at,category_id) VALUES (?,?,?,?,?,?,?,?,?)",
                    row.get("id"), row.get("title"), row.get("description"), row.get("priority"), row.get("status"),
                    row.get("deadline"), row.get("created_at"), row.get("updated_at"), row.get("category_id"));
        }
        jdbc.execute("RENAME TABLE tasks TO tasks_legacy");
    }

    public static void main(String[] args) {
        SpringApplication.run(TaskFlowApplication.class, args);
    }
}

package pink.zak.giveawaybot.service.storage.backends.mysql;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.SneakyThrows;
import pink.zak.giveawaybot.service.bot.SimpleBot;
import pink.zak.giveawaybot.service.storage.Backend;
import pink.zak.giveawaybot.service.storage.settings.StorageSettings;

import javax.naming.OperationNotSupportedException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

public class MySqlBackend implements Backend {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `%where%` ( id VARCHAR(36) NOT NULL, json MEDIUMBLOB NOT NULL, PRIMARY KEY (id) )"; // Reminder that if I ever use this these statements should be moved.
    private static final String DELETE = "DELETE FROM `%where%` WHERE id=?";
    private static final String INSERT = "INSERT INTO `%where%` (id, json) VALUES(?, ?)";
    private static final String SELECT = "SELECT id, json FROM `%where%` WHERE id=?";
    private static final String SELECT_ALL = "SELECT * FROM `%where%`";
    private final StorageSettings storageSettings; // getters can return null values
    private final MySqlConnectionFactory connectionFactory;
    private final UnaryOperator<String> processor;

    public MySqlBackend(SimpleBot bot, String tableName) {
        this.storageSettings = bot.getStorageSettings();
        this.connectionFactory = new MySqlConnectionFactory(this.storageSettings);
        this.processor = query -> query.replace("%where%", this.storageSettings.getPrefix().concat(tableName));
        this.createTable();
    }

    @Override
    @SneakyThrows
    public JsonObject load(String id) {
        try (Connection connection = this.connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(this.processor.apply(SELECT))) {
                statement.setString(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return JsonParser.parseString(resultSet.getString("json")).getAsJsonObject();
                    }
                }
            }
        }
        return null;
    }

    @SneakyThrows
    @Override
    public JsonObject load(Map<String, String> valuePairs) {
        throw new OperationNotSupportedException("Method not available with mysql storage.");
    }

    @Override
    @SneakyThrows
    public void save(String id, JsonObject json) {
        try (Connection connection = this.connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(this.processor.apply(DELETE))) {
                statement.setString(1, id);
                statement.execute();
            }
        }
        try (Connection connection = this.connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(this.processor.apply(INSERT))) {
                statement.setString(1, id);
                statement.setString(2, new Gson().toJson(json));
                statement.execute();
            }
        }
    }

    @SneakyThrows
    @Override
    public void save(Map<String, String> valuePairs, JsonObject json) {
        throw new OperationNotSupportedException("Method not available with mysql storage.");
    }

    @Override
    @SneakyThrows
    public Set<JsonObject> loadAll() {
        Set<JsonObject> all = Sets.newHashSet();
        try (Connection connection = this.connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(this.processor.apply(SELECT_ALL))) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        all.add(JsonParser.parseString(resultSet.getString("json")).getAsJsonObject());
                    }
                }
            }
        }
        return all;
    }

    @Override
    @SneakyThrows
    public void delete(String id) {
        try (Connection connection = this.connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(this.processor.apply(DELETE))) {
                statement.setString(1, id);
                statement.execute();
            }
        }
    }

    @Override
    public void close() {
        this.connectionFactory.close();
    }

    @SneakyThrows
    private void createTable() {
        try (Connection connection = this.connectionFactory.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(this.processor.apply(CREATE_TABLE));
            }
        }
    }
}

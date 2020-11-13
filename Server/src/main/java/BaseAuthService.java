import java.sql.*;

public class BaseAuthService {
    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement preparedStatement;
    private static ResultSet resultSet;


    public void start() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:C:/sqlite/users.db");
            statement = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Сервис аутентификации запущен");
    }


    public String getAccessByLoginPass(String login, String pass) {
        try {
            System.out.println("login = " + login + " pass = " + pass);
            preparedStatement = connection.prepareStatement("SELECT login FROM users WHERE login LIKE ? AND password LIKE ?");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, pass);

            resultSet = preparedStatement.executeQuery();
            boolean hasNext = resultSet.next();
            if (!hasNext) {
                System.out.println("RS is empty");
                return null;
            }
            return resultSet.getString(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void stop() {
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Сервис аутентификации остановлен");
    }
}

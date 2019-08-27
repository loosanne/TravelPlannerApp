package com.github.travelplannerapp.ServerApp.db.repositories

import com.github.travelplannerapp.ServerApp.db.DbConnection
import com.github.travelplannerapp.ServerApp.db.dao.User
import org.springframework.stereotype.Component
import java.sql.PreparedStatement
import java.sql.ResultSet

@Component
class UserRepository : Repository<User>(), IUserRepository {
    companion object {
        const val tableName = "app_user"
        const val columnId = "id"
        const val columnEmail = "email"
        const val columnPassword = "password"
        const val columnAuthToken = "authtoken"
        const val columnExpirationDate = "expirationdate"
    }

    override val selectStatement = "SELECT * FROM $tableName "
    override val insertStatement = "INSERT INTO $tableName " +
            "($columnId,$columnEmail,$columnPassword,$columnAuthToken,$columnExpirationDate) " +
            "VALUES (?,?,?,?,?) "
    override val deleteStatement = "DELETE FROM $tableName "
    override val updateStatement = "UPDATE $tableName " +
            "SET $columnEmail=?, $columnPassword=?, $columnAuthToken=?, $columnExpirationDate=?" +
            " WHERE $columnId=?"
    override val nextIdStatement = "SELECT nextval(pg_get_serial_sequence('$tableName', '$columnId')) AS new_id"

    override fun getUserByEmail(email: String): User? {
        val statement = DbConnection
                .conn
                .prepareStatement(selectStatement + "WHERE $columnEmail=?")
        statement.setString(1, email)
        val result: ResultSet = statement.executeQuery()
        if (result.next()) {
            return User(result)
        }
        return null
    }

    override fun T(result: ResultSet): User? {
        return User(result)
    }

    override fun prepareInsertStatement(obj: User): PreparedStatement {
        val statement = DbConnection
                .conn
                .prepareStatement(insertStatement)
        statement.setInt(1,obj.id!!)
        statement.setString(2, obj.email)
        statement.setString(3, obj.password)
        statement.setString(4, obj.token)
        statement.setTimestamp(5, obj.expirationDate)
        return statement
    }

    override fun prepareUpdateStatement(obj: User): PreparedStatement {
        val statement = DbConnection
                .conn
                .prepareStatement(updateStatement)
        statement.setString(1, obj.email)
        statement.setString(2, obj.password)
        statement.setString(3, obj.token)
        statement.setTimestamp(4, obj.expirationDate)
        statement.setInt(5,obj.id!!)
        return statement
    }
}
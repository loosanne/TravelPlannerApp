package com.github.travelplannerapp.ServerApp.db.repositories

import com.github.travelplannerapp.ServerApp.db.DbConnection
import com.github.travelplannerapp.ServerApp.db.dao.UserTravel
import org.springframework.stereotype.Component
import java.sql.PreparedStatement
import java.sql.ResultSet

@Component
class UserTravelRepository : Repository<UserTravel>(), IUserTravelRepository {
    companion object {
        const val tableName = "app_user_travel"
        const val columnId = "id"
        const val columnUserId = "app_user_id"
        const val columnTravelId = "travel_id"
    }

    override val selectStatement = "SELECT * FROM $tableName "
    override val insertStatement = "INSERT INTO $tableName ($columnId, $columnUserId, $columnTravelId) " +
            "VALUES (?, ?, ?) "
    override val deleteStatement = "DELETE FROM $tableName "
    override val updateStatement = "UPDATE $tableName SET $columnUserId=?, $columnTravelId=?  WHERE $columnId=?"
    override val nextIdStatement = "SELECT nextval(pg_get_serial_sequence('$tableName', '$columnId')) AS new_id"

    override fun T(result: ResultSet): UserTravel? {
        return UserTravel(result)
    }

    override fun prepareInsertStatement(obj: UserTravel): PreparedStatement {
        val statement = DbConnection
                .conn
                .prepareStatement(insertStatement)
        statement.setInt(1, obj.id!!)
        statement.setInt(2, obj.userId!!)
        statement.setInt(3, obj.travelId!!)
        return statement
    }

    override fun prepareUpdateStatement(obj: UserTravel): PreparedStatement {
        val statement = DbConnection
                .conn
                .prepareStatement(updateStatement)
        statement.setInt(1, obj.userId!!)
        statement.setInt(2, obj.travelId!!)
        statement.setInt(3, obj.id!!)
        return statement
    }
}
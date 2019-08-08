package com.github.travelplannerapp.ServerApp.db.dao

import java.sql.ResultSet

class Travel (var name: String,
              var id: Int = -1) {
    constructor(result: ResultSet) :
            this(
                result.getString(2),
                result.getInt(1)
            )
}
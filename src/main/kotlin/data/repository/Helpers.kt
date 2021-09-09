package data.repository

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

fun ResultSet.getIntOrNull(columnLabel: String): Int? {
    val i = getInt(columnLabel)
    return if (wasNull()) {
        null
    } else {
        i
    }
}

fun ResultSet.getStringOrNull(columnLabel: String): String? {
    val s = getString(columnLabel)
    return if (wasNull()) {
        null
    } else {
        s
    }
}

fun PreparedStatement.setIntOrNull(columnIndex: Int, i: Int?) {
    if (i != null)
        setInt(columnIndex, i)
    else
        setNull(columnIndex, Types.INTEGER)
}

fun PreparedStatement.setStringOrNull(columnIndex: Int, s: String?) {
    if (s != null)
        setString(columnIndex, s)
    else
        setNull(columnIndex, Types.VARCHAR)
}

fun <T> ResultSet.iterator(block: (ResultSet) -> T): Iterator<T> {
    val rs = this
    return object : Iterator<T> {
        private var didNext = false
        private var hasNext = false

        override fun hasNext(): Boolean {
            if (!didNext) {
                hasNext = rs.next()
                didNext = true
            }

            return hasNext
        }

        override fun next(): T {
            if (!didNext) {
                rs.next()
            }
            didNext = false
            return block(rs)
        }
    }
}
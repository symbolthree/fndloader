/******************************************************************************
 *
 * ≡≡ FNDLOADER ≡≡
 * Copyright (C) 2009-2016 Christopher Ho
 * All Rights Reserved, symbolthree.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * E-mail: christopher.ho@symbolthree.com
 *
 * ================================================
 *
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/DBConnection.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/


package symbolthree.oracle.fndload;

//~--- non-JDK imports --------------------------------------------------------

import oracle.jdbc.*;

//~--- JDK imports ------------------------------------------------------------

import java.sql.*;

public class DBConnection {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/DBConnection.java 1     11/06/16 1:12a Christopher Ho $";
    private static DBConnection instance = null;
    private String              jdbcURL  = null;
    private Connection          connection;

    protected DBConnection(String url, String username, String password) throws SQLException {
        DriverManager.registerDriver(new OracleDriver());
        connection = DriverManager.getConnection(url, username, password);
        jdbcURL    = url;
        setSessionLang("AMERICAN");
    }

    public static DBConnection getInstance(String url, String username, String password) throws SQLException {
        if (instance == null) {
            instance = new DBConnection(url, username, password);
        }

        return instance;
    }

    public static DBConnection getInstance() throws FNDLOADERException {
        if (instance == null) {
            throw new FNDLOADERException("DBConnection is not instantiated.");
        }

        return instance;
    }

    public String getJDBCUrl() {
        return jdbcURL;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setSessionLang(String language) throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("alter session set NLS_LANGUAGE='" + language + "'");
    }

    public void disconnect() throws SQLException {
        connection.close();
        instance = null;
    }
}

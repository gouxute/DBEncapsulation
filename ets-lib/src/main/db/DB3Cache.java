package com.etrans.lib.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.etrans.lib.utils.Coder;

/**
 * Created by maotz on 2015-04-10.
 * Set up database in cache;
*/
public class DB3Cache implements IDB3Cache{
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DB3Cache.class);

    private Connection connection;
    private Statement exeStmt;
    private Statement qryStmt;
    private DBConfig config;
    private boolean isMemDB;
    private final static String MEMORY = ":memory:";

    public DB3Cache(){
    }

    public void setup() throws Exception{
        setup(MEMORY);
    }

    public void openTrans() throws SQLException {
        connection.setAutoCommit(false);
    }
    public void closeTrans() throws SQLException {
        connection.setAutoCommit(true);
    }

    public void setup(String _file) throws Exception{
        if(null == _file || _file.isEmpty()){
            logger.error("DB3Cache setup _file is null");
            return;
        }
//        logger.info("setup({})", _file);

        if(null==connection) {
            config = new DBConfig(DBDriver.SQLITE);
            config.name = _file;
            connection = DBHelper.newConnection(config);
            assert connection != null;
            qryStmt = connection.createStatement();
            exeStmt = connection.createStatement();
            isMemDB = MEMORY.equalsIgnoreCase(_file);
        }
    }

    private long curTicks;
    private void TT(String _msg){
        if(null==_msg)
            curTicks = System.currentTimeMillis();
        else{
            long mills = System.currentTimeMillis() - curTicks;
            if(mills > 500)
                logger.debug("total mills {} in {}", mills, _msg);
        }
    }

    private final static String C_DROP_TABLE = "drop table %s";
    public void delTable(String _table) throws Exception{
        logger.debug("dropTable({})", _table);
        exeStmt.execute( String.format(C_DROP_TABLE, _table) );
    }

    public void newTable(String _table, ResultSet _rs) throws Exception{
        ResultSetMetaData meta = _rs.getMetaData();
        int cols = meta.getColumnCount();

        StringBuilder sb = new StringBuilder();
        sb.append("create table ").append(_table).append("(");
        for(int i=1; i<=cols; i++){
            String label = meta.getColumnLabel(i);
            int type = meta.getColumnType(i);
            String stype = meta.getColumnTypeName(i);
            sb.append(label);
            switch (type){
                case Types.BOOLEAN: sb.append(" bit");break;
                default: sb.append(" ").append(stype);
            }
            if(i<cols)
                sb.append(",");
        }
        sb.append(")");

        logger.debug("createTable({})", sb);
        exeStmt.execute(sb.toString());
    }

    public int put(String _table, ResultSet _rs) throws Exception{
        logger.debug("put({},{})", _table, _rs);
        if(hasTable(_table))
            delTable(_table);
        newTable(_table, _rs);
        return insData(_table, _rs, false);
    }

    static void row2Line(StringBuilder _sb, ResultSetMetaData _meta, ResultSet _rs, int _cols) throws Exception{
        for(int i=1; i<=_cols; i++) {
            switch (_meta.getColumnType(i)){
                case Types.BIGINT   : {
                    int res = _rs.getInt(i);
                    if(_rs.wasNull())
                        _sb.append("null");
                    else
                        _sb.append(res);
                }break;
                case Types.INTEGER  : {
                    int res = _rs.getInt(i);
                    if(_rs.wasNull())
                        _sb.append("null");
                    else
                        _sb.append(res);
                }break;
                case Types.BIT      : {
                    int res = _rs.getInt(i);
                    if(_rs.wasNull())
                        _sb.append("null");
                    else
                        _sb.append(res);
                }break;
                case Types.DATE     : {
                    Date res = _rs.getDate(i);
                    if (_rs.wasNull())
                        _sb.append("null");
                    else
                        _sb.append("'").append(Coder.DateTimeToStr(res)).append("'");
                }break;
                case Types.TIME     :{
                    Time res = _rs.getTime(i);
                    if (_rs.wasNull())
                        _sb.append("null");
                    else
                        _sb.append("'").append(Coder.DateTimeToStr(res)).append("'");
                }break;
                case Types.TIMESTAMP     :{
                    Timestamp res = _rs.getTimestamp(i);
                    if (_rs.wasNull())
                        _sb.append("null");
                    else
                        _sb.append("'").append(Coder.DateTimeToStr(res)).append("'");
                }break;
                case Types.VARCHAR  :{
                    String res = _rs.getString(i);
                    if (_rs.wasNull())
                        _sb.append("null");
                    else
                        _sb.append("'").append(res).append("'");
                }break;
                default:{
                    String res = _rs.getString(i);
                    if (_rs.wasNull())
                        _sb.append("null");
                    else
                        _sb.append("'").append(res).append("'");
                }
            }

            if(i<_cols)
                _sb.append(",");
        }
    }

    public int insData(String _table, ResultSet _rs, boolean _remove_old) throws Exception{
        TT(null);
        ResultSetMetaData meta = _rs.getMetaData();
        int cols = meta.getColumnCount();
        StringBuilder sb = new StringBuilder();
        int rows = 0;
        int total = 0;
        connection.setAutoCommit(false);
        while(_rs.next()){
            total++;
            if(_remove_old)
                sb.append("delete from ").append(_table).append(" where id=").append(_rs.getInt("id")).append(";");

            sb.append("insert into ").append(_table).append(" values(");
            row2Line(sb, meta, _rs, cols);
            sb.append(")");

//            logger.debug(sb.toString());

            exeStmt.addBatch(sb.toString());
            sb.delete(0, sb.length());

            if(rows++>1000) {
                //logger.debug("exec batch a.enter");
                exeStmt.executeBatch();
                connection.commit();
                //logger.debug("exec batch a.leave");
                exeStmt.clearBatch();
                rows=0;
            }
        }//end of while
        _rs.close();

        if(rows>0){
//            logger.debug("exec batch b");
            exeStmt.executeBatch();
            connection.commit();
            exeStmt.clearBatch();
        }
        connection.setAutoCommit(true);

        TT("insData " + _table + " rows:" + total);
        return total;
    }

    private final static String C_GET_TABLE = "select [sql] from sqlite_master where [type] = 'table' and lower(name) = '%s'";
    public boolean hasTable(String _table) throws Exception{
        String sql = String.format(C_GET_TABLE, _table.toLowerCase());
        if(!qryStmt.isClosed())
            qryStmt.close();
        ResultSet rs = qryStmt.executeQuery(sql);
        boolean res = rs.next();
        rs.close();
        qryStmt.close();
        return res;
    }

    private final static String C_LIST_TABLE = "select [name] from sqlite_master where [type] = 'table' ";
    public List<String> getTableNames() throws Exception{
        if(!qryStmt.isClosed())
            qryStmt.close();
        ResultSet rs = qryStmt.executeQuery(C_LIST_TABLE);
        List<String> list = new ArrayList<>();
        while( rs.next() ){
            list.add(rs.getString(1));
        }
        rs.close();
        return list;
    }

    public void batchAdd(String _sql) throws Exception{
        exeStmt.addBatch(_sql);
    }
    public void batchRun() throws Exception{
        exeStmt.executeBatch();
        connection.commit();
        exeStmt.clearBatch();
    }

    public void runSQL(String _sql) throws Exception{
        //logger.debug("sql={}", _sql);
        exeStmt.execute(_sql);
    }

    public ResultSet get(String _sql) throws Exception{
        if(!qryStmt.isClosed())
            qryStmt.close();

        //logger.debug("get in {} :\r\n {}", config.name, _sql);
        return qryStmt.executeQuery(_sql);
    }

    public boolean isMemDB(){
        return isMemDB;
    }

    /**
     * main
     * @param _args j
     * @throws Exception
     */
    public static void main(String[] _args) throws Exception{
        DB3Cache db3 = new DB3Cache();
        db3.setup();

        DBConfig config = new DBConfig(DBDriver.MYSQL);
        config.host = "10.10.3.22";
        config.name = "etbasedata";
        config.user = "root";
        config.pass = "root";
        Connection connection = DBHelper.newConnection(config);
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("select VehicleID, VehicleNo, TerminalType, CommNO, terminalPWD, IsDeleted, IsMQHandle from v_vehicle_simple");
        db3.put("v_vehicle_simple", rs);
        rs.close();
        statement.close();

        rs = db3.get("select VehicleID, VehicleNo, TerminalType, CommNO, terminalPWD, IsDeleted, IsMQHandle from v_vehicle_simple");
        while(rs.next()){
            System.out.println(rs.getInt(1)+" "+rs.getString(2));
        }
    }
}

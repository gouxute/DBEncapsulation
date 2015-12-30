package com.etrans.lib.db;

import com.etrans.lib.kryo.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by maotz on 2015-04-10.
 * Data access from terminal;
 */
public class DBClient implements IDBClient{
    /**
     * 日志对象
     */
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DBClient.class);
    /**
     * 通信对象
     */
    private final IKryoClient kryoClient = new KryoClient(new DBObjManager());
    /**
     * 事件监听器
     */
    private final DBResultListeners listeners = new DBResultListeners();
    /**
     * 链接未建立时，所发的请求在此排队，等到链接成功后，立即自动提交
     */
    private final Queue<DBRequest> requestQueue = new ConcurrentLinkedQueue<>();
    /**
     * 已经提交到服务端的任务，放入此哈希表
     */
    private final Map<Integer, Task> taskMap = new ConcurrentHashMap<>();
    /**
     * 过期任务列表
     */
    private final List<Task> expiredList = new LinkedList<>();

    class Task {
        private final DBRequest request;
        private final IDBResultListener listener;
        public Task(DBRequest _request, IDBResultListener _listener){
            request = _request;
            listener = _listener;
        }
    }

    /**
     * 构造函数
     */
    public DBClient(){
        kryoClient.registerListeher(new IKryoListener<IKryoClient>() {
            @Override
            public void onConn(IKryoClient _sender) {
                while (!requestQueue.isEmpty()) {
                    DBRequest request = requestQueue.poll();
                    kryoClient.send(request);
                }
            }

            @Override
            public void onBrok(IKryoClient _sender) {
            }

            @Override
            public void onData(IKryoClient _sender, KryoObj _kryo_obj) {
                DBResult result = (DBResult) _kryo_obj;
                listeners.onResult(result);

                int request_id = result.getRequestID();
                Task task = taskMap.get(request_id);
                if (null != task && null != task.listener)
                    task.listener.onResult(result);

                taskMap.remove(request_id);
            }
        });

        // 定期检查是否过期的定时器
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                checkTimeout();
            }
        }, 10, 5, TimeUnit.SECONDS);
    }

    /**
     * 触发任务过期事件
     * @param _task 请求的任务
     */
    private void fireExpired(Task _task){
        // 如果还在排队中，未提交，则从队头剔除
        DBRequest top = requestQueue.peek();
        if (top==_task.request)
            requestQueue.poll();

        // 触发过期事件
        listeners.onExpired(_task.request);

        // 删除等待映射
        taskMap.remove(_task.request.getRequestID());
    }

    /**
     * 检查哪些请求已经过期
     */
    synchronized private void checkTimeout(){
        // 挑出所有过期任务
        for(Task task : taskMap.values()){
            if(task.request.hasExpired())
                expiredList.add(task);
        }

        // 触发过期事件
        for(Task task : expiredList){
            fireExpired(task);
        }
        expiredList.clear();
    }

    /**
     * 注册事件监听器
     * @param _listener 监听器
     */
    public void registerListener(IDBResultListener _listener){
        listeners.registerListener(_listener);
    }

    /**
     * 设置服务地址与端口，并启动
     * @param _host 主机
     * @param _port 端口
     */
    public void start(String _host, int _port){
        kryoClient.setServer(_host, _port);
        kryoClient.setActive(true);
    }

    /**
     * 发送请求，异步发送，等待回调
     * @param _request 请求消息
     */
    public void sendRequest(DBRequest _request){
        sendRequest(_request, null);
    }

    /**
     * 发送请求，异步发送，等待回调
     * @param _request 请求消息
     * @param _listener 监听器
     */
    public void sendRequest(DBRequest _request, IDBResultListener _listener){
        logger.info("doRequest {}", _request);

        Task task = new Task(_request, _listener);

        // 先放入等待应答哈希表
        taskMap.put(_request.getRequestID(), task);

        /* 根据链接状态确定入队或立即发送
         * 链接状态，先发送完队列，然后发送当前任务
         * 链接处于断开状态，放入队列末尾
         */
        if (kryoClient.isConnected())
            kryoClient.send(_request);
        else
            requestQueue.offer(_request);
    }

    /**
     * 同步执行，等待结果
     * @param _request 请求
     * @return 结果
     */
    public DBResult runRequest(DBRequest _request){
        final AtomicReference ref = new AtomicReference(DBResult.makeTimeout(_request.getRequestID()));
        final AtomicBoolean qry_ok = new AtomicBoolean(false);

        IDBResultListener listener = new IDBResultListener() {
            @Override
            public void onExpired(DBRequest _request) {
                ref.set(DBResult.makeTimeout(_request.getRequestID()));
                qry_ok.set(true);
            }

            @Override
            public void onResult(DBResult _result) {
                ref.set(_result);
                qry_ok.set(true);
            }
        };

        sendRequest(_request, listener);

        while(!qry_ok.get()){
            if(_request.hasExpired()){
                ref.set(DBResult.makeTimeout(_request.getRequestID()));
                break;
            }else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.error("error on run request", e);
                }
            }
        }

        return (DBResult)ref.get();
    }

    private static void testSYNC() throws Exception{
        final DBClient client = new DBClient();
        client.start("127.0.0.1", 9000);

        client.registerListener(new IDBResultListener() {
            @Override
            public void onExpired(DBRequest _request) {
                logger.info("onExpired {}", _request);
            }

            @Override
            public void onResult(DBResult _result) {
                logger.info("onResult {}", _result);
            }
        });

        String sql = "select * from alarm_info_2015_05 "
                +"where alarmtime between \"2015-05-16\" and \"2015-05-17\" ";
        DBRequest req = DBRequest.makeQueryRequest(sql);
        DBReqArg arg = req.getArg();
        arg.setDBName("etalarm");
        arg.setDataFormat(DBDataSetFormat.DATASET_FORMAT_JSON);
        arg.setTimeout(10);

        client.sendRequest(req);
    }

    private static void testASYNC() throws Exception{
        final DBClient client = new DBClient();
        client.start("127.0.0.1", 9000);

        String sql = "select * from alarm_info_2015_05 "
                +"where alarmtime between \"2015-05-16\" and \"2015-05-17\" ";
        DBRequest req = DBRequest.makeQueryRequest(sql);
        DBReqArg arg = req.getArg();
        arg.setDBName("etalarm");
        arg.setDataFormat(DBDataSetFormat.DATASET_FORMAT_JSON);
        arg.setTimeout(10);

        long tt = System.currentTimeMillis();
        DBResult result = client.runRequest(req);
        tt = System.currentTimeMillis() - tt;

        logger.info("ticks={} data={}", tt, result);
    }

    public static void main(String[] _args) throws Exception{
        testSYNC();
        testASYNC();

        Thread.sleep(Integer.MAX_VALUE);
    }
}

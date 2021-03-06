/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redp.HbaseInterface;

/**
 *
 * @author Administrator
 */
import com.redp.*;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class BaseOperate {

    private Gson gson = new Gson();
    public static Configuration conf = null;
    public HTable table = null;
    public HBaseAdmin admin = null;

    static {
        conf = HBaseConfiguration.create();
        System.out.println(conf.get("hbase.zookeeper.quorum"));
    }

    /**
     * 创建一张表
     *
     * @param tableName 要创建的表名
     * @param familys 列族名
     * @throws Exception 抛出异常
     */
    public static void creatTable(String tableName, String[] familys)
            throws Exception {
        HBaseAdmin admin = new HBaseAdmin(conf);
        if (admin.tableExists(tableName)) {
            System.out.println("table already exists!");
        } else {
            HTableDescriptor tableDesc = new HTableDescriptor(tableName);
            for (int i = 0; i < familys.length; i++) {
                tableDesc.addFamily(new HColumnDescriptor(familys[i]));
            }
            admin.createTable(tableDesc);

            System.out.println("create table " + tableName + " ok.");
        }
    }

    /**
     * 删除表
     *
     * @param tableName 要删除的表名
     * @throws Exception 抛出异常
     */
    public static void deleteTable(String tableName) throws Exception {
        try {
            HBaseAdmin admin = new HBaseAdmin(conf);
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
            System.out.println("delete table " + tableName + " ok.");
        } catch (MasterNotRunningException | ZooKeeperConnectionException e) {
            e.printStackTrace();
        }
    }

    /**
     * 插入一行记录
     *
     * @param tableName 表名
     * @param rowKey rowkey
     * @param family 列族
     * @param qualifier 列
     * @param value 值
     * @throws Exception 抛出异常
     */
    public static void addRecord(String tableName, String rowKey,
            String family, String qualifier, String qualifier1, String value, int count) throws Exception {
        try {
            HTable table = new HTable(conf, tableName);
            Put put = new Put(Bytes.toBytes(rowKey));
            put.add(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(value));
            put.add(Bytes.toBytes(family), Bytes.toBytes(qualifier1), Bytes.toBytes(String.valueOf(count)));
            table.put(put);
            System.out.println("insert recored " + rowKey + " to table " + tableName + " ok.");
        } catch (IOException e) {
            System.out.println("--------------------------------rowkey:" + rowKey + ",family:" + family + ",qualifier:" + qualifier + ",value:" + value + "......" + "保存失败！！！" + "-------------------------");
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 删除某一个单元格的值
     *
     * @param tableName 表名
     * @param rowKey rowkey
     * @param family 列族
     * @param qualifier 列
     * @param value 值
     * @throws IOException 抛出异常
     */
    public void deleteValue(String tableName, String rowKey, String family, String qualifier, String value) throws IOException {
        HTable table = new HTable(conf, tableName);
        Delete delete = new Delete(Bytes.toBytes(rowKey));
        delete.deleteColumns(Bytes.toBytes(family), Bytes.toBytes(qualifier));
        try {
            table.checkAndDelete(Bytes.toBytes(rowKey), Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(value), delete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除某一行
     *
     * @param tableName 表名
     * @param rowKey rowkey
     * @throws IOException 抛出异常
     */
    public static void delete(String tableName, String rowKey) throws IOException {
        HTable table = new HTable(conf, tableName);
        List list = new ArrayList();
        Delete del = new Delete(rowKey.getBytes());
        list.add(del);
        table.delete(list);
        System.out.println("del recored " + rowKey + " ok.");
    }

    /**
     * 根据rowkey查找某行记录
     *
     * @param tableName 表名
     * @param row rowkey
     */
    public static void getOneRecord(String tableName, String row) {
        try {
            HashMap<String, String> map = new HashMap<>();
            HTable table = new HTable(conf, tableName);
            Get get = new Get(Bytes.toBytes(row));
            Result result = table.get(get);
            for (Cell cell : result.rawCells()) {
                System.out.println("列族：" + Bytes.toString(CellUtil.cloneFamily(cell)) + "...列：" + Bytes.toString(CellUtil.cloneQualifier(cell)) + "...值：" + Bytes.toString(CellUtil.cloneValue(cell)));
            }
        } catch (IOException ex) {
            Logger.getLogger(BaseOperate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 根据rowkey查找某行记录
     *
     * @param tableName 表名
     * @param row rowkey
     */
    public static HashMap<String, Object> getNewOneRecord(String tableName, String row) {
        try {
            HashMap<String, Object> map = new HashMap<>();
            HTable table = new HTable(conf, tableName);
            Get get = new Get(Bytes.toBytes(row));
            Result result = table.get(get);
            for (Cell cell : result.rawCells()) {
                map.put(Bytes.toString(CellUtil.cloneFamily(cell)) + ":" + Bytes.toString(CellUtil.cloneQualifier(cell)), Bytes.toString(CellUtil.cloneValue(cell)));
            }
            return map;
        } catch (IOException ex) {
            Logger.getLogger(BaseOperate.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * 删除指定行某一列
     *
     * @param tableName 表名
     * @param row rowkey
     * @param family 列族
     * @param qualifier 列
     */
    public static void deleteColumn(String tableName, String row, String family, String qualifier) {
        try {
            HTable table = new HTable(conf, tableName);
            Delete delete = new Delete(Bytes.toBytes(row));
            delete.deleteColumns(Bytes.toBytes(family), Bytes.toBytes(qualifier));
            table.delete(delete);
            System.out.println("删除操作...row: " + row + " ...qualifier: " + qualifier);
        } catch (IOException ex) {
            Logger.getLogger(BaseOperate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 根据传入的row，family，统计该行的列族里共有多少列qualifier
     *
     * @param tableName 表名
     * @param row rowkey
     * @param family 列族
     * @return qualifier的总条数加1，异常返回0
     */
    public static int getCount(String tableName, String row, String family) {
        try {
            HTable table = new HTable(conf, tableName);
            Get get = new Get(Bytes.toBytes(row));
            get.addFamily(Bytes.toBytes(family));
            Result result = table.get(get);
            int count = 0;
            for (Cell cell : result.rawCells()) {
                count += 1;
            }
            count = count + 1;
            return count;
        } catch (IOException ex) {
            Logger.getLogger(BaseOperate.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    public static int getQualifier(String tableName, String row, String family, String qualifier) {
        try {
            HTable table = new HTable(conf, tableName);
            Get get = new Get(Bytes.toBytes(row));
            get.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier));
            Result result = table.get(get);
            int count = 0;
            String value = "";
            for (Cell cell : result.rawCells()) {
                value = Bytes.toString(CellUtil.cloneValue(cell));
            }
            if (!"".equals(value)) {
                count = Integer.valueOf(value);
            }
            return count;
        } catch (IOException ex) {
            Logger.getLogger(BaseOperate.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    
    //添加新的列簇family
    public static void addNewFamily(String tableName, String fam) {
        try {
            HBaseAdmin admin = new HBaseAdmin(conf);
            HTable table = new HTable(conf, tableName);
            HTableDescriptor descriptor = new HTableDescriptor(table.getTableDescriptor());
            descriptor.addFamily(new HColumnDescriptor(Bytes.toBytes(fam)));
            admin.disableTable(tableName);
            admin.modifyTable(Bytes.toBytes(tableName), descriptor);
            admin.enableTable(tableName);
            table.close();
            admin.close();
        } catch (IOException ex) {
            Logger.getLogger(BaseOperate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String args[]) {
        //deleteColumn("WechatBid", "55653", "keywords", "key_4");
        addNewFamily("WechatLog", "userID");
//        int count = BaseOperate.getQualifier("WechatLog", "55653", "keywords", "key_count");
//        System.out.print(count);
    }

}

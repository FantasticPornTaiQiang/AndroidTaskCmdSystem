package com.example.homework.utils;

import com.example.homework.data.LongTermTask;
import com.example.homework.data.PeriodicTask;
import com.example.homework.data.Task;
import com.example.homework.data.TaskFactory;
import com.example.homework.data.TaskKind;
import com.example.homework.data.TemporaryTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SerializeAndDeserialize {

    //序列化任务
    public static String serializeforTask(List<?> taskList, int position) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(taskList.get(position));
        String serializedStr = byteArrayOutputStream.toString("ISO-8859-1");
        serializedStr = java.net.URLEncoder.encode(serializedStr, "UTF-8");
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return serializedStr;
    }

    //序列化任务
    public static String serializeforLongtermTask(LongTermTask longTermTask) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(longTermTask);
        String serializedStr = byteArrayOutputStream.toString("ISO-8859-1");
        serializedStr = java.net.URLEncoder.encode(serializedStr, "UTF-8");
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return serializedStr;
    }

    //序列化任务清单
    public static String serializeforTaskList(List<?> taskList) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(taskList);
        String serializedStr = byteArrayOutputStream.toString("ISO-8859-1");
        serializedStr = java.net.URLEncoder.encode(serializedStr, "UTF-8");
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return serializedStr;
    }

    //序列化总列表
    public static String serializeforTaskFactoryList(List<TaskFactory> taskFactoryList) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(taskFactoryList);
        String serializedStr = byteArrayOutputStream.toString("ISO-8859-1");
        serializedStr = java.net.URLEncoder.encode(serializedStr, "UTF-8");
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return serializedStr;
    }

    //反序列化任务
    public static Task deSerializeforTask(String str, TaskKind taskKind) throws IOException, ClassNotFoundException {
        String readStr = java.net.URLDecoder.decode(str, "UTF-8");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                readStr.getBytes("ISO-8859-1"));
        ObjectInputStream objectInputStream = new ObjectInputStream(
                byteArrayInputStream);
        TemporaryTask temporaryTask = null;
        PeriodicTask periodicTask = null;
        LongTermTask longTermTask = null;
        if(taskKind == TaskKind.TEMPORARYTASK) temporaryTask = (TemporaryTask) objectInputStream.readObject();
        else if (taskKind == TaskKind.PERIODICTASK) periodicTask = (PeriodicTask) objectInputStream.readObject();
        else if (taskKind == TaskKind.LONGTERMTASK) longTermTask = (LongTermTask) objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();

        if(taskKind == TaskKind.TEMPORARYTASK) return temporaryTask;
        else if (taskKind == TaskKind.PERIODICTASK) return periodicTask;
        else if (taskKind == TaskKind.LONGTERMTASK) return longTermTask;
        else return null;
    }

    //反序列化任务
    public static LongTermTask deSerializeforLongTermTask(String str) throws IOException, ClassNotFoundException {
        String readStr = java.net.URLDecoder.decode(str, "UTF-8");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                readStr.getBytes("ISO-8859-1"));
        ObjectInputStream objectInputStream = new ObjectInputStream(
                byteArrayInputStream);
        LongTermTask longTermTask = (LongTermTask) objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();
        return longTermTask;
    }

    //反序列化任务清单
    public static List<?> deSerializeforTaskList(String str, TaskKind taskKind) throws IOException, ClassNotFoundException {
        String readStr = java.net.URLDecoder.decode(str, "UTF-8");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                readStr.getBytes("ISO-8859-1"));
        ObjectInputStream objectInputStream = new ObjectInputStream(
                byteArrayInputStream);
        List<TemporaryTask> temporaryTaskList = new ArrayList<>();
        List<PeriodicTask> periodicTaskList = new ArrayList<>();
        List<LongTermTask> longTermTaskList = new ArrayList<>();
        if(taskKind == TaskKind.TEMPORARYTASK) temporaryTaskList = (List<TemporaryTask>) objectInputStream.readObject();
        else if (taskKind == TaskKind.PERIODICTASK) periodicTaskList = (List<PeriodicTask>) objectInputStream.readObject();
        else if (taskKind == TaskKind.LONGTERMTASK) longTermTaskList = (List<LongTermTask>) objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();

        if(taskKind == TaskKind.TEMPORARYTASK) return temporaryTaskList;
        else if (taskKind == TaskKind.PERIODICTASK) return periodicTaskList;
        else if (taskKind == TaskKind.LONGTERMTASK) return longTermTaskList;
        else return null;
    }

    //反序列化总列表
    public static List<TaskFactory> deSerializeforTaskFactoryList(String str) throws IOException, ClassNotFoundException {
        String readStr = java.net.URLDecoder.decode(str, "UTF-8");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                readStr.getBytes("ISO-8859-1"));
        ObjectInputStream objectInputStream = new ObjectInputStream(
                byteArrayInputStream);
        List<TaskFactory> taskFactoryList = (List<TaskFactory>) objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();
        return taskFactoryList;
    }
}

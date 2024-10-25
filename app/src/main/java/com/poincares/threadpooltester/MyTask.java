package com.poincares.threadpooltester;


import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.io.InputStreamReader;



public class MyTask implements Runnable
{
  private TaskResponser responser_ = null;
  private String urlString_ = null;

  public interface TaskResponser {
    public void onResponse(String url, long executeSpan, String urlInfo, String error);
  }


  public MyTask(TaskResponser responser, String urlString ) {
    this.responser_ = responser;
    this.urlString_ = urlString;
  }

  @Override
  public void run()  {
    long startTs = System.currentTimeMillis();
    long executeSpan = -1;
    String urlRetInfo = null;
    String error = null;

    if (null != urlString_) {
      try {
        urlRetInfo = curlIpInfo(urlString_);
      } catch (Exception e) {
//      throw new RuntimeException(e);
//        e.printStackTrace();
        error = e.toString();
      }

      executeSpan = System.currentTimeMillis() - startTs;
    }

    if (null != responser_) {
     responser_.onResponse(urlString_, executeSpan, urlRetInfo, error);
    }

  }

  //String urlString = "https://ipinfo.io";
  public static String curlIpInfo(String urlString) throws Exception {
    // 目标URL

    URL url = new URL(urlString);

    // 打开连接
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

    // 配置请求方式为GET
    connection.setRequestMethod("GET");

    // 设置请求头，告诉服务器我们期望JSON响应
    connection.setRequestProperty("Accept", "application/json");

    // 设置超时时间
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(5000);

    // 获取响应代码
    int responseCode = connection.getResponseCode();

    // 如果响应代码为200 OK，继续处理
    if (responseCode == HttpURLConnection.HTTP_OK) {
      // 创建BufferedReader读取输入流
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuilder response = new StringBuilder();

      // 逐行读取响应并追加到StringBuilder
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      // 断开连接
      connection.disconnect();

      // 返回响应结果字符串
      return response.toString();
    } else {
      // 响应代码非200，抛出异常
      throw new Exception("Request failed with response code: " + responseCode);
    }
  }



}
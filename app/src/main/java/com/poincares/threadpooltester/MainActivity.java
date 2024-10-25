package com.poincares.threadpooltester;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements MyTask.TaskResponser{

  private Button btActiveOneFetch_ = null;
  private ThreadPoolExecutor executor_ = null;
  private long mainThreadId = 0;
  private String TAG = "ThreadPool";
  private long logIdx_ = 0;

  private int currentUrlIdx_ = 0;
  private String[] urlArray_ = {"https://ipinfo.io",
                                "https://www.baidu.com",
                                "https://ip.tool.chinaz.com/ip",
                                "https://httpbin.org/ip"};
  private StringBuilder sb = new StringBuilder();


  //setting for thread pool
  private int maxPoolSize = 30;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_main);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });


    initThreadPool();

    btActiveOneFetch_ = findViewById(R.id.btActiveFetch);
    btActiveOneFetch_.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        doBatchJobs();
      }
    });
  }

  @Override
  protected void onPause() {
    super.onPause();
    uninitThreadPool();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    uninitThreadPool();
  }

  @Override
  protected void onResume() {
    super.onResume();
    initThreadPool();
  }

  private void initThreadPool() {
    if (null == executor_) {
      executor_ = new ThreadPoolExecutor(5, maxPoolSize, 100,
              TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>(20),
              new ThreadPoolExecutor.CallerRunsPolicy());
    }

    mainThreadId = Thread.currentThread().getId();

  }

  private void uninitThreadPool() {
    if (null != executor_) {
      executor_.shutdownNow();
      executor_ = null;
    }
  }


  @Override
  public void onResponse(String url, long executeSpan, String urlInfo, String error) {

    String log2show = null;
    long threadID = -1;
    synchronized (this){
      threadID = Thread.currentThread().getId();

      sb.setLength(0);

      sb.append("logIdx(").append(++logIdx_).append("), ");
      sb.append("MainThread:").append(mainThreadId)
              .append(", ExecuteThread:").append(threadID);
      sb.append(" || URL -- ").append(url).append(" || executeSpan:")
              .append(executeSpan);
      if (null != urlInfo)
        sb.append(" || return info: ").append(urlInfo);
      if (null != error)
        sb.append(" || error:").append(error);

      log2show = sb.toString();

    }

    String finalLog2show = log2show;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, finalLog2show);
      }
    });
  }


  private void doBatchJobs() {
    int loopCount = 30;

    for (int i = 0; i < loopCount; i++) {
      doOneJob();
    }

  }

  private void doOneJob() {
    int curIdx = (currentUrlIdx_++ ) % urlArray_.length;
    if (currentUrlIdx_ < 0) {
      currentUrlIdx_ = 0;
      curIdx = 0;
    }

    executor_.submit(new MyTask(MainActivity.this, urlArray_[curIdx]));
  }

}
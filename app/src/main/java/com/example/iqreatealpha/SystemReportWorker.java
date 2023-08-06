package com.example.iqreatealpha;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SystemReportWorker extends Worker {
    private Context context;
    private String userId;
    private String email;
    private long appLaunchTime;

    public SystemReportWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        userId = workerParams.getInputData().getString("userId");
        email = workerParams.getInputData().getString("email");
        appLaunchTime = workerParams.getInputData().getLong("appLaunchTime", 0L);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            SystemReportGenerator reportGenerator = new SystemReportGenerator(context, userId);
            reportGenerator.generateSystemReport(email, appLaunchTime);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}

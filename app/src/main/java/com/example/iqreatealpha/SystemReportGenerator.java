package com.example.iqreatealpha;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrength;
import android.telephony.TelephonyManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.datatransport.BuildConfig;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SystemReportGenerator {
    private Context context;
    private String userId;

    public SystemReportGenerator(Context context, String userId) {
        this.context = context;
        this.userId = userId;
    }

    public void generateSystemReport(String email, long appLaunchTime) {
        // Retrieve report details
        String appName = getAppName();
        String appVersion = getAppVersion();
        String packageName = getPackageName();
        String releaseType = getReleaseType();
        String deviceModel = getDeviceModel();
        String osVersion = getOSVersion();
        String deviceId = getDeviceId();
        String screenResolution = getScreenResolution();
        String availableStorage = getAvailableStorage();
        String networkStatus = getNetworkStatus();
        String authenticationStatus = getAuthenticationStatus();
        int maxContentWidth = 580;

        // Retrieve the username
        getUserUsername(userId, new OnUsernameRetrievedListener() {
            @Override
            public void onUsernameRetrieved(String username) {
                if (username != null) {
                    // Now you have the username, you can use it to generate the system report
                    // You can pass it to the PdfAdapter or use it in any other way you need.
                    Log.d("SystemReportGenerator", "Username retrieved: " + username);

                    // Create the system report document using the username
                    PdfDocument document = new PdfDocument();
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size: 595x842 pixels
                    PdfDocument.Page page = document.startPage(pageInfo);
                    Canvas canvas = page.getCanvas();

                    // Define the text format
                    int fontSize = 12;
                    int textAreaWidth = maxContentWidth;
                    float x = 20;
                    float y = 20;

                    String permissionsText = getGrantedPermissions();

                    // TextPaint for StaticLayout
                    TextPaint textPaint = new TextPaint();
                    textPaint.setColor(Color.BLACK);
                    textPaint.setTextSize(fontSize);

                    // Write the report details to the PDF document
                    Paint paint = new Paint();
                    paint.setColor(Color.BLACK);
                    paint.setTextSize(fontSize);

                    StaticLayout staticLayout = new StaticLayout(permissionsText, textPaint, textAreaWidth,
                            Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

                    // User Information
                    y = drawTextWithLineBreaks(canvas,"User Information: ", x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"User ID: " + userId, x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"User Username: " + username, x, y, paint);
                    y += fontSize * 2;

                    // Application Information
                    y = drawTextWithLineBreaks(canvas,"Application Information: ", x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"Application Name: " + appName, x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"Application Version: " + appVersion, x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"Package Name: " + packageName, x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"Release Type: " + releaseType, x, y, paint);
                    y += fontSize * 2;

                    // Device Information
                    y = drawTextWithLineBreaks(canvas,"Device Information: ", x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"Device Model: " + deviceModel, x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"Operating System Version: " + osVersion, x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"Device ID: " + deviceId, x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"Screen Resolution: " + screenResolution, x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"Available Storage: " + availableStorage, x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"Network Status: " + networkStatus, x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"Authentication Status: " + authenticationStatus, x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"App Launch Time: " + appLaunchTime + " ms", x, y, paint);
                    y += fontSize * 2;

                    // Network Information
                    y = drawTextWithLineBreaks(canvas,"Network Information: ", x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"IP Address: " + getIPAddress(), x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"Wi-Fi Network Details: " + getWiFiNetworkDetails(), x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"Cellular Network Information: " + getCellularNetworkInfo(), x, y, paint);
                    y += fontSize * 2;

                    // Permissions Information
                    y = drawTextWithLineBreaks(canvas,"Granted App Permissions: ", x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"Permissions Granted: " + getGrantedPermissions(), x, y, paint);
                    y += fontSize * 2;

                    // Performance Information
                    y = drawTextWithLineBreaks(canvas,"Analytics and Performance Metrics: ", x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"App Performance Data: " + getAppPerformance(), x, y, paint);
                    y += fontSize;

                    // App Usage Information
                    y = drawTextWithLineBreaks(canvas,"App Usage and Interactions: ", x, y, paint);
                    y += fontSize;
                    y = drawTextWithLineBreaks(canvas,"Error Logs: " + getErrorLogs(), x, y, paint);
                    y += fontSize * 2;

                    // Finish writing the document
                    document.finishPage(page);

                    // Save the system report as a PDF file
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    String currentDate = dateFormat.format(new Date());
                    String fileName = email.replace("@", "_") + ".pdf";

                    // Save the system report as a PDF file
                    File reportFile = new File(context.getExternalFilesDir(null), fileName);

                    try {
                        FileOutputStream outputStream = new FileOutputStream(reportFile);
                        document.writeTo(outputStream);
                        document.close();
                        outputStream.flush();
                        outputStream.close();
                        Log.d("SystemReportGenerator", "System report saved: " + fileName);

                        // Upload the system report to Firebase Storage
                        uploadSystemReport(reportFile, fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Handle case when the username is not found in Firestore
                    Log.d("SystemReportGenerator", "Username not found for userId: " + userId);
                }
            }
            private float drawTextWithLineBreaks(Canvas canvas, String text, float x, float y, Paint paint) {
                int maxContentWidth = 500;
                List<String> lines = splitTextToFitWidth(text, paint, maxContentWidth);
                for (String line : lines) {
                    canvas.drawText(line, x, y, paint);
                    y += paint.getFontSpacing();
                }
                return y;
            }

            private List<String> splitTextToFitWidth(String text, Paint paint, int maxWidth) {
                List<String> lines = new ArrayList<>();
                int start = 0;
                int end;
                int textLength = text.length();
                while (start < textLength) {
                    end = paint.breakText(text, start, textLength, true, maxWidth, null);
                    lines.add(text.substring(start, start + end));
                    start += end;
                }
                return lines;
            }
        });
    }


    private void uploadSystemReport(File reportFile, String fileName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("SystemReports").child(fileName);
        storageRef.putFile(Uri.fromFile(reportFile))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("SystemReportGenerator", "System report uploaded: " + fileName);

                        // Get the download URL for the uploaded file
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String pdfUrl = uri.toString(); // Use pdfUrl instead of downloadUrl
                                saveReportToFirestore(fileName, pdfUrl); // Pass pdfUrl instead of downloadUrl
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("SystemReportGenerator", "Failed to get download URL for the system report: " + fileName, e);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("SystemReportGenerator", "Failed to upload system report: " + fileName, e);
                    }
                });
    }

    private void saveReportToFirestore(String fileName, String pdfUrl) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String reportName = getReportNameFromFileName(fileName);
        String dateGenerated = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> report = new HashMap<>();
        report.put("reportName", reportName);
        report.put("dateGenerated", dateGenerated);
        report.put("userId", userId);
        report.put("pdfUrl", pdfUrl);

        firestore.collection("SystemReports")
                .document(reportName) // Use the report name as the document ID
                .set(report) // Set the data for the document
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("SystemReportGenerator", "System report saved to Firestore: " + fileName);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("SystemReportGenerator", "Failed to save system report to Firestore: " + fileName, e);
                    }
                });
    }

    // Helper methods to retrieve report details
    private String getAppName() {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.applicationInfo.loadLabel(packageManager).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getAppVersion() {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getPackageName() {
        return context.getPackageName();
    }

    private String getReleaseType() {
        int appVersionCode = BuildConfig.VERSION_CODE;
        return (appVersionCode % 2 == 0) ? "Production" : "Debug";
    }

    private String getDeviceModel() {
        return Build.MODEL;
    }

    private String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    private String getDeviceId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private String getScreenResolution() {
        int density = context.getResources().getDisplayMetrics().densityDpi;
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;
        return width + "x" + height + " (" + density + "dpi)";
    }

    private String getAvailableStorage() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return (availableBlocks * blockSize) / (1024 * 1024) + " MB";
    }

    private String getNetworkStatus() {
        boolean isConnected = NetworkUtils.isNetworkConnected(context);
        return isConnected ? "Connected" : "Not Connected";
    }

    private String getAuthenticationStatus() {
        return "Authenticated";
    }

    private void getUserUsername(String userId, OnUsernameRetrievedListener listener) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Users")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            String username = documentSnapshot.getString("username");
                            // Return the username using the listener
                            listener.onUsernameRetrieved(username);
                        } else {
                            // User not found in Firestore or no documents found
                            listener.onUsernameRetrieved(null);
                        }
                    } else {
                        // Error fetching data from Firestore
                        Log.e("SystemReportGenerator", "Error fetching user data from Firestore:", task.getException());
                        listener.onUsernameRetrieved(null);
                    }
                });
    }


    public interface OnUsernameRetrievedListener {
        void onUsernameRetrieved(String username);
    }



    private String getReportNameFromFileName(String fileName) {
        // Extract the report name from the file name
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex != -1) {
            return fileName.substring(0, extensionIndex);
        }
        return fileName;
    }

    private String getIPAddress() {
        // Retrieve IP address using the IPAddressUtils class
        String ipAddress = IPAddressUtils.getDeviceIPAddress();
        return ipAddress != null ? ipAddress : "N/A"; // Provide a default value if the IP address is not available
    }

    private String getWiFiNetworkDetails() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                String ssid = wifiInfo.getSSID();
                String bssid = wifiInfo.getBSSID();

                return "SSID: " + ssid + ", BSSID: " + bssid;
            }
        }

        return "Wi-Fi details not available";
    }

    private String getCellularNetworkInfo() {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null && context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            StringBuilder cellularInfo = new StringBuilder();

            // Get the operator name
            String operatorName = telephonyManager.getNetworkOperatorName();
            if (!operatorName.isEmpty()) {
                cellularInfo.append("Operator: ").append(operatorName);
            } else {
                cellularInfo.append("Operator: N/A");
            }

            // Get the signal strength (RSSI)
            List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();
            if (allCellInfo != null) {
                for (CellInfo cellInfo : allCellInfo) {
                    if (cellInfo instanceof CellInfoGsm) {
                        CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                        CellSignalStrength cellSignalStrength = cellInfoGsm.getCellSignalStrength();
                        int signalStrength = cellSignalStrength.getDbm();
                        cellularInfo.append(", Signal Strength: ").append(signalStrength).append(" dBm");
                        break; // Assuming there is only one GSM cell in the list
                    }
                }
            }

            return cellularInfo.toString();
        }

        return "Cellular details not available";
    }

    private String getGrantedPermissions() {
        StringBuilder permissionsList = new StringBuilder();

        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] permissions = packageInfo.requestedPermissions;

            if (permissions != null) {
                for (String permission : permissions) {
                    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                        // Get the name of the permission from its constant value
                        PermissionInfo permissionInfo = context.getPackageManager().getPermissionInfo(permission, 0);
                        String permissionName = permissionInfo.loadLabel(context.getPackageManager()).toString();

                        // Append the granted permission to the list
                        permissionsList.append(permissionName).append(", ");
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Remove the trailing comma and space if there are granted permissions
        if (permissionsList.length() > 2) {
            permissionsList.setLength(permissionsList.length() - 2);
        }

        return permissionsList.toString();
    }


    private String getScreensVisited() {
        // Retrieve screens visited within the app
        return "Home Screen, Settings Screen, Profile Screen, Search Screen, Upload Screen";
    }

    private String getErrorLogs() {
        // Retrieve error logs and exceptions encountered

        // Create a StringBuilder to collect error logs
        StringBuilder errorLogs = new StringBuilder();

        // Set the log tag to identify your application's logs
        String logTag = "IqreateErrorLogs";

        // Get the logs from Logcat
        try {
            // Run 'logcat -d' to get the logs in the device buffer
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(logTag)) {
                    errorLogs.append(line).append("\n");
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return the collected error logs
        return errorLogs.toString();
    }

    public String getAppPerformance() {
        // Retrieve app performance data (CPU usage, memory consumption)

        // CPU Usage
        double cpuUsage = getCPUUsage();

        // Memory Consumption
        long memoryConsumption = getMemoryConsumption(context);

        return "CPU Usage: " + cpuUsage + "%, Memory Consumption: " + memoryConsumption + " MB";
    }

    // Helper method to get CPU usage as a percentage
    private double getCPUUsage() {
        double cpuUsage = 0.0;

        try {
            String[] cpuStats = readCpuStats();
            if (cpuStats != null) {
                // Calculate total CPU time
                long totalCpuTime = Long.parseLong(cpuStats[0]) + Long.parseLong(cpuStats[1]) +
                        Long.parseLong(cpuStats[2]) + Long.parseLong(cpuStats[3]);

                // Calculate idle CPU time
                long idleCpuTime = Long.parseLong(cpuStats[3]);

                // Calculate CPU usage percentage
                cpuUsage = (totalCpuTime - idleCpuTime) / (double) totalCpuTime * 100;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cpuUsage;
    }

    private static String[] readCpuStats() {
        String[] cpuStats = null;
        try {
            FileReader fileReader = new FileReader("/proc/stat");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            if (line != null) {
                cpuStats = line.split(" ");
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cpuStats;
    }

    // Helper method to get memory consumption in MB
    private long getMemoryConsumption(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        long memoryUsedMB = (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024);
        return memoryUsedMB;
    }

    private String getCrashReports() {
        // Retrieve crash reports and error logs
        return "Crash Report 1: java.lang.NullPointerException";
    }
}

package knf.animeflv.BackgroundChecker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.msebera.android.httpclient.Header;
import knf.animeflv.BackDownload;
import knf.animeflv.JsonFactory.BaseGetter;
import knf.animeflv.JsonFactory.JsonTypes.INICIO;
import knf.animeflv.Parser;
import knf.animeflv.R;
import knf.animeflv.Utils.FileUtil;
import knf.animeflv.Utils.NetworkUtils;
import knf.animeflv.Utils.NoLogInterface;
import knf.animeflv.Utils.UtilNotBlocker;
import knf.animeflv.Utils.UtilSound;
import knf.animeflv.Utils.objects.MainObject;
import knf.animeflv.newMain;

public class startBackground {
    public static void compareNots(final Context context) {
        if (NetworkUtils.isNetworkAvailable()) {
            try {
                BaseGetter.getJson(context, new INICIO(), new BaseGetter.AsyncInterface() {
                    @Override
                    public void onFinish(String json) {
                        try {
                            startCompare(context, new JSONObject(json));
                        } catch (Exception e) {
                            e.printStackTrace();
                            startCompare(context, null);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Conexion", "No hay internet  -- Error Async Nots");
            }
        }
    }

    public static void checkUpdate(final Context context) throws Exception {
        if (NetworkUtils.isNetworkAvailable()) {
            try {
                AsyncHttpClient client = new AsyncHttpClient();
                client.setConnectTimeout(15000);
                client.setLogInterface(new NoLogInterface());
                client.setLoggingEnabled(false);
                client.get("https://raw.githubusercontent.com/jordyamc/Animeflv/master/app/version.html", null, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        startUpdate(context, "");
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        startUpdate(context, responseString);
                    }
                });
            } catch (Exception e) {
                Log.e("Conexion", "No hay internet  -- Error Async Update");
            }
        }
    }

    private static void startCompare(Context context, @Nullable JSONObject s) {
        if (new Parser().checkStatus(s) == 0) {
            String ext_storage_state = Environment.getExternalStorageState();
            File mediaStorage = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache");
            if (ext_storage_state.equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
                if (!mediaStorage.exists()) {
                    mediaStorage.mkdirs();
                }
            }
            File file = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache/inicio.txt");
            String file_loc = Environment.getExternalStorageDirectory() + "/Animeflv/cache/inicio.txt";
            if (NetworkUtils.isNetworkAvailable() && s != null) {
                if (!file.exists()) {
                    Log.d("Archivo:", "No existe");
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        Log.d("Archivo:", "Error al crear archivo");
                    }
                    FileUtil.writeToFile(s.toString(), file);
                } else {
                    String txt = FileUtil.getStringFromFile(file_loc);
                    List<MainObject> mainobjects = Parser.parseMainList(s);
                    List<MainObject> oldobjects = Parser.parseMainList(txt);
                    Boolean desc = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("autoDesc", false);
                    if (FileUtil.isJSONValid(txt)) {
                        if (!mainobjects.get(0).eid.equals(oldobjects.get(0).eid)) {
                            FileUtil.writeToFile(s.toString(), file);
                            String act = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("reload", "0");
                            if (act.trim().equals("0")) {
                                context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putString("reload", "1").apply();
                            } else {
                                context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putString("reload", "0").apply();
                            }
                            int num = 0;
                            Boolean isnot = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notificaciones", true);
                            Set<String> sts = context.getSharedPreferences("data", Context.MODE_PRIVATE).getStringSet("eidsNot", new HashSet<String>());
                            loop:
                            {
                                for (MainObject st : mainobjects) {
                                    if (!st.eid.equals(oldobjects.get(0).eid)) {
                                        String favoritos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("favoritos", "");
                                        Boolean comp = favoritos.startsWith(st.aid + ":::") || favoritos.contains(":::" + st.aid + ":::");
                                        if (comp && desc && isnot) {
                                            Descargar(context, st.titulo, st.eid);
                                        }
                                        num += 1;
                                        sts.add(st.eid);
                                    } else {
                                        break loop;
                                    }
                                }
                            }
                            if (isnot && !UtilNotBlocker.isBlocked()) {
                                int nCaps = context.getSharedPreferences("data", Context.MODE_PRIVATE).getInt("nCaps", 0) + num;
                                context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putInt("nCaps", nCaps).apply();
                                context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putStringSet("eidsNot", sts).apply();
                                String mess;
                                String NotTit;
                                if (nCaps == 1) {
                                    mess = mainobjects.get(0).titulo + " " + mainobjects.get(0).eid.replace("E", "").split("_")[1];
                                    NotTit = "Nuevo capitulo disponible!";
                                } else {
                                    mess = "Hay " + Integer.toString(nCaps) + " nuevos capitulos disponibles!!!";
                                    NotTit = "AnimeFLV";
                                }
                                String temp = "";
                                List<String> tlist = new ArrayList<>();
                                tlist.addAll(sts);
                                List<String> eids = Parser.getEids(s);
                                for (String alone : tlist) {
                                    String[] data = alone.replace("E", "").split("_");
                                    if (tlist.get(tlist.size() - 1).equals(alone)) {
                                        temp += mainobjects.get(eids.indexOf(alone)).titulo + " " + data[1];
                                    } else {
                                        temp += mainobjects.get(eids.indexOf(alone)).titulo + " " + data[1] + "\n";
                                    }
                                }
                                if (temp.endsWith("\n")) {
                                    temp = temp.substring(0, temp.length() - 2);
                                }
                                NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
                                bigTextStyle.setBigContentTitle("Animes:");
                                bigTextStyle.bigText(temp);
                                NotificationCompat.Builder mBuilder =
                                        new NotificationCompat.Builder(context)
                                                .setSmallIcon(R.drawable.ic_not_r)
                                                .setContentTitle(NotTit)
                                                .setContentText(mess);
                                mBuilder.setVibrate(new long[]{100, 200, 100, 500});
                                mBuilder.setStyle(bigTextStyle);
                                int not = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("sonido", "0"));
                                mBuilder.setSound(UtilSound.getSoundUri(not));
                                mBuilder.setAutoCancel(true);
                                mBuilder.setPriority(Notification.PRIORITY_MAX);
                                mBuilder.setLights(Color.argb(0, 255, 128, 0), 5000, 2000);
                                mBuilder.setGroup("animeflv_group");
                                Intent resultIntent = new Intent(context, newMain.class);
                                PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                mBuilder.setContentIntent(resultPendingIntent);
                                int mNotificationId = 6991;
                                NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                mNotifyMgr.cancel(mNotificationId);
                                mNotifyMgr.notify(mNotificationId, mBuilder.build());
                            } else {
                                if (UtilNotBlocker.isBlocked()) {
                                    Log.d("Not Service", "isBlocked");
                                    UtilNotBlocker.setBlocked(false);
                                }
                            }
                        } else {
                            Log.d("JSON", "Es igual");
                        }
                    } else {
                        Log.d("Error", "Borrar archivo");
                        new File(file_loc).delete();
                    }
                }
            } else {
                Log.d("Conexion", "No hay internet isNull " + String.valueOf(s == null));
            }
        }
    }

    private static void startUpdate(Context context, String s) {
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            Log.d("ERROR", "Get Versioncode");
        }
        Log.d("Version", Integer.toString(versionCode) + " >> " + s.trim());
        String data = s.trim();
        if (data.trim().equals("")) {
            data = Integer.toString(versionCode);
        }
        if (versionCode >= Integer.parseInt(data.trim())) {
            Log.d("Version", "OK");
        } else {
            Boolean auto = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("autoUpdate", false);
            if (!auto) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
                Boolean isnot = sharedPreferences.getBoolean("notVer", false);
                if (isnot) {
                    Log.d("Version", "Not ya existe");
                } else {
                    sharedPreferences.edit().putBoolean("notVer", true).apply();
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.ic_not_r)
                                    .setContentTitle("AnimeFLV")
                                    .setContentText("Nueva Version Disponible!!!");
                    mBuilder.setVibrate(new long[]{100, 200, 100, 500});
                    int not = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("sonido", "0"));
                    mBuilder.setSound(UtilSound.getSoundUri(not));
                    mBuilder.setAutoCancel(true);
                    mBuilder.setPriority(Notification.PRIORITY_MAX);
                    mBuilder.setLights(Color.BLUE, 5000, 2000);
                    mBuilder.setGroup("animeflv_group");
                    Intent resultIntent = new Intent(context, newMain.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("act", "1");
                    resultIntent.putExtras(bundle);
                    PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(resultPendingIntent);
                    int mNotificationId = 1964;
                    NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                }
            } else {
                Log.d("Auto", "true");
                final File descarga = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache", "Animeflv_Nver.apk");
                int isDesc = context.getSharedPreferences("data", Context.MODE_PRIVATE).getInt("isDescDown", versionCode);
                Boolean downloading = context.getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("isDescRun", false);
                if (descarga.exists() && isDesc < Integer.parseInt(s.trim()) && !downloading) {
                    if (isDesc < Integer.parseInt(s.trim())) {
                        Log.d("Descarga", "Numero menor");
                        descarga.delete();
                        DescargarActualizacion(context, descarga, s.trim());
                    } else {
                        Log.d("Valores", String.valueOf(descarga.exists()) + " <--> " + String.valueOf(isDesc < versionCode) + " <--> " + String.valueOf(downloading));
                    }
                } else {
                    if (isDesc < Integer.parseInt(s.trim())) {
                        if (!descarga.exists()) {
                            if (!downloading) {
                                DescargarActualizacion(context, descarga, s.trim());
                                Log.d("Descarga", "No en curso");
                            } else {
                                int progreso = context.getSharedPreferences("data", Context.MODE_PRIVATE).getInt("ActProg", 0);
                                Log.d("Descarga", "En Curso " + Integer.toString(progreso));
                            }
                        }
                    } else {
                        Log.d("Actualizacion", "ya descargada");
                    }
                }
            }
        }
    }

    private static void DescargarActualizacion(final Context context, final File descarga, final String s) {
        context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putBoolean("isDescRun", true).apply();
        context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putBoolean("notVer", true).apply();
        Uri download = Uri.parse("https://github.com/jordyamc/Animeflv/blob/master/app/app-release.apk?raw=true");
        final ThinDownloadManager downloadManager = new ThinDownloadManager();
        final DownloadRequest downloadRequest = new DownloadRequest(download)
                .setDestinationURI(Uri.fromFile(descarga))
                .setStatusListener(new DownloadStatusListenerV1() {
                    @Override
                    public void onDownloadComplete(DownloadRequest downloadRequest) {
                        Log.d("Actualizacion", "OK");
                        context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putBoolean("isDescRun", false).apply();
                        context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putInt("isDescDown", Integer.parseInt(s.trim())).apply();

                        int not = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("sonido", "0"));
                        Uri ring = UtilSound.getSoundUri(not);
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context)
                                        .setSmallIcon(R.drawable.ic_not_r)
                                        .setContentTitle("AnimeFLV")
                                        .setContentText("Instalar version " + s.trim());
                        mBuilder.setVibrate(new long[]{100, 200, 100, 500});
                        mBuilder.setSound(ring, AudioManager.STREAM_NOTIFICATION);
                        mBuilder.setAutoCancel(true);
                        mBuilder.setPriority(Notification.PRIORITY_MAX);
                        mBuilder.setLights(Color.BLUE, 5000, 2000);
                        mBuilder.setGroup("animeflv_group");
                        Intent resultIntent = new Intent(Intent.ACTION_VIEW)
                                .setDataAndType(Uri.fromFile(descarga),
                                        "application/vnd.android.package-archive");
                        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(resultPendingIntent);
                        int mNotificationId = (int) Math.round(Math.random());
                        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotifyMgr.notify(mNotificationId, mBuilder.build());
                    }

                    @Override
                    public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                        context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putBoolean("notVer", false).apply();
                        context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putBoolean("isDescRun", false).apply();
                        Log.d("Actualizacion", "Fallada");
                    }

                    @Override
                    public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {
                        context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putInt("ActProg", progress).apply();
                    }
                });
        downloadManager.add(downloadRequest);
    }

    private static void Descargar(Context context, String titulo, String eid) {
        String[] semi = eid.replace("E", "").split("_");
        String aid = semi[0];
        String num = semi[1];
        int not = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("sonido", "0"));
        Uri ring = UtilSound.getSoundUri(not);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_not_r)
                        .setContentTitle(titulo)
                        .setContentText("Descargar capitulo " + num);
        mBuilder.setVibrate(new long[]{100, 200, 100, 500});
        mBuilder.setSound(ring, AudioManager.STREAM_NOTIFICATION);
        mBuilder.setAutoCancel(true);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setLights(Color.BLUE, 5000, 2000);
        mBuilder.setGroup("animeflv_group");
        Intent resultIntent = new Intent(context, BackDownload.class);
        Bundle bundle = new Bundle();
        bundle.putString("aid", aid);
        bundle.putString("num", num);
        bundle.putString("titulo", titulo);
        bundle.putString("eid", eid);
        resultIntent.putExtras(bundle);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        int mNotificationId = (int) Math.round(Math.random());
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
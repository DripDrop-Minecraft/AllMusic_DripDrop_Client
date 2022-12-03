package Coloryr.AllMusic.Hud;

import Coloryr.AllMusic.AllMusic;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.concurrent.Semaphore;

public class HudUtils {
    public String Info = "";
    public String List = "";
    public String Lyric = "";
    public SaveOBJ save;
    public final Object lock = new Object();
    private final Semaphore semaphore = new Semaphore(0);
    private InputStream inputStream;

    public HudUtils() {
        Thread thread = new Thread(this::run);
        thread.setName("allmusic_pic");
        thread.start();
    }

    public void close() {
        Info = List = Lyric = "";
        getClose();
    }

    private void getClose() {
        try {
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void run() {
        while (true) {
            try {
                semaphore.acquire();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setPos(String data) {
        synchronized (lock) {
            save = new Gson().fromJson(data, SaveOBJ.class);
        }
    }

    public void update() {
        if (save == null) return;
        synchronized (lock) {
            showSongInfo((float) save.getInfo().getX(), (float) save.getInfo().getY());
            showSongList((float) save.getList().getX(), (float) save.getList().getY());
            showSongLyrics((float) save.getLyric().getX(), (float) save.getLyric().getY());
        }
    }

    private void showSongLyrics(float originalX, float originalY) {
        try {
            if (save.isEnableLyric() && !Lyric.isEmpty()) {
                String[] temp = Lyric.split("\n");
                int offset = 0;
                for (String item : temp) {
                    AllMusic.drawText("§e" + item, originalX, originalY + offset);
                    offset += 10;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void showSongList(float originalX, float originalY) {
        try {
            if (save.isEnableList() && !List.isEmpty()) {
                String[] temp = List.split("\n");
                int offset = 0;
                for (String item : temp) {
                    if (item.length() > 18) {
                        item = item.substring(0, 19) + "...";
                    }
                    AllMusic.drawText("§d" + item, originalX, originalY + offset);
                    offset += 10;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void showSongInfo(float originalX, float originalY) {
        try {
            if (save.isEnableInfo() && !Info.isEmpty()) {
                String[] temp = Info.split("\n");
                String durationRegex = "[0-9]{2}:[0-9]{2}/[0-9]{2}:[0-9]{2}";
                String name = "§a" + temp[0].replaceAll(durationRegex,"").trim();
                if (name.length() > 20) {
                    name = name.substring(0, 21) + "...";
                }
                String demander = "§a点歌: " + temp[4].replace("by:","");
                String from = "§a来源: " + temp[3];
                if (from.length() > 20) {
                    from = from.substring(0, 21) + "...";
                }
                String byWhom;
                if ("电台".equals(temp[3])) {
                    byWhom = "§a主播: " + temp[1];
                } else {
                    byWhom = "§a演唱: " + temp[1];
                }
                if (byWhom.length() > 23) {
                    byWhom = byWhom.substring(0, 24) + "...";
                }
                AllMusic.drawText(name, originalX, originalY);
                AllMusic.drawText(byWhom, originalX, originalY + 10);
                AllMusic.drawText("", originalX, originalY + 20);
                AllMusic.drawText(from, originalX, originalY + 30);
                AllMusic.drawText(demander, originalX, originalY + 40);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}

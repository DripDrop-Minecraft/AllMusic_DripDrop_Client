package coloryr.allmusic.player;

import coloryr.allmusic.AllMusic;
import coloryr.allmusic.player.decoder.BuffPack;
import coloryr.allmusic.player.decoder.IDecoder;
import coloryr.allmusic.player.decoder.flac.DataFormatException;
import coloryr.allmusic.player.decoder.flac.FlacDecoder;
import coloryr.allmusic.player.decoder.mp3.Mp3Decoder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import javax.sound.sampled.AudioFormat;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class APlayer {

    private HttpClient client;
    private boolean isClose;
    private IDecoder decoder;
    private final List<URL> urls = new ArrayList<>();
    private int time;
    private URL url;

    private final Semaphore semaphore = new Semaphore(0);
    private int index = -1;

    public APlayer() {
        try {
            new Thread(this::run, "allmusic_run").start();
            client = HttpClientBuilder.create().useSystemProperties().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void set(String time) {
        try {
            int time1 = Integer.parseInt(time);
            set(time1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void set(int time) {
        close();
        this.time = time;
        urls.add(url);
        semaphore.release();
    }

    private void run() {
        while (true) {
            try {
                semaphore.acquire();
                if (urls.size() > 0) {
                    AllMusic.isPlay = true;
                    index = AL10.alGenSources();
                    url = urls.remove(urls.size() - 1);
                    urls.clear();
                    try {
                        decoder = new FlacDecoder();
                        decoder.set(client, url);
                    } catch (DataFormatException e) {
                        decoder = new Mp3Decoder();
                        decoder.set(client, url);
                    }
                    AudioFormat audioformat = new AudioFormat(decoder.getOutputFrequency(),
                            16,
                            decoder.getOutputChannels(),
                            true,
                            false);
                    if (time != 0) {
                        decoder.set(time);
                    }
                    isClose = false;

                    int soundFormat;
                    if (audioformat.getChannels() == 1) {
                        if (audioformat.getSampleSizeInBits() == 8) {
                            soundFormat = AL10.AL_FORMAT_MONO8;
                        } else if (audioformat.getSampleSizeInBits() == 16) {
                            soundFormat = AL10.AL_FORMAT_MONO16;
                        } else {
                            break;
                        }
                    } else if (audioformat.getChannels() == 2) {
                        if (audioformat.getSampleSizeInBits() == 8) {
                            soundFormat = AL10.AL_FORMAT_STEREO8;
                        } else if (audioformat.getSampleSizeInBits() == 16) {
                            soundFormat = AL10.AL_FORMAT_STEREO16;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }

                    while (true) {
                        try {
                            if (isClose)
                                break;

                            BuffPack output = decoder.decodeFrame();
                            if (output == null)
                                break;

                            ByteBuffer byteBuffer = BufferUtils.createByteBuffer(
                                    output.len).put(output.buff, 0, output.len);
                            ((Buffer) byteBuffer).flip();

                            IntBuffer intBuffer;

                            intBuffer = BufferUtils.createIntBuffer(1);
                            AL10.alGenBuffers(intBuffer);

                            AL10.alBufferData(intBuffer.get(0), soundFormat, byteBuffer, (int) audioformat.getSampleRate());
                            AL10.alSourcef(index, AL10.AL_GAIN, MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.RECORDS));

                            AL10.alSourceQueueBuffers(index, intBuffer);
                            if (AL10.alGetSourcei(index,
                                    AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) {
                                AL10.alSourcePlay(index);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    try {
                        while (!isClose && AL10.alGetSourcei(index,
                                AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING) {
                            AL10.alSourcef(index, AL10.AL_GAIN, MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.RECORDS));
                            Thread.sleep(10);
                        }
                        AL10.alSourceStop(index);
                        int m_numqueued = AL10.alGetSourcei(index, AL10.AL_BUFFERS_QUEUED);
                        while (m_numqueued > 0) {
                            int temp = AL10.alSourceUnqueueBuffers(index);
                            AL10.alDeleteBuffers(temp);
                            m_numqueued--;
                        }
                        AL10.alDeleteSources(index);
                        decoder.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    AllMusic.isPlay = false;
                } else {
                    Thread.sleep(50);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setMusic(URL url) {
        time = 0;
        close();
        this.url = url;
        urls.add(url);
        semaphore.release();
    }

    public void close() {
        urls.clear();
        isClose = true;
        if (decoder != null) {
            try {
                decoder.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

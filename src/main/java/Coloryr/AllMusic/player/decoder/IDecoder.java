package Coloryr.AllMusic.player.decoder;

public interface IDecoder {
    BuffPack decodeFrame() throws Exception;

    void close() throws Exception;

    boolean set() throws Exception;

    int getOutputFrequency();

    int getOutputChannels();

    void set(int time);
}

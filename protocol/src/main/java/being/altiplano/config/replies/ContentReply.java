package being.altiplano.config.replies;

import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;

/**
 * Created by gaoyuan on 22/02/2017.
 */
abstract class ContentReply implements Reply {
    final String content;

    public ContentReply(String content) {
        this.content = content;
    }

    public ContentReply(byte[] data) {
        this(MsgConverter.bytesToString(data));
    }

    public String getContent() {
        return content;
    }

    public byte[] toBytes() {
        return MsgConverter.stringToBytes(content);
    }
}

package being.altiplano.config.replies;

import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;

/**
 * A Reply with content in string
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

    @Override
    public String toString() {
        String content = getContent();
        return this.getClass().getSimpleName() +
                " \"" + content + "\"(" + content.length() + ")";
    }
}

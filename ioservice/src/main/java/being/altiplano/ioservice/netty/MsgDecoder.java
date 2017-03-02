package being.altiplano.ioservice.netty;

import being.altiplano.config.Msg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by gaoyuan on 01/03/2017.
 */
class MsgDecoder extends ByteToMessageDecoder {
    private static class Pack {
        private boolean headDone = false;
        private int code;
        private int len;
        private byte[] data = null;
        private int offset;

        void setHead(int code, int len) {
            if (!headDone) {
                this.code = code;
                this.len = len;
                this.data = new byte[len];
                this.offset = 0;
                this.headDone = true;
            } else {
                throw new IllegalStateException();
            }
        }

        int requires() {
            if (!headDone) {
                throw new IllegalStateException();
            }
            return len - offset;
        }

        void readBytes(ByteBuf in, int readLen) {
            in.readBytes(data, offset, readLen);
            offset += readLen;
        }

        void reset() {
            this.code = 0;
            this.len = 0;
            this.data = null;
            this.offset = 0;
            this.headDone = false;
        }

        Msg toMsg() {
            return new Msg(this.code, this.data);
        }
    }

    private Pack pack;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.pack = new Pack();
        super.handlerAdded(ctx);
    }

    @Override
    protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved0(ctx);
        this.pack = null;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!pack.headDone) {
            if (in.readableBytes() < 8) {
                return;
            } else {
                int code = in.readInt();
                int len = in.readInt();
                pack.setHead(code, len);
            }
        }
        int requires = pack.requires();
        int remains = in.readableBytes();
        int cpyLen = Math.min(requires, remains);
        pack.readBytes(in, cpyLen);
        requires = pack.requires();
        if (requires == 0) {
            out.add(pack.toMsg());
            this.pack.reset();
        }
    }
}
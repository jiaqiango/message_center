/*
 * MIT License
 *
 * Copyright (c) 2022 jiaqiango
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.asmyun.message.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Message message = new Message();
        message.setMagicNumber(in.readInt());  // 读取魔数
        message.setMainVersion(in.readByte()); // 读取主版本号
        message.setSubVersion(in.readByte()); // 读取次版本号
        message.setModifyVersion(in.readByte()); // 读取修订版本号
        CharSequence sessionId = in.readCharSequence(
                Constants.SESSION_ID_LENGTH, Charset.defaultCharset()); // 读取sessionId
        message.setSessionId((String) sessionId);

        message.setMessageType(MessageTypeEnum.get(in.readByte())); // 读取当前的消息类型
        short attachmentSize = in.readShort(); // 读取附件长度
        for (short i = 0; i < attachmentSize; i++) {
            int keyLength = in.readInt(); // 读取键长度和数据
            CharSequence key = in.readCharSequence(keyLength, Charset.defaultCharset());
            int valueLength = in.readInt(); // 读取值长度和数据
            CharSequence value = in.readCharSequence(valueLength, Charset.defaultCharset());
            message.addAttachment(key.toString(), value.toString());
        }

        int bodyLength = in.readInt(); // 读取消息体长度和数据
        CharSequence body = in.readCharSequence(bodyLength, Charset.defaultCharset());
        message.setBody(body.toString());
        out.add(message);
    }

}

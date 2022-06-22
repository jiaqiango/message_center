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
import io.netty.handler.codec.MessageToByteEncoder;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;

public class MessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        // 这里会判断消息类型是不是EMPTY类型，如果是EMPTY类型，则表示当前消息不需要写入到管道中
        if (msg.getMessageType() != MessageTypeEnum.EMPTY) {
            out.writeInt(Constants.MAGIC_NUMBER); // 写入当前的魔数
            out.writeByte(Constants.MAIN_VERSION); // 写入当前的主版本号
            out.writeByte(Constants.SUB_VERSION); // 写入当前的次版本号
            out.writeByte(Constants.MODIFY_VERSION); // 写入当前的修订版本号
            if (!StringUtils.hasText(msg.getSessionId())) {
                // 生成一个sessionId，并将其写入到字节序列中
//                String sessionId = SessionIdGenerator.generate();
//                msg.setSessionId(sessionId);
                out.writeCharSequence(null, Charset.defaultCharset());
            }

            out.writeByte(msg.getMessageType().getType()); // 写入当前消息的类型
            out.writeShort(msg.getAttachments().size()); // 写入当前消息的附加参数数量
            msg.getAttachments().forEach((key, value) -> {
                Charset charset = Charset.defaultCharset();
                out.writeInt(key.length()); // 写入键的长度
                out.writeCharSequence(key, charset); // 写入键数据
                out.writeInt(value.length()); // 希尔值的长度
                out.writeCharSequence(value, charset); // 写入值数据
            });

            if (null == msg.getBody()) {
                out.writeInt(0); // 如果消息体为空，则写入0，表示消息体长度为0
            } else {
                out.writeInt(msg.getBody().length());
                out.writeCharSequence(msg.getBody(), Charset.defaultCharset());
            }
        }
    }
}

/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.goalie.builtin;

import io.netty.channel.Channel;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.goalie.ApiContext;
import org.aoju.bus.goalie.consts.NettyMode;
import org.aoju.bus.logger.Logger;

import java.io.File;

/**
 * 下载权限配置
 *
 * @author Kimi Liu
 * @version 6.0.9
 * @since JDK 1.8++
 */
public class DownloadLocalPermission extends AbstractClientClientProcessor {

    public DownloadLocalPermission(ConfigClient configClient, NettyMode nettyMode) {
        super(configClient, nettyMode);
    }

    @Override
    protected void on(Channel channel, String data) {
        String localConfigFile = ApiContext.getConfig().getLocalPermissionConfigFile();
        FileKit.writeString(data, new File(localConfigFile), Charset.DEFAULT_UTF_8);
        Logger.info("权限配置下载成功，保存路径：{}", localConfigFile);
        this.configClient.getPermissionManager().loadPermissionCache(data);
    }

}

package cn.lds.common.data;

import cn.lds.common.data.base.BaseModel;

/**
 * Created by leadingsoft on 2017/11/30.
 * 系统配置 数据类型
 */

public class SystemConfigModel extends BaseModel {


    /**
     * data : {"interval":0,"serviceCall":"string"}
     * timestamp : 2017-12-19T06:20:46.030Z
     */

    private DataBean data;
    private long timestamp;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public static class DataBean {
        /**
         * 短信发送最短间隔（秒）
         */

        private int interval;
        /**
         * 400服务电话
         */
        private String serviceCall;

        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }

        public String getServiceCall() {
            return serviceCall;
        }

        public void setServiceCall(String serviceCall) {
            this.serviceCall = serviceCall;
        }
    }
}

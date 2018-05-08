package cn.lds.common.data;

import java.util.List;

import cn.lds.common.data.base.BaseModel;

/**
 * 经销商列表数据模型
 * Created by leadingsoft on 17/12/26.
 */

public class DealerListModel extends BaseModel {

    /**
     * data : [{"address":"string","dealerCode":"string","dealerName":"string","dealerPhone":"string","latitude":0,"longitude":0}]
     * timestamp : 2017-12-26T01:29:13.798Z
     */

    private String timestamp;
    private List<DataBean> data;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * 详细地址
         */
        private String address;
        /**
         * 经销商代码
         */
        private String dealerCode;
        /**
         * 经销商名称
         */
        private String dealerName;
        /**
         * 电话
         */
        private String dealerPhone;
        /**
         * 纬度
         */
        private double latitude;
        /**
         * 经度
         */
        private double longitude;

        private boolean subscriberDealer;

        private boolean collected;

        private String collectionId;

//        		"dealerCode": "CFA1174",
//                        "dealerName": "惠州市坤隆汽车销售服务有限公司",
//                        "longitude": 114.409401,
//                        "latitude": 23.127938,
//                        "address": "惠州市江北西区三环路旁",
//                        "dealerPhone": "0752-2799111",
//                        "activationDealer": false,
//                        "subscriberDealer": false,
//                        "distance": 9248.573399825265,
//                        "collected": false,
//                        "collectionId": null

        public String getCollectionId() {
            return collectionId;
        }

        public void setCollectionId( String collectionId ) {
            this.collectionId = collectionId;
        }

        public boolean isCollected() {
            return collected;
        }

        public void setCollected( boolean collected ) {
            this.collected = collected;
        }

        public boolean isSubscriberDealer() {
            return subscriberDealer;
        }

        public void setSubscriberDealer( boolean subscriberDealer ) {
            this.subscriberDealer = subscriberDealer;
        }

        private int distance;

        public int getDistance() {
            return distance;
        }

        public void setDistance( int distance ) {
            this.distance = distance;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getDealerCode() {
            return dealerCode;
        }

        public void setDealerCode(String dealerCode) {
            this.dealerCode = dealerCode;
        }

        public String getDealerName() {
            return dealerName;
        }

        public void setDealerName(String dealerName) {
            this.dealerName = dealerName;
        }

        public String getDealerPhone() {
            return dealerPhone;
        }

        public void setDealerPhone(String dealerPhone) {
            this.dealerPhone = dealerPhone;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}

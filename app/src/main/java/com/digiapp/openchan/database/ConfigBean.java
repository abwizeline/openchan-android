package com.digiapp.openchan.database;

public class ConfigBean {

    public boolean error;

    public Details[] data;

    public static Details fromServerDetails(ServerDetails serverDetails){
        Details details = new Details();
        details.IP = serverDetails.ip;
        details.CountryShort = serverDetails.geo;
        details.city = serverDetails.title;
        details.CountryLong = serverDetails.title;
        details.OpenVPN_ConfigData = serverDetails.config;
        return details;
    }

    public static class Details{

        public String IP;

        public String CountryLong;

        public String CountryShort;

        public String city;

        public String OpenVPN_ConfigData;
    }

    public boolean isError() {
        return error;
    }
}

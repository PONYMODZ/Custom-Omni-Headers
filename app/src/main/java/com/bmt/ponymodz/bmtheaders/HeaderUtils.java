package com.bmt.ponymodz.bmtheaders;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class HeaderUtils {
    private static final boolean DEBUG = true;
    private static final String TAG = "BrowseHeaderActivity";

    public static class DaylightHeaderInfo {
        public int mType = 0;
        public int mHour = -1;
        public int mDay = -1;
        public int mMonth = -1;
        public String mImage;
        public String mName;
    }

    public static String loadHeaders(Resources res, String headerName, List<DaylightHeaderInfo> headersList) throws XmlPullParserException, IOException {
        headersList.clear();
        String creatorName = null;
        InputStream in = null;
        XmlPullParser parser = null;

        try {
            if (headerName == null) {
                if (DEBUG) Log.i(TAG, "Load header pack config daylight_header.xml");
                in = res.getAssets().open("daylight_header.xml");
            } else {
                int idx = headerName.lastIndexOf(".");
                String headerConfigFile = headerName.substring(idx + 1) + ".xml";
                if (DEBUG) Log.i(TAG, "Load header pack config " + headerConfigFile);
                in = res.getAssets().open(headerConfigFile);
            }
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();
            parser.setInput(in, "UTF-8");
            creatorName = loadResourcesFromXmlParser(parser, headersList);
        } finally {
            // Cleanup resources
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return creatorName;
    }

    private static String loadResourcesFromXmlParser(XmlPullParser parser, List<DaylightHeaderInfo> headersList) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        String creatorName = null;
        do {
            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equalsIgnoreCase("day_header")) {
                DaylightHeaderInfo headerInfo = new DaylightHeaderInfo();
                headerInfo.mType = 0;
                String day = parser.getAttributeValue(null, "day");
                if (day != null) {
                    headerInfo.mDay = Integer.valueOf(day);
                }
                String month = parser.getAttributeValue(null, "month");
                if (month != null) {
                    headerInfo.mMonth = Integer.valueOf(month);
                }
                String image = parser.getAttributeValue(null, "image");
                if (image != null) {
                    headerInfo.mImage = image;
                }
                String imageName = parser.getAttributeValue(null, "name");
                if (imageName != null) {
                    headerInfo.mName = imageName;
                }
                if (headerInfo.mImage != null && headerInfo.mDay != -1 && headerInfo.mMonth != -1) {
                    headersList.add(headerInfo);
                }
            } else if (name.equalsIgnoreCase("hour_header")) {
                DaylightHeaderInfo headerInfo = new DaylightHeaderInfo();
                headerInfo.mType = 1;
                String hour = parser.getAttributeValue(null, "hour");
                if (hour != null) {
                    headerInfo.mHour = Integer.valueOf(hour);
                }
                String image = parser.getAttributeValue(null, "image");
                if (image != null) {
                    headerInfo.mImage = image;
                }
                String imageName = parser.getAttributeValue(null, "name");
                if (imageName != null) {
                    headerInfo.mName = imageName;
                }
                if (headerInfo.mImage != null && headerInfo.mHour != -1) {
                    headersList.add(headerInfo);
                }
            } else if (name.equalsIgnoreCase("random_header") ||
                    name.equalsIgnoreCase("list_header")) {
                DaylightHeaderInfo headerInfo = new DaylightHeaderInfo();
                headerInfo.mType = 2;
                String image = parser.getAttributeValue(null, "image");
                if (image != null) {
                    headerInfo.mImage = image;
                }
                String imageName = parser.getAttributeValue(null, "name");
                if (imageName != null) {
                    headerInfo.mName = imageName;
                }
                if (headerInfo.mImage != null) {
                    headersList.add(headerInfo);
                }
            } else if (name.equalsIgnoreCase("meta_data")) {
                creatorName = parser.getAttributeValue(null, "creator");
                if (DEBUG) Log.i(TAG, "creator = " + creatorName);
            }
        } while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT);
        if (DEBUG) Log.i(TAG, "loaded size = " + headersList.size());
        return creatorName;
    }
}
